package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.crypto.bcrypt.BCrypt;

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

    @Column(name = "password")
    @JsonIgnore
    private String password;

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

    @Column(name = "department_id")
    private UUID departmentId;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Department department;

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
        if (userEntity.password != null) this.password = encodePassword(userEntity.password);
        if (userEntity.countryCode != null) this.countryCode = userEntity.countryCode;
        if (userEntity.departmentId != null) this.departmentId = userEntity.departmentId;
        if (userEntity.enable != null) this.enable = userEntity.enable;
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

    public static String encodePassword(String plainPassword) {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(plainPassword, salt);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
