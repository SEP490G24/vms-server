package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.persistence.entity.Card;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.UUID;


public interface ICardService extends IGenericService<Card, UUID> {

    Card create(ICardController.CardDto cardDto);
//
//    Page<Card> filter(Pageable pageable,
//                      List<String> names,
//                      UUID siteId,
//                      LocalDateTime fromDate,
//                      LocalDateTime toDate,
//                      String keyword);
//
//    List<Card> filter(
//        List<String> names,
//        UUID siteId,
//        LocalDateTime fromDate,
//        LocalDateTime toDate,
//        String keyword);
//
//    List<Card> finAllBySiteId(UUID siteId);
}
