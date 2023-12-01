package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.controller.ITicketController;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;


public interface CardCheckInHistoryRepositoryCustom {

    Page<ITicketController.CardCheckInHistoryDTO> getAllCardHistoryOfCustomer(Pageable pageable, @Param("checkInCode") @NotNull String checkInCode);
}
