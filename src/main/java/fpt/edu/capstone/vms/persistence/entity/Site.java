package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(schema = "vms", name = "site")
@EqualsAndHashCode(callSuper = true)
public class Site extends AbstractBaseEntity implements ModelBaseInterface<String> {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "organization_id")
    private String organizationId;

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

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL)
    @MapKey(name = "siteDepartmentMapPk.siteId")
    private Map<String, SiteDepartmentMap> siteDepartmentMaps;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
