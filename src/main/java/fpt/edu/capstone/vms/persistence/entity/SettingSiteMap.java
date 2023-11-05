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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "setting_site_map")
public class SettingSiteMap extends AbstractBaseEntity<SettingSiteMapPk> {

    @EmbeddedId
    private SettingSiteMapPk settingSiteMapPk;

    @ManyToOne
    @JoinColumn(name = "setting_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Setting settingEntity;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site siteEntity;

    @Column(name = "description")
    private String description;

    @Column(name = "property_value", length = 500)
    private String value;

    @Column(name = "status")
    private Boolean status;

    public SettingSiteMap update(SettingSiteMap settingSiteMap) {
        if (settingSiteMap.description != null) this.description = settingSiteMap.description;
        if (settingSiteMap.value != null) this.value = settingSiteMap.value;
        if (settingSiteMap.status != null) this.status = settingSiteMap.status;
        if (settingSiteMap.getCreatedBy() != null) this.setCreatedBy(settingSiteMap.getCreatedBy());
        if (settingSiteMap.getCreatedOn() != null) this.setCreatedOn(settingSiteMap.getCreatedOn());
        return this;
    }
    @Override
    public void setId(SettingSiteMapPk id) {
        this.settingSiteMapPk = id;
    }

    @Override
    public SettingSiteMapPk getId() {
        return this.settingSiteMapPk;
    }
}
