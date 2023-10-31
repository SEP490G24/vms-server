package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Card;
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
public interface CardRepository extends GenericRepository<Card, UUID> {

    @Query(value = "select u from Card u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:fromDate as date) is null ) or (cast(:toDate as date) is null )) or (u.createdOn between :fromDate and :toDate)) " +
        "and ((cast(:siteId as string) is null) or (u.siteId = :siteId)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    Page<Card> filter(Pageable pageable,
                      @Param("names") @Nullable Collection<String> names,
                      @Param("siteId") @Nullable UUID siteId,
                      @Param("fromDate") @Nullable LocalDateTime fromDate,
                      @Param("toDate") @Nullable LocalDateTime toDate,
                      @Param("keyword") @Nullable String keyword);

    @Query(value = "select u from Card u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:fromDate as date) is null ) or (cast(:toDate as date) is null )) or (u.createdOn between :fromDate and :toDate)) " +
        "and ((cast(:siteId as string) is null) or (u.siteId = :siteId)) " +
        "and ((:keyword is null) " +
        "or ( UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    List<Card> filter(
        @Param("names") @Nullable Collection<String> names,
        @Param("siteId") @Nullable UUID siteId,
        @Param("fromDate") @Nullable LocalDateTime fromDate,
        @Param("toDate") @Nullable LocalDateTime toDate,
        @Param("keyword") @Nullable String keyword);

    List<Card> findAllBySiteId(UUID siteId);
}
