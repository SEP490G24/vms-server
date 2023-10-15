package fpt.edu.capstone.vms.persistence.repository;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>,UserRepositoryCustom {

    @Transactional
    @Modifying
    @Query("update User u set u.enable = ?1 where u.username = ?2")
    int updateStateByUsername(@NonNull boolean isEnable, @NonNull String username);

    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsername(@Param("username") @NonNull String username);

    /*@Query(value = "select u from User u " +
        "where ((coalesce(:usernames) is null) or (u.username in :usernames)) " +
        "and ((coalesce(:roles) is null) or (u.role in :roles)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd))" +
        "and ((:enable is null) or (u.enable = :enable))" +
        "and ((:keyword is null) or (u.username LIKE %:keyword% or u.firstName LIKE %:keyword% or u.lastName LIKE %:keyword% or u.email LIKE %:keyword% or u.phoneNumber LIKE %:keyword% ))")
    Page<User> filter(Pageable pageable,
                                                @Param("usernames") @Nullable Collection<String> usernames,
                                                @Param("roles") @Nullable Collection<Constants.UserRole> roles,
                                                @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                                                @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                                                @Param("enable") @Nullable Boolean isEnable,
                                                @Param("keyword") @Nullable String keyword,
                                                @Param("departmentId") @Nullable String departmentId);*/

    User findFirstByUsername(String username);

    List<User> findAllByEnableIsTrue();
}
