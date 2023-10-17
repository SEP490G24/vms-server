package fpt.edu.capstone.vms.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IUserResource {
    String create(UserDto account);
    boolean update(UserDto account);
    void changeState(String userId, boolean stateEnable);

    void updateRole(String openId, List<String> roles);

    void delete(String userId);

    void changePassword(String openId, String newPassword);
    List<UserDto> users();

    @Data
    @Accessors(chain = true)
    class UserDto {
        private String orgId;
        private String openid;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        @JsonIgnore
        private String password;
        private String phone;
        private String avatar;
        private String countryCode;
        private LocalDate dateOfBirth;
        private Boolean enable;
        private Constants.Gender gender;
        private Constants.UserRole role;
        private UUID departmentId;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    class RoleDto {
        private String name;
    }
}
