package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "setting_group")
@EqualsAndHashCode(callSuper = true)
public class SettingGroup extends AbstractBaseEntity<Long> {

    @Id
    @Column(name = "id", columnDefinition = "int", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public SettingGroup update(SettingGroup settingGroupEntity) {
        if (settingGroupEntity.name != null) this.name = settingGroupEntity.name;
        if (settingGroupEntity.getCreatedBy() != null) this.setCreatedBy(settingGroupEntity.getCreatedBy());
        if (settingGroupEntity.getCreatedOn() != null) this.setCreatedOn(settingGroupEntity.getCreatedOn());
        return this;
    }


}
