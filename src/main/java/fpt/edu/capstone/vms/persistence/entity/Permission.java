package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "site")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends AbstractBaseEntity<UUID> {

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

//    @Column(name = "site_id")
//    private UUID siteId;

//    @ManyToOne
//    @JoinColumn(name = "organization_id", referencedColumnName = "id", insertable = false, updatable = false)
//    @JsonIgnore
//    private Organization organization;

//    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
//    @MapKey(name = "templateSiteMapPk.siteId")
//    private Map<UUID, TemplateSiteMap> templateSiteMaps;

//    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
//    @MapKey(name = "pricePackageSiteMapPk.siteId")
//    private Map<UUID, PricePackageSiteMap> pricePackageSiteMaps;

//    public Site update(Site siteEntity) {
//        if (siteEntity.name != null) this.name = siteEntity.name;
//        if (siteEntity.code != null) this.code = siteEntity.code;
//        if (siteEntity.organizationId != null) this.organizationId = siteEntity.organizationId;
//        if (siteEntity.phoneNumber != null) this.phoneNumber = siteEntity.phoneNumber;
//        if (siteEntity.province != null) this.province = siteEntity.province;
//        if (siteEntity.ward != null) this.ward = siteEntity.ward;
//        if (siteEntity.district != null) this.district = siteEntity.district;
//        if (siteEntity.address != null) this.address = siteEntity.address;
//        if (siteEntity.taxCode != null) this.taxCode = siteEntity.taxCode;
//        if (siteEntity.description != null) this.description = siteEntity.description;
//        if (siteEntity.enable != null) this.enable = siteEntity.enable;
//        if (siteEntity.getCreatedBy() != null) this.setCreatedBy(siteEntity.getCreatedBy());
//        if (siteEntity.getCreatedOn() != null) this.setCreatedOn(siteEntity.getCreatedOn());
//        return this;
//    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}
