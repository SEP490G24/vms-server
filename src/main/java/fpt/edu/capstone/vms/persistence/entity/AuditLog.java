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

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}
