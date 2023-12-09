package fpt.edu.capstone.vms.persistence.entity;

import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
            '}';
    }
}
