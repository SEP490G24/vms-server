package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PermissionRoleMapPk implements Serializable {

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "role_id")
    private UUID roleId;
}
