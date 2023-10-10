package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "department")
@EqualsAndHashCode(callSuper = true)
public class Department extends AbstractBaseEntity<UUID>{

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

    @OneToMany(mappedBy = "departmentEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "siteDepartmentMapPk.departmentId")
    private Map<UUID, SiteDepartmentMap> siteDepartmentMaps;

    @OneToMany(mappedBy = "departmentEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "departmentUserMapPk.departmentId")
    private Map<UUID, DepartmentUserMap> departmentUserMaps;

    public Department update(Department department) {
        if (department.name != null) this.name = department.name;
        if (department.code != null) this.code = department.code;
        if (department.description != null) this.description = department.description;
        if (department.enable != null) this.enable = department.enable;
        if (department.getCreatedBy() != null) this.setCreatedBy(department.getCreatedBy());
        if (department.getCreatedOn() != null) this.setCreatedOn(department.getCreatedOn());
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
