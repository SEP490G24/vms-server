package fpt.edu.capstone.vms.persistence.entity;

import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "audit_log")
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "site_id")
    private String siteId;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "primary_key")
    private String primaryKey;

    @Column(name = "table_name")
    private String tableName;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_type")
    private Constants.AuditType auditType;

    @Column(name = "old_value", length = 10000)
    private String oldValue;

    @Column(name = "new_value", length = 10000)
    private String newValue;

    public AuditLog(String siteId, String organizationId, String primaryKey, String tableName, Constants.AuditType auditType, String oldValue, String newValue) {
        this.code = generateAuditLogCode(auditType);
        this.siteId = siteId;
        this.organizationId = organizationId;
        this.primaryKey = primaryKey;
        this.tableName = tableName;
        this.auditType = auditType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
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
