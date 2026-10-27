package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.*;
import com.dermabliss.clinic.entity.Patient;
import com.dermabliss.clinic.entity.RefreshToken;
import com.dermabliss.clinic.entity.Role;
import com.dermabliss.clinic.entity.User;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.exception.UserAlreadyExistsException;
import com.dermabliss.clinic.repository.PatientRepository;
import com.dermabliss.clinic.repository.RefreshTokenRepository;
import com.dermabliss.clinic.repository.UserRepository;
import com.dermabliss.clinic.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       PatientRepository patientRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse registerPatient(RegisterRequest req, String ipAddress, String userAgent) {
        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new UserAlreadyExistsException("A user with this phone number is already registered");
        }
        if (req.getEmail() != null && !req.getEmail().isBlank() && userRepository.existsByEmail(req.getEmail())) {
            throw new UserAlreadyExistsException("A user with this email is already registered");
        }

        User user = new User();
        user.setPhoneNumber(req.getPhoneNumber());
        user.setEmail(req.getEmail() != null && !req.getEmail().isBlank() ? req.getEmail() : null);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.ROLE_PATIENT);
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFullName(req.getFullName());
        patient.setMobileNumber(req.getPhoneNumber());
        patient.setEmail(user.getEmail());
        patient.setGender(req.getGender());
        patient.setDateOfBirth(req.getDateOfBirth());
        patient.setPatientNumber("PT-" + String.format("%06d", user.getId()));
        patient.setAccountStatus("ACTIVE");
        patient = patientRepository.save(patient);

        auditService.log(user, "USER_REGISTER", "users", String.valueOf(user.getId()), "CREATE", ipAddress, userAgent, "Patient self-registration");

        return generateAuthResponse(user, patient);
    }

    @Transactional
    public AuthResponse login(AuthRequest req, String ipAddress, String userAgent) {
        User user = userRepository.findByPhoneNumber(req.getPhoneNumber())
                .or(() -> userRepository.findByEmail(req.getPhoneNumber()))
                .orElseThrow(() -> new BadCredentialsException("Invalid phone number or password"));

        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(LocalDateTime.now())) {
            throw new BadCredentialsException("Account temporarily locked due to multiple failed attempts. Try again later.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);
            if (failedAttempts >= 5) {
                user.setLockoutUntil(LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(user);
            auditService.log(user, "LOGIN_FAILED", "users", String.valueOf(user.getId()), "LOGIN", ipAddress, userAgent, "Failed password attempt");
            throw new BadCredentialsException("Invalid phone number or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        userRepository.save(user);

        Patient patient = patientRepository.findByUserId(user.getId()).orElse(null);

        auditService.log(user, "LOGIN_SUCCESS", "users", String.valueOf(user.getId()), "LOGIN", ipAddress, userAgent, "Successful login");

        return generateAuthResponse(user, patient);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        String hashed = tokenProvider.hashToken(req.getRefreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHashAndIsRevokedFalse(hashed)
                .orElseThrow(() -> new BadCredentialsException("Invalid or revoked refresh token"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new BadCredentialsException("Refresh token has expired");
        }

        // Revoke the used refresh token (One-time rotation)
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        Patient patient = patientRepository.findByUserId(user.getId()).orElse(null);

        return generateAuthResponse(user, patient);
    }

    @Transactional
    public void logout(String rawRefreshToken, Long userId) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String hashed = tokenProvider.hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHashAndIsRevokedFalse(hashed).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
        if (userId != null) {
            refreshTokenRepository.revokeAllByUserId(userId);
        }
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Patient patient = patientRepository.findByUserId(userId).orElse(null);
        return mapToUserDto(user, patient);
    }

    private AuthResponse generateAuthResponse(User user, Patient patient) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole().name());
        String rawRefreshToken = tokenProvider.generateRefreshTokenString();

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setTokenHash(tokenProvider.hashToken(rawRefreshToken));
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(tokenProvider.getRefreshExpirationMs() / 1000));
        refreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(refreshTokenEntity);

        UserDto userDto = mapToUserDto(user, patient);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                tokenProvider.getAccessExpirationSeconds(),
                userDto
        );
    }

    private UserDto mapToUserDto(User user, Patient patient) {
        String fullName = patient != null ? patient.getFullName() :
                (user.getRole() == Role.ROLE_ADMIN ? "Dr. Ankita Santosh Koli" : "Clinic Patient");

        Long patientId = patient != null ? patient.getId() : null;
        String patientNumber = patient != null ? patient.getPatientNumber() : null;

        return new UserDto(
                user.getId(),
                user.getPhoneNumber(),
                user.getEmail(),
                fullName,
                user.getRole().name(),
                patientId,
                patientNumber
        );
    }
}
