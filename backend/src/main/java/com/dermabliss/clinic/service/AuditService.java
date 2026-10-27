package com.dermabliss.clinic.service;

import com.dermabliss.clinic.entity.AuditLog;
import com.dermabliss.clinic.entity.User;
import com.dermabliss.clinic.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User user, String eventType, String entityName, String entityId, String action, String ipAddress, String userAgent, String detailsJson) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setEventType(eventType);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setIpAddress(ipAddress != null ? ipAddress : "127.0.0.1");
        auditLog.setUserAgent(userAgent);
        auditLog.setDetailsJson(detailsJson);

        auditLogRepository.save(auditLog);
    }
}
