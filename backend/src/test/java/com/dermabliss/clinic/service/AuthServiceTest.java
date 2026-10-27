package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.AuthRequest;
import com.dermabliss.clinic.dto.AuthResponse;
import com.dermabliss.clinic.dto.RefreshTokenRequest;
import com.dermabliss.clinic.dto.RegisterRequest;
import com.dermabliss.clinic.entity.Patient;
import com.dermabliss.clinic.entity.RefreshToken;
import com.dermabliss.clinic.entity.Role;
import com.dermabliss.clinic.entity.User;
import com.dermabliss.clinic.exception.UserAlreadyExistsException;
import com.dermabliss.clinic.repository.PatientRepository;
import com.dermabliss.clinic.repository.RefreshTokenRepository;
import com.dermabliss.clinic.repository.UserRepository;
import com.dermabliss.clinic.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setPhoneNumber("+919876543210");
        testUser.setEmail("patient@example.com");
        testUser.setPasswordHash("hashed_secret");
        testUser.setRole(Role.ROLE_PATIENT);
        testUser.setStatus("ACTIVE");
        testUser.setFailedLoginAttempts(0);

        testPatient = new Patient();
        testPatient.setId(100L);
        testPatient.setUser(testUser);
        testPatient.setFullName("Rohan Sharma");
        testPatient.setMobileNumber("+919876543210");
        testPatient.setPatientNumber("PT-000010");
        testPatient.setAccountStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should successfully authenticate valid user and issue tokens")
    void login_Success() {
        AuthRequest req = new AuthRequest("+919876543210", "Password123!");

        when(userRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "hashed_secret")).thenReturn(true);
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(testPatient));
        when(tokenProvider.generateAccessToken(eq(10L), eq("+919876543210"), eq("ROLE_PATIENT"))).thenReturn("mock_access_jwt");
        when(tokenProvider.generateRefreshTokenString()).thenReturn("mock_refresh_uuid");
        when(tokenProvider.hashToken("mock_refresh_uuid")).thenReturn("mock_hashed_refresh");
        when(tokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
        when(tokenProvider.getAccessExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.login(req, "127.0.0.1", "Mozilla/5.0");

        assertNotNull(response);
        assertEquals("mock_access_jwt", response.getAccessToken());
        assertEquals("mock_refresh_uuid", response.getRefreshToken());
        assertEquals(900L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("Rohan Sharma", response.getUser().getFullName());
        assertEquals("ROLE_PATIENT", response.getUser().getRole());

        // Verify failed attempts reset to 0
        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNull(testUser.getLockoutUntil());
        verify(userRepository, atLeastOnce()).save(testUser);
    }

    @Test
    @DisplayName("Should increment failed attempts on invalid password")
    void login_WrongPassword_IncrementsFailedAttempts() {
        AuthRequest req = new AuthRequest("+919876543210", "WrongPassword!");

        when(userRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword!", "hashed_secret")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                authService.login(req, "127.0.0.1", "TestClient"));

        assertEquals("Invalid phone number or password", ex.getMessage());
        assertEquals(1, testUser.getFailedLoginAttempts());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should lock account for 15 minutes after 5 consecutive failed attempts")
    void login_FifthFailedAttempt_LocksAccount() {
        testUser.setFailedLoginAttempts(4);
        AuthRequest req = new AuthRequest("+919876543210", "WrongAgain!");

        when(userRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongAgain!", "hashed_secret")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () ->
                authService.login(req, "127.0.0.1", "TestClient"));

        assertEquals(5, testUser.getFailedLoginAttempts());
        assertNotNull(testUser.getLockoutUntil());
        assertTrue(testUser.getLockoutUntil().isAfter(LocalDateTime.now().plusMinutes(14)));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should reject login if account is locked out")
    void login_AccountLocked_ThrowsException() {
        testUser.setLockoutUntil(LocalDateTime.now().plusMinutes(10));
        AuthRequest req = new AuthRequest("+919876543210", "AnyPassword");

        when(userRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.of(testUser));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                authService.login(req, "127.0.0.1", "TestClient"));

        assertTrue(ex.getMessage().contains("temporarily locked"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should register new patient successfully")
    void registerPatient_Success() {
        RegisterRequest req = new RegisterRequest(
                "+919876500000",
                "Secret@123",
                "Pooja Deshmukh",
                "pooja@example.com",
                "FEMALE",
                LocalDate.of(1995, 5, 20)
        );

        when(userRepository.existsByPhoneNumber("+919876500000")).thenReturn(false);
        when(userRepository.existsByEmail("pooja@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded_pass");

        User savedUser = new User();
        savedUser.setId(55L);
        savedUser.setPhoneNumber(req.getPhoneNumber());
        savedUser.setEmail(req.getEmail());
        savedUser.setRole(Role.ROLE_PATIENT);
        savedUser.setStatus("ACTIVE");

        Patient savedPatient = new Patient();
        savedPatient.setId(505L);
        savedPatient.setUser(savedUser);
        savedPatient.setFullName(req.getFullName());
        savedPatient.setPatientNumber("PT-000055");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        when(tokenProvider.generateAccessToken(eq(55L), eq("+919876500000"), eq("ROLE_PATIENT"))).thenReturn("access_token_pooja");
        when(tokenProvider.generateRefreshTokenString()).thenReturn("refresh_uuid_pooja");
        when(tokenProvider.hashToken("refresh_uuid_pooja")).thenReturn("hashed_refresh_pooja");
        when(tokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
        when(tokenProvider.getAccessExpirationSeconds()).thenReturn(900L);

        AuthResponse resp = authService.registerPatient(req, "192.168.1.1", "MobileApp");

        assertNotNull(resp);
        assertEquals("access_token_pooja", resp.getAccessToken());
        assertEquals("Pooja Deshmukh", resp.getUser().getFullName());
        assertEquals("PT-000055", resp.getUser().getPatientNumber());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should throw exception if phone number already exists on registration")
    void registerPatient_DuplicatePhone_ThrowsException() {
        RegisterRequest req = new RegisterRequest(
                "+919876543210",
                "Secret@123",
                "Existing Patient",
                "test@test.com",
                "FEMALE",
                LocalDate.of(1992, 1, 1)
        );

        when(userRepository.existsByPhoneNumber("+919876543210")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                authService.registerPatient(req, "127.0.0.1", "WebClient"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should rotate refresh token on valid refresh request")
    void refreshToken_RotatesTokenSuccessfully() {
        String rawToken = "valid_raw_refresh_token";
        String hashedToken = "hashed_raw_token";

        RefreshToken storedToken = new RefreshToken();
        storedToken.setId(1L);
        storedToken.setUser(testUser);
        storedToken.setTokenHash(hashedToken);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(5));
        storedToken.setRevoked(false);

        when(tokenProvider.hashToken(rawToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByTokenHashAndIsRevokedFalse(hashedToken)).thenReturn(Optional.of(storedToken));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(testPatient));

        when(tokenProvider.generateAccessToken(eq(10L), eq("+919876543210"), eq("ROLE_PATIENT"))).thenReturn("new_access_token");
        when(tokenProvider.generateRefreshTokenString()).thenReturn("new_refresh_token");
        when(tokenProvider.hashToken("new_refresh_token")).thenReturn("new_hashed_refresh");
        when(tokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
        when(tokenProvider.getAccessExpirationSeconds()).thenReturn(900L);

        AuthResponse resp = authService.refreshToken(new RefreshTokenRequest(rawToken));

        assertNotNull(resp);
        assertEquals("new_access_token", resp.getAccessToken());
        assertEquals("new_refresh_token", resp.getRefreshToken());
        assertTrue(storedToken.isRevoked(), "Original refresh token must be revoked during rotation");
        verify(refreshTokenRepository, atLeastOnce()).save(storedToken);
    }
}
