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

}
