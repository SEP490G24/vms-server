package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends GenericRepository<Customer, UUID> {
    Customer findByIdentificationNumberAndOrganizationId(String identificationNumber, String organizationId);

    @Query(value = "SELECT u FROM Customer u " +
        "WHERE u.organizationId = :organizationId AND u.id NOT IN (" +
        "SELECT cu.id FROM Customer cu " +
        "INNER JOIN CustomerTicketMap c ON cu.id = c.customerTicketMapPk.customerId " +
        "INNER JOIN Ticket t ON t.id = c.customerTicketMapPk.ticketId " +
        "WHERE cu.organizationId = :organizationId AND t.startTime < :endTime AND t.endTime > :startTime)")
    List<Customer> findAllByOrganizationId(String organizationId, LocalDateTime startTime, LocalDateTime endTime);

    boolean existsByIdAndAndOrganizationId(UUID id, String organizationId);

    boolean existsByEmailAndOrganizationId(String email, String organizationId);

    boolean existsByPhoneNumberAndOrganizationId(String phoneNumber, String organizationId);

    boolean existsByIdentificationNumberAndOrganizationId(String identificationNumber, String organizationId);

    @Query(value = "select u from Customer u " +
        "where ((coalesce(:names) is null) or (u.visitorName in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
        "and ((:organizationId is null) or (u.organizationId in :organizationId)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:identificationNumber is null) or (u.identificationNumber in :identificationNumber)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.visitorName) LIKE %:keyword% " +
        "or UPPER(u.phoneNumber) LIKE %:keyword% " +
        "or UPPER(u.email) LIKE %:keyword% " +
        "or UPPER(u.identificationNumber) LIKE %:keyword% ))")
    List<Customer> filter(
        @Param("names") @Nullable Collection<String> names,
        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
        @Param("createBy") @Nullable String createBy,
        @Param("organizationId") @Nullable String organizationId,
        @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
        @Param("identificationNumber") @Nullable String identificationNumber,
        @Param("keyword") @Nullable String keyword);

    @Query(value = "select u from Customer u " +
        "where ((coalesce(:names) is null) or (u.visitorName in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
        "and ((:organizationId is null) or (u.organizationId in :organizationId)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:identificationNumber is null) or (u.identificationNumber in :identificationNumber)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.phoneNumber) LIKE %:keyword% " +
        "or UPPER(u.visitorName) LIKE %:keyword% " +
        "or UPPER(u.lastUpdatedBy) LIKE %:keyword% " +
        "or UPPER(u.createdBy) LIKE %:keyword% " +
        "or UPPER(u.email) LIKE %:keyword% " +
        "or UPPER(u.identificationNumber) LIKE %:keyword% ))")
    Page<Customer> filter(Pageable pageable,
                          @Param("names") @Nullable Collection<String> names,
                          @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                          @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                          @Param("createBy") @Nullable String createBy,
                          @Param("organizationId") @Nullable String organizationId,
                          @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
                          @Param("identificationNumber") @Nullable String identificationNumber,
                          @Param("keyword") @Nullable String keyword);
}
