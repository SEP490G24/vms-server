package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(schema = "vms", name = "setting_site")
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

    @Column(name = "enable")
    private Boolean enable;

    @Column(name = "setting_group_id")
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_group_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private SettingGroup settingGroup;

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "settingSiteMapPk.siteId")
    private Map<UUID, SettingSiteMap> settingSiteMaps;
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
}
