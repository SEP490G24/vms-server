package fpt.edu.capstone.vms.oauth2;

import fpt.edu.capstone.vms.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.rmi.server.UID;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IUserResource {
    String create(UserDto account);
    boolean update(UserDto account);
    void changeState(String userId, boolean stateEnable);
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
        private String password;
        private String phone;
        private String avatar;
        private String countryCode;
        private LocalDate dateOfBirth;
        private Boolean enable = true;
        private Constants.Gender gender;
        private Constants.UserRole role;
        private String departmentId;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    class RoleDto {
        private String name;
    }
}
