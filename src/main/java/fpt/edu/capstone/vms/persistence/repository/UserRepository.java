package fpt.edu.capstone.vms.persistence.repository;


import fpt.edu.capstone.vms.constants.Constants;
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
public interface UserRepository extends JpaRepository<User, String> {

    @Transactional
    @Modifying
    @Query("update User u set u.state = ?1 where u.username = ?2")
    int updateStateByUsername(@NonNull Constants.UserState state, @NonNull String username);

    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsername(@Param("username") @NonNull String username);

    @Query(value = "select u from User u " +
            "where ((coalesce(:usernames) is null) or (u.username in :usernames)) " +
            "and ((coalesce(:roles) is null) or (u.role in :roles)) " +
            "and ((:createdOnStart is null or :createdOnEnd is null) or (u.createdOn between :createdOnStart and :createdOnEnd))" +
            "and ((:state is null) or (u.state = :state))")
    Page<User> filter(Pageable pageable,
                      @Param("usernames") @Nullable Collection<String> usernames,
                      @Param("roles") @Nullable Collection<Constants.UserRole> roles,
                      @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                      @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                      @Param("state") @Nullable Constants.UserState state);

    List<User> findByState(Constants.UserState userState);

    List<User> findByStateAndUsernameIn(Constants.UserState userState, List<String> usernames);

    User findFirstByUsername(String username);
}
