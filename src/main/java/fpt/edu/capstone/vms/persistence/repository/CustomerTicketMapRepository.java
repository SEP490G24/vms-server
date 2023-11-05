package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface CustomerTicketMapRepository extends GenericRepository<CustomerTicketMap, CustomerTicketMapPk> {

    List<CustomerTicketMap> findAllByCustomerTicketMapPk_TicketId(UUID ticketId);

    CustomerTicketMap findByCustomerTicketMapPk_TicketIdAndCustomerTicketMapPk_CustomerId(UUID ticketId, UUID customerId);

//    @Query(value = "select ctm from CustomerTicketMap ctm " +
//        "left join Ticket t on ctm.ticketEntity.id = t.id " +
//        "left join Customer c on ctm.customerEntity.id = c.id " +
//        "where ((cast(:roomId as string) is null) or (u.roomId = :roomId)) " +
//        "and ((cast(:status as string) is null) or (u.status = :status)) " +
//        "and ((cast(:purpose as string) is null) or (u.purpose = :purpose)) " +
//        "and ((:keyword is null) " +
//        "or (c.visitorName LIKE %:keyword% " +
//        "or c.phoneNumber LIKE %:keyword% " +
//        "or c.email LIKE %:keyword% ))")
//    Page<CustomerTicketMap> filter1(Pageable pageable,
//                        @Param("names") @Nullable Collection<String> names,
//                        @Param("sites") @Nullable Collection<String> sites,
//                        @Param("username") @Nullable String username,
//                        @Param("roomId") @Nullable UUID roomId,
//                        @Param("status") @Nullable Constants.StatusTicket status,
//                        @Param("purpose") @Nullable Constants.Purpose purpose,
//                        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
//                        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
//                        @Param("startTimeStart") @Nullable LocalDateTime startTimeStart,
//                        @Param("startTimeEnd") @Nullable LocalDateTime startTimeEnd,
//                        @Param("endTimeStart") @Nullable LocalDateTime endTimeStart,
//                        @Param("endTimeEnd") @Nullable LocalDateTime endTimeEnd,
//                        @Param("createdBy") @Nullable String createdBy,
//                        @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
//                        @Param("bookmark") @Nullable Boolean bookmark,
//                        @Param("keyword") @Nullable String keyword);

}
