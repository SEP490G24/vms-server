package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "user_role_map")
public class UserRoleMap extends AbstractBaseEntity<UserRoleMapPk> {

    @EmbeddedId
    private UserRoleMapPk userRoleMapPk;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Role roleEntity;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    @JsonIgnore
    private User userEntity;

    public UserRoleMap update(UserRoleMap userRoleMap) {
        if (userRoleMap.getCreatedBy() != null) this.setCreatedBy(userRoleMap.getCreatedBy());
        if (userRoleMap.getCreatedOn() != null) this.setCreatedOn(userRoleMap.getCreatedOn());
        return this;
    }

    @Override
    public void setId(UserRoleMapPk id) {
        this.userRoleMapPk = id;
    }

    @Override
    public UserRoleMapPk getId() {
        return this.userRoleMapPk;
    }
}
