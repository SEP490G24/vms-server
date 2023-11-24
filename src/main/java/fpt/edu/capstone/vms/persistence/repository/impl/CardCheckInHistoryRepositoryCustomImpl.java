package fpt.edu.capstone.vms.persistence.repository.impl;

import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.repository.CardCheckInHistoryRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class CardCheckInHistoryRepositoryCustomImpl implements CardCheckInHistoryRepositoryCustom {

    final EntityManager entityManager;

    @Override
    public List<ITicketController.CardCheckInHistoryDTO> getAllCardHistoryOfCustomer(String checkInCode) {
        Map<String, Object> queryParams = new HashMap<>();
        String orderByClause = "";
        String sqlGetData = "SELECT u.id, u.check_in_code, u.mac_ip, u.status," +
            " u.created_on, c.name ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM card_check_in_history u ");
        sqlConditional.append("LEFT JOIN room_site c ON c.mac_ip = u.mac_ip ");
        sqlConditional.append("WHERE 1=1 ");

        sqlConditional.append("AND u.check_in_code = :checkInCode ");
        queryParams.put("checkInCode", checkInCode.toUpperCase());


        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional + orderByClause);
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<ITicketController.CardCheckInHistoryDTO> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            ITicketController.CardCheckInHistoryDTO cardCheckInHistoryDTO = new ITicketController.CardCheckInHistoryDTO();
            cardCheckInHistoryDTO.setId((Integer) object[0]);
            cardCheckInHistoryDTO.setCheckInCode((String) object[1]);
            cardCheckInHistoryDTO.setMacIp((String) object[2]);
            cardCheckInHistoryDTO.setStatus((String) object[3]);
            cardCheckInHistoryDTO.setCreatedOn((String) object[4]);
            cardCheckInHistoryDTO.setRoomName((String) object[5]);
            listData.add(cardCheckInHistoryDTO);
        }
        return listData;
    }
}