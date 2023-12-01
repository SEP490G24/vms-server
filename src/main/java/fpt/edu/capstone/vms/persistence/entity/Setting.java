package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
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
@Table(schema = "vms", name = "setting")
@EqualsAndHashCode(callSuper = true)
public class Setting extends AbstractBaseEntity<Long> {

    @Id
    @Column(name = "id", columnDefinition = "int", updatable = false, nullable = false)
    @GeneratedValue (strategy = GenerationType. IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, updatable = false, nullable = false, length = 100)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Constants.SettingType type;

    @Column(name = "default_property_value", length = 500)
    private String defaultValue;

    @Column(name = "value_list", length = 500)
    private String valueList;

    @Column(name = "enable")
    private Boolean enable;

    @Column(name = "setting_group_id")
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_group_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private SettingGroup settingGroup;

    public Setting update(Setting settingEntity) {
        if (settingEntity.name != null) this.name = settingEntity.name;
        if (settingEntity.code != null) this.code = settingEntity.code;
        if (settingEntity.description != null) this.description = settingEntity.description;
        if (settingEntity.groupId != null) this.groupId = settingEntity.groupId;
        if (settingEntity.enable != null) this.enable = settingEntity.enable;
        if (settingEntity.type != null) this.type = settingEntity.type;
        if (settingEntity.defaultValue != null) this.defaultValue = settingEntity.defaultValue;
        if (settingEntity.getCreatedBy() != null) this.setCreatedBy(settingEntity.getCreatedBy());
        if (settingEntity.getCreatedOn() != null) this.setCreatedOn(settingEntity.getCreatedOn());
        return this;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Setting{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", type=" + type +
            ", defaultValue='" + defaultValue + '\'' +
            ", valueList='" + valueList + '\'' +
            ", enable=" + enable +
            ", groupId=" + groupId +
            '}';
    }
}
