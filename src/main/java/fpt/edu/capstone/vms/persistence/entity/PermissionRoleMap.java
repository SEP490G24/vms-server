package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "permission_role_map")
public class PermissionRoleMap extends AbstractBaseEntity<PermissionRoleMapPk> {

    @EmbeddedId
    private PermissionRoleMapPk permissionRoleMapPk;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Role roleEntity;

    @ManyToOne
    @JoinColumn(name = "permission_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Permission permissionEntity;


    @Column(name = "status")
    private Boolean status;
    @Override
    public void setId(PermissionRoleMapPk id) {
        this.permissionRoleMapPk = id;
    }

    @Override
    public PermissionRoleMapPk getId() {
        return this.permissionRoleMapPk;
    }
}
