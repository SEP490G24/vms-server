package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.controller.ITicketController;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CardCheckInHistoryRepositoryCustom {

    List<ITicketController.CardCheckInHistoryDTO> getAllCardHistoryOfCustomer(@Param("checkInCode") @NotNull String checkInCode);
}
