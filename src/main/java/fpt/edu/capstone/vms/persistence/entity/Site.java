package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "site")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Site extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "organization_id")
    private UUID organizationId;

    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Organization organization;

    @Column(name = "phone_number")
    @Min(value = 0)
    @Max(value = 10)
    private String phoneNumber;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "ward")
    private String ward;

    @Column(name = "address")
    private String address;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "description")
    private String description;

    @Column(name = "enable")
    private Boolean enable;

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "templateSiteMapPk.siteId")
    private Map<UUID, TemplateSiteMap> templateSiteMaps;

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "pricePackageSiteMapPk.siteId")
    private Map<UUID, PricePackageSiteMap> pricePackageSiteMaps;

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "settingSiteMapPk.siteId")
    private Map<UUID, SettingSiteMap> settingSiteMaps;

    public Site update(Site siteEntity) {
        if (siteEntity.name != null) this.name = siteEntity.name;
        if (siteEntity.code != null) this.code = siteEntity.code;
        if (siteEntity.organizationId != null) this.organizationId = siteEntity.organizationId;
        if (siteEntity.phoneNumber != null) this.phoneNumber = siteEntity.phoneNumber;
        if (siteEntity.province != null) this.province = siteEntity.province;
        if (siteEntity.ward != null) this.ward = siteEntity.ward;
        if (siteEntity.district != null) this.district = siteEntity.district;
        if (siteEntity.address != null) this.address = siteEntity.address;
        if (siteEntity.taxCode != null) this.taxCode = siteEntity.taxCode;
        if (siteEntity.description != null) this.description = siteEntity.description;
        if (siteEntity.enable != null) this.enable = siteEntity.enable;
        if (siteEntity.getCreatedBy() != null) this.setCreatedBy(siteEntity.getCreatedBy());
        if (siteEntity.getCreatedOn() != null) this.setCreatedOn(siteEntity.getCreatedOn());
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
