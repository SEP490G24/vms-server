package fpt.edu.capstone.vms.persistence.repository;


import fpt.edu.capstone.vms.persistence.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends GenericRepository<User, String>, UserRepositoryCustom {

    @Transactional
    @Modifying
    @Query("update User u set u.enable = ?1 where u.username = ?2")
    int updateStateByUsername(@NonNull boolean isEnable, @NonNull String username);

    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsername(@Param("username") @NonNull String username);

    User findFirstByUsername(String username);

    List<User> findAllByEnableIsTrue();

    List<User> findAllByAvatarIsNotNull();

    @Query("select u from User u join Department d on u.departmentId = d.id where d.siteId = :siteId")
    List<User> findAllBySiteId(@Param("siteId") @NonNull UUID siteId);

    @Query("SELECT u FROM User u WHERE u.role LIKE %:role%")
    List<User> findByRole(@Param("role") String role);

    boolean existsByDepartmentId(UUID departmentId);
}
