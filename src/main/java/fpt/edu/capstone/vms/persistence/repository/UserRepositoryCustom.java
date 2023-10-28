package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.controller.IUserController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserRepositoryCustom {
    Page<IUserController.UserFilterResponse> filter(Pageable pageable,
                                                    @Param("usernames") @Nullable Collection<String> usernames,
                                                    @Param("roles") @Nullable String role,
                                                    @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                                                    @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                                                    @Param("enable") @Nullable Boolean isEnable,
                                                    @Param("keyword") @Nullable String keyword,
                                                    @Param("departmentId") @Nullable String departmentId);

    List<IUserController.UserFilterResponse> filter(@Param("usernames") @Nullable Collection<String> usernames,
                                                    @Param("roles") @Nullable String role,
                                                    @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                                                    @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                                                    @Param("enable") @Nullable Boolean isEnable,
                                                    @Param("keyword") @Nullable String keyword,
                                                    @Param("departmentId") @Nullable String departmentId);
}
