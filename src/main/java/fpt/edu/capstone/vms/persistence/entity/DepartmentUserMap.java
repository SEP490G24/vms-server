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
@Table(schema = "vms", name = "department_user_map")
public class DepartmentUserMap extends AbstractBaseEntity<DepartmentUserMapPk> {

    @EmbeddedId
    private DepartmentUserMapPk departmentUserMapPk;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Department departmentEntity;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    @JsonIgnore
    private User userEntity;

    @Override
    public void setId(DepartmentUserMapPk id) {
        this.departmentUserMapPk = id;
    }

    @Override
    public DepartmentUserMapPk getId() {
        return this.departmentUserMapPk;
    }
}
