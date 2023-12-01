package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Builder
public class Site extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", updatable = false, nullable = false)
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

    @Column(name = "province_id")
    private Integer provinceId;

    @ManyToOne
    @JoinColumn(name = "province_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Province province;

    @Column(name = "district_id")
    private Integer districtId;

    @ManyToOne
    @JoinColumn(name = "district_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private District district;

    @Column(name = "commune_id")
    private Integer communeId;

    @ManyToOne
    @JoinColumn(name = "commune_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Commune commune;

    @Column(name = "address")
    private String address;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "description")
    private String description;

    @Column(name = "enable")
    private Boolean enable;

    public Site update(Site siteEntity) {
        if (siteEntity != null) {
            if (siteEntity.name != null) this.name = siteEntity.name;
            if (siteEntity.organizationId != null) this.organizationId = siteEntity.organizationId;
            if (siteEntity.phoneNumber != null) this.phoneNumber = siteEntity.phoneNumber;
            if (siteEntity.provinceId != null) this.provinceId = siteEntity.provinceId;
            if (siteEntity.communeId != null) this.communeId = siteEntity.communeId;
            if (siteEntity.districtId != null) this.districtId = siteEntity.districtId;
            if (siteEntity.address != null) this.address = siteEntity.address;
            if (siteEntity.taxCode != null) this.taxCode = siteEntity.taxCode;
            if (siteEntity.description != null) this.description = siteEntity.description;
            if (siteEntity.enable != null) this.enable = siteEntity.enable;
            if (siteEntity.getCreatedBy() != null) this.setCreatedBy(siteEntity.getCreatedBy());
            if (siteEntity.getCreatedOn() != null) this.setCreatedOn(siteEntity.getCreatedOn());
        }
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

    @Override
    public String toString() {
        return "Site{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", code='" + code + '\'' +
            ", organizationId=" + organizationId +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", provinceId=" + provinceId +
            ", districtId=" + districtId +
            ", communeId=" + communeId +
            ", address='" + address + '\'' +
            ", taxCode='" + taxCode + '\'' +
            ", description='" + description + '\'' +
            ", enable=" + enable +
            '}';
    }
}
