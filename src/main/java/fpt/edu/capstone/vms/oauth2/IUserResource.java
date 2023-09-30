package fpt.edu.capstone.vms.oauth2;

import fpt.edu.capstone.vms.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

public interface IUserResource {
    String create(UserDto account);
    boolean update(UserDto account);
    void changeState(String userId, boolean stateEnable);
    void delete(String userId);

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
        private Boolean isEnable;
        private Constants.UserRole role;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    class RoleDto {
        private String name;
    }
}
