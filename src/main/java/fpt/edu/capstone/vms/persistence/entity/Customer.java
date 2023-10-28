package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private String visitor_name;

    @Column(name = "code")
    private String code;

    @Min(value = 1)
    @Max(value = 12)
    @Column(name = "identification_number")
    private String identificationNumber;

    @Column(name = "license_plate_number", length = 64)
    private String licensePlateNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Constants.Gender gender;

    @Column(name = "description")
    private String description;

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

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @ManyToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Ticket ticket;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}
