package com.example.banking.service;

import com.example.banking.entity.AuditLog;
import com.example.banking.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import com.example.banking.security.AuthenticationFacade;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuthenticationFacade authenticationFacade;

    public AuditLogService(AuditLogRepository auditLogRepository, AuthenticationFacade authenticationFacade) {
        this.auditLogRepository = auditLogRepository;
        this.authenticationFacade = authenticationFacade;
    }

    public void logAction(String action, String details) {
        String username = authenticationFacade.getCurrentUsername();
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setPerformedBy(username);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);
        auditLogRepository.save(log);
    }



    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}

