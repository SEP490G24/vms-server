package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

    @Column(name = "mandatory_health")
    private Boolean mandatoryHealth;

    @Column(name = "mandatory_trip_code")
    private Boolean mandatoryTripCode;

    @Column(name = "nucleic_acid_test_report")
    private Boolean nucleicAcidTestReport;

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
    @MapKey(name = "siteDepartmentMapPk.siteId")
    private Map<UUID, SiteDepartmentMap> siteDepartmentMaps;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}
