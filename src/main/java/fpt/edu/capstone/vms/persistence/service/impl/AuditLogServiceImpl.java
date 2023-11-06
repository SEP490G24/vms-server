package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.service.IAuditLogService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class AuditLogServiceImpl extends GenericServiceImpl<AuditLog, UUID> implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.init(auditLogRepository);
    }

    @Override
    public void logAudit(Constants.AuditType auditType
        , String siteId
        , String organizationId
        , String primaryKey
        , String tableName
        , String oldValue
        , String newValue) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditType(auditType);
        auditLog.setCode(generateAuditLogCode(auditType));
        auditLog.setSiteId(siteId);
        auditLog.setOrganizationId(organizationId);
        auditLog.setPrimaryKey(primaryKey);
        auditLog.setTableName(tableName);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);

        auditLogRepository.save(auditLog);
    }

    private static String generateAuditLogCode(Constants.AuditType auditType) {
        String code = "";
        switch (auditType) {
            case CREATE -> code = "C";
            case UPDATE -> code = "U";
            case DELETE -> code = "D";
            case NONE -> code = "N";
            default -> code = "A";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String dateCreated = dateFormat.format(new Date());

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(dateCreated.getBytes());

            long randomNumber = 0;
            for (int i = 0; i < 8; i++) {
                randomNumber = (randomNumber << 8) | (hash[i] & 0xff);
            }

            return code + dateCreated + String.format("%04d", Math.abs(randomNumber));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }
}
