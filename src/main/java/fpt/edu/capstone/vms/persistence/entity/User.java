package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "user")
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractBaseEntity<String> {

    @Id
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "openid")
    private String openid;

    @Column(name = "role")
    private String role;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "enable")
    private Boolean enable;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Constants.Gender gender;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "last_login_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
    private LocalDateTime lastLoginTime;

    @Column(name = "country_code")
    private String countryCode;

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

    @Column(name = "department_id")
    private UUID departmentId;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Department department;

    public User update(User userEntity) {
        if (userEntity.openid != null) this.openid = userEntity.openid;
        if (userEntity.firstName != null) this.firstName = userEntity.firstName;
        if (userEntity.lastName != null) this.lastName = userEntity.lastName;
        if (userEntity.email != null) this.email = userEntity.email;
        if (userEntity.phoneNumber != null) this.phoneNumber = userEntity.phoneNumber;
        if (userEntity.lastLoginTime != null) this.lastLoginTime = userEntity.lastLoginTime;
        if (userEntity.gender != null) this.gender = userEntity.gender;
        if (userEntity.dateOfBirth != null) this.dateOfBirth = userEntity.dateOfBirth;
        if (userEntity.countryCode != null) this.countryCode = userEntity.countryCode;
        if (userEntity.departmentId != null) this.departmentId = userEntity.departmentId;
        if (userEntity.enable != null) this.enable = userEntity.enable;
        if (userEntity.provinceId != null) this.provinceId = userEntity.provinceId;
        if (userEntity.communeId != null) this.communeId = userEntity.communeId;
        if (userEntity.districtId != null) this.districtId = userEntity.districtId;
        if (userEntity.address != null) this.address = userEntity.address;
        if (userEntity.getCreatedBy() != null) this.setCreatedBy(userEntity.getCreatedBy());
        if (userEntity.getCreatedOn() != null) this.setCreatedOn(userEntity.getCreatedOn());
        return this;
    }

    @Override
    public void setId(String id) {
        username = id;
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
            "username='" + username + '\'' +
            ", role='" + role + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", avatar='" + avatar + '\'' +
            ", email='" + email + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", enable=" + enable +
            ", gender=" + gender +
            ", dateOfBirth=" + dateOfBirth +
            ", provinceId=" + provinceId +
            ", districtId=" + districtId +
            ", communeId=" + communeId +
            ", address='" + address + '\'' +
            ", departmentId=" + departmentId +
            '}';
    }
}
