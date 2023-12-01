package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(schema = "vms", name = "customer")
@EqualsAndHashCode(callSuper = true)
public class Customer extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "visitor_name")
    private String visitorName;

    @Min(value = 1)
    @Max(value = 12)
    @Column(name = "identification_number", unique = true, nullable = false)
    private String identificationNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Constants.Gender gender;

    @Column(name = "description")
    private String description;

    @Column(name = "organization_id")
    private String organizationId;

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

    public Customer(UUID id, String visitorName, String identificationNumber, String email, String phoneNumber, Constants.Gender gender, String description, String organizationId, Integer provinceId, Integer districtId, Integer communeId) {
        this.id = id;
        this.visitorName = visitorName;
        this.identificationNumber = identificationNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.description = description;
        this.organizationId = organizationId;
        this.provinceId = provinceId;
        this.districtId = districtId;
        this.communeId = communeId;
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
        return "Customer{" +
            "id=" + id +
            ", visitorName='" + visitorName + '\'' +
            ", identificationNumber='" + identificationNumber + '\'' +
            ", email='" + email + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", gender=" + gender +
            ", description='" + description + '\'' +
            ", organizationId='" + organizationId + '\'' +
            ", provinceId=" + provinceId +
            ", districtId=" + districtId +
            ", communeId=" + communeId +
            '}';
    }
}
