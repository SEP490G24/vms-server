package fpt.edu.capstone.vms.persistence.entity;

import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "permission")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "enable")
    private Boolean enable;

    @Column(name = "module")
    private String module;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type")
    private Constants.PermissionType permissionType;

    @Column(name = "scope")
    private String scope;

    public Permission update(Permission permissionEntity) {
        if (permissionEntity.name != null) this.name = permissionEntity.name;
        if (permissionEntity.code != null) this.code = permissionEntity.code;
        if (permissionEntity.description != null) this.description = permissionEntity.description;
        if (permissionEntity.enable != null) this.enable = permissionEntity.enable;
        if (permissionEntity.module != null) this.module = permissionEntity.module;
        if (permissionEntity.scope != null) this.scope = permissionEntity.scope;
        if (permissionEntity.permissionType != null) this.permissionType = permissionEntity.permissionType;
        if (permissionEntity.getCreatedBy() != null) this.setCreatedBy(permissionEntity.getCreatedBy());
        if (permissionEntity.getCreatedOn() != null) this.setCreatedOn(permissionEntity.getCreatedOn());
        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}
