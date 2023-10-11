package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Constants.UserRole role;

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
    private boolean enable;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Constants.Gender gender;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "last_login_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
    private LocalDateTime lastLoginTime;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "departmentUserMapPk.username")
    private Map<String, DepartmentUserMap> departmentUserMaps;

    public User update(User userEntity) {
        if (userEntity.username != null) this.username = userEntity.username;
        if (userEntity.openid != null) this.openid = userEntity.openid;
        if (userEntity.firstName != null) this.firstName = userEntity.firstName;
        if (userEntity.lastName != null) this.lastName = userEntity.lastName;
        if (userEntity.avatar != null) this.avatar = userEntity.avatar;
        if (userEntity.email != null) this.email = userEntity.email;
        if (userEntity.phoneNumber != null) this.phoneNumber = userEntity.phoneNumber;
        if (userEntity.lastLoginTime != null) this.lastLoginTime = userEntity.lastLoginTime;
        if (userEntity.gender != null) this.gender = userEntity.gender;
        if (userEntity.dateOfBirth != null) this.dateOfBirth = userEntity.dateOfBirth;
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
}
