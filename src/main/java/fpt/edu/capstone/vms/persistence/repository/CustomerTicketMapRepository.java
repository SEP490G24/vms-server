package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
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
public interface CustomerTicketMapRepository extends GenericRepository<CustomerTicketMap, CustomerTicketMapPk> {

    List<CustomerTicketMap> findAllByCustomerTicketMapPk_TicketId(UUID ticketId);

    CustomerTicketMap findByCustomerTicketMapPk_TicketIdAndCustomerTicketMapPk_CustomerId(UUID ticketId, UUID customerId);

    @Query(value = "select ctm from CustomerTicketMap ctm " +
        " join Ticket t on ctm.ticketEntity.id = t.id " +
        " join Customer c on ctm.customerEntity.id = c.id " +
        "and ((cast(:roomId as string) is null) or (t.roomId = :roomId)) " +
        "and ((cast(:status as string) is null) or (ctm.status = :status)) " +
        "and ((cast(:purpose as string) is null) or (t.purpose = :purpose)) " +
        "and ((:keyword is null) " +
        "or (c.phoneNumber LIKE %:keyword% " +
        "or c.email LIKE %:keyword% " +
        "or c.visitorName LIKE %:keyword% ))")
    Page<CustomerTicketMap> filter(Pageable pageable,
                                   @Param("roomId") @Nullable UUID roomId,
                                   @Param("status") @Nullable Constants.StatusTicket status,
                                   @Param("purpose") @Nullable Constants.Purpose purpose,
                                   @Param("keyword") @Nullable String keyword);

    @Query(value = "select ctm from CustomerTicketMap ctm " +
        " join Ticket t on ctm.ticketEntity.id = t.id " +
        " join Customer c on ctm.customerEntity.id = c.id " +
        "and ((coalesce(:sites) is null) or (t.siteId in :sites))" +
        "and (((cast(:formCheckInTime as date) is null ) or (cast(:toCheckInTime as date) is null )) or (ctm.checkInTime between :formCheckInTime and :toCheckInTime)) " +
        "and (((cast(:formCheckOutTime as date) is null ) or (cast(:toCheckOutTime as date) is null )) or (ctm.checkOutTime between :formCheckOutTime and :toCheckOutTime)) " +
        "and ((cast(:status as string) is null) or (ctm.status = :status)) " +
        "and ((:keyword is null) " +
        "or (c.phoneNumber LIKE %:keyword% " +
        "or c.email LIKE %:keyword% " +
        "or t.name LIKE %:keyword% " +
        "or t.username LIKE %:keyword% " +
        "or c.identificationNumber LIKE %:keyword% " +
        "or c.visitorName LIKE %:keyword% ))")
    Page<CustomerTicketMap> accessHistory(Pageable pageable,
                                          @Param("sites") @Nullable Collection<String> sites,
                                          @Param("formCheckInTime") @Nullable LocalDateTime formCheckInTime,
                                          @Param("toCheckInTime") @Nullable LocalDateTime toCheckInTime,
                                          @Param("formCheckOutTime") @Nullable LocalDateTime formCheckOutTime,
                                          @Param("toCheckOutTime") @Nullable LocalDateTime toCheckOutTime,
                                          @Param("status") @Nullable Constants.StatusTicket status,
                                          @Param("keyword") @Nullable String keyword);

}
