package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "setting_site")
@EqualsAndHashCode(callSuper = true)
public class SettingSite extends AbstractBaseEntity<Long> {

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

    @Column(name = "type")
    private String type;

    @Column(name = "property_value", length = 500)
    private String value;

    @Column(name = "default_property_value", length = 500)
    private String defaultValue;

    @Column(name = "site_id")
    private UUID siteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Site site;

    @Column(name = "setting_group_id")
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_group_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private SettingGroup settingGroup;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
