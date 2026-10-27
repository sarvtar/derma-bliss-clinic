package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.QueueTokenDto;
import com.dermabliss.clinic.entity.QueueStatus;
import com.dermabliss.clinic.entity.QueueToken;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.repository.QueueTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueService {

    private final QueueTokenRepository queueTokenRepository;

    public QueueService(QueueTokenRepository queueTokenRepository) {
        this.queueTokenRepository = queueTokenRepository;
    }

    @Transactional(readOnly = true)
    public List<QueueTokenDto> getQueueForDate(LocalDate date) {
        return queueTokenRepository.findByTokenDateOrderByTokenNumberAsc(date)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QueueTokenDto> getActiveWaitingQueue(LocalDate date) {
        return queueTokenRepository.findByTokenDateAndStatusOrderByTokenNumberAsc(date, QueueStatus.WAITING)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QueueTokenDto callNextToken(LocalDate date) {
        List<QueueToken> waiting = queueTokenRepository.findByTokenDateAndStatusOrderByTokenNumberAsc(date, QueueStatus.WAITING);
        if (waiting.isEmpty()) {
            return null;
        }

        QueueToken nextToken = waiting.get(0);
        nextToken.setStatus(QueueStatus.IN_CONSULTATION);
        nextToken.setCalledAt(LocalDateTime.now());
        nextToken = queueTokenRepository.save(nextToken);

        return mapToDto(nextToken);
    }

    @Transactional
    public QueueTokenDto completeToken(Long tokenId) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue token not found: " + tokenId));

        token.setStatus(QueueStatus.COMPLETED);
        token.setCompletedAt(LocalDateTime.now());
        token = queueTokenRepository.save(token);

        return mapToDto(token);
    }

    private QueueTokenDto mapToDto(QueueToken token) {
        QueueTokenDto dto = new QueueTokenDto();
        dto.setId(token.getId());
        dto.setTokenNumber(token.getTokenNumber());
        dto.setTokenDate(token.getTokenDate());
        if (token.getAppointment() != null) {
            dto.setAppointmentId(token.getAppointment().getId());
            dto.setAppointmentNumber(token.getAppointment().getAppointmentNumber());
        }
        dto.setPatientId(token.getPatient().getId());
        dto.setPatientName(token.getPatient().getFullName());
        dto.setPatientMobile(token.getPatient().getMobileNumber());
        dto.setStatus(token.getStatus().name());
        dto.setCalledAt(token.getCalledAt());
        dto.setCompletedAt(token.getCompletedAt());
        dto.setCreatedAt(token.getCreatedAt());
        return dto;
    }
}
