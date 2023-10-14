package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Collection;

public interface UserRepositoryCustom {
    Page<IUserController.UserFilter> filter(Pageable pageable,
                                            @Param("usernames") @Nullable Collection<String> usernames,
                                            @Param("roles") @Nullable Collection<Constants.UserRole> roles,
                                            @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                                            @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                                            @Param("enable") @Nullable Boolean isEnable,
                                            @Param("keyword") @Nullable String keyword,
                                            @Param("departmentId") @Nullable String departmentId);
}
