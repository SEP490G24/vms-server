package fpt.edu.capstone.vms.persistence.repository.impl;

import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.repository.CardCheckInHistoryRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class CardCheckInHistoryRepositoryCustomImpl implements CardCheckInHistoryRepositoryCustom {

    final EntityManager entityManager;

    @Override
    public Page<ITicketController.CardCheckInHistoryDTO> getAllCardHistoryOfCustomer(Pageable pageable, String checkInCode) {
        Map<String, Object> queryParams = new HashMap<>();
        Sort sort = pageable.getSort();
        String orderByClause = "";
        if (sort.isSorted()) {
            orderByClause = "ORDER BY ";
            for (Sort.Order order : sort) {
                orderByClause += order.getProperty() + " " + order.getDirection() + ", ";
            }
            orderByClause = orderByClause.substring(0, orderByClause.length() - 2);
        }
        String sqlCountAll = "SELECT COUNT(1) ";
        String sqlGetData = "SELECT u.id, u.check_in_code, u.mac_ip, u.status," +
            " u.created_on, c.name ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM card_check_in_history u ");
        sqlConditional.append("LEFT JOIN device d ON d.mac_ip = u.mac_ip ");
        sqlConditional.append("LEFT JOIN room_site c ON c.device_id = d.id ");
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
            cardCheckInHistoryDTO.setCreatedOn((Date) object[4]);
            cardCheckInHistoryDTO.setRoomName((String) object[5]);
            listData.add(cardCheckInHistoryDTO);
        }
        Query queryCountAll = entityManager.createNativeQuery(sqlCountAll + sqlConditional);
        queryParams.forEach(queryCountAll::setParameter);
        int countAll = ((Number) queryCountAll.getSingleResult()).intValue();
        return new PageImpl<>(listData, pageable, countAll);
    }
}
