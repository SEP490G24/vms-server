package fpt.edu.capstone.vms.persistence.entity;

import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Map;
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

    @OneToMany(mappedBy = "permissionEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "permissionRoleMapPk.permissionId")
    private Map<UUID, PermissionRoleMap> permissionRoleMaps;

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
