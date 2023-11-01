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

    List<Customer> findAllByOrganizationId(String organizationId);

    boolean existsByIdAndAndOrganizationId(UUID id, String organizationId);

    @Query(value = "select u from Customer u " +
        "where ((coalesce(:names) is null) or (u.visitorName in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:identificationNumber is null) or (u.identificationNumber in :identificationNumber)) " +
        "and ((:orgId is null) or (u.organizationId = :orgId)) " +
        "and ((:keyword is null) " +
        "or (u.visitorName LIKE %:keyword% " +
        "or u.phoneNumber LIKE %:keyword% " +
        "or u.email LIKE %:keyword% " +
        "or u.identificationNumber LIKE %:keyword% ))")
    List<Customer> filter(
        @Param("names") @Nullable Collection<String> names,
        @Param("orgId") @Nullable String orgId,
        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
        @Param("createBy") @Nullable String createBy,
        @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
        @Param("identificationNumber") @Nullable String identificationNumber,
        @Param("keyword") @Nullable String keyword);

    @Query(value = "select u from Customer u " +
        "where ((coalesce(:names) is null) or (u.visitorName in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:identificationNumber is null) or (u.identificationNumber in :identificationNumber)) " +
        "and ((:orgId is null) or (u.organizationId = :orgId)) " +
        "and ((:keyword is null) " +
        "or (u.phoneNumber LIKE %:keyword% " +
        "or u.visitorName LIKE %:keyword% " +
        "or u.email LIKE %:keyword% " +
        "or u.identificationNumber LIKE %:keyword% ))")
    Page<Customer> filter(Pageable pageable,
                          @Param("names") @Nullable Collection<String> names,
                          @Param("orgId") @Nullable String orgId,
                          @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                          @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                          @Param("createBy") @Nullable String createBy,
                          @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
                          @Param("identificationNumber") @Nullable String identificationNumber,
                          @Param("keyword") @Nullable String keyword);
}
