package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.IAccessHistoryService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessHistoryServiceImpl extends GenericServiceImpl<Ticket, UUID> implements IAccessHistoryService {

    static final String PATH_FILE = "/jasper/access-history.jrxml";

    final ModelMapper mapper;
    final SiteRepository siteRepository;
    final CustomerTicketMapRepository customerTicketMapRepository;


    public AccessHistoryServiceImpl(TicketRepository ticketRepository,
                                    ModelMapper mapper, SiteRepository siteRepository,
                                    CustomerTicketMapRepository customerTicketMapRepository) {
        this.mapper = mapper;
        this.siteRepository = siteRepository;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.init(ticketRepository);
    }


    @Override
    public Page<CustomerTicketMap> accessHistory(Pageable pageable, String keyword, Constants.StatusTicket status,
                                                 LocalDateTime formCheckInTime, LocalDateTime toCheckInTime,
                                                 LocalDateTime formCheckOutTime, LocalDateTime toCheckOutTime, List<String> sites) {
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        Page<CustomerTicketMap> customerTicketMapPage = customerTicketMapRepository.accessHistory(pageableSort, SecurityUtils.getListSiteToString(siteRepository, sites), formCheckInTime, toCheckInTime, formCheckOutTime, toCheckOutTime, status, keyword, null);
        return customerTicketMapPage;
    }

    @Override
    public IAccessHistoryController.AccessHistoryResponseDTO viewAccessHistoryDetail(UUID ticketId, UUID customerId) {
        var customerTicketMap = customerTicketMapRepository.findByCustomerTicketMapPk_TicketIdAndCustomerTicketMapPk_CustomerId(ticketId, customerId);
        return mapper.map(customerTicketMap, IAccessHistoryController.AccessHistoryResponseDTO.class);
    }

    @Override
    public ByteArrayResource export(IAccessHistoryController.AccessHistoryFilter filter) throws JRException {
        Pageable pageable = PageRequest.of(0, 99999);
        Page<CustomerTicketMap> customerTicketMapPage = accessHistory(pageable, filter.getKeyword(), filter.getStatus(), filter.getFormCheckInTime(), filter.getToCheckInTime(), filter.getFormCheckOutTime(), filter.getToCheckOutTime(), filter.getSites());
        List<IAccessHistoryController.AccessHistoryResponseDTO> accessHistoryResponseDTOS = mapper.map(customerTicketMapPage.getContent(), new TypeToken<List<IAccessHistoryController.AccessHistoryResponseDTO>>() {
        }.getType());
        Page<IAccessHistoryController.AccessHistoryResponseDTO> listData = new PageImpl<>(accessHistoryResponseDTOS, pageable, customerTicketMapPage.getTotalElements());
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(PATH_FILE));

            JRBeanCollectionDataSource listDataSource = new JRBeanCollectionDataSource(
                listData.getContent().size() == 0 ? Collections.singletonList(new IAccessHistoryController.AccessHistoryResponseDTO()) : listData.getContent());
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("tableDataset", listDataSource);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
            exporter.exportReport();

            byte[] excelBytes = byteArrayOutputStream.toByteArray();
            return new ByteArrayResource(excelBytes);
        } catch (Exception e) {
            return null;
        }
    }
}
