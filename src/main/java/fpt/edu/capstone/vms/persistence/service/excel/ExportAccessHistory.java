package fpt.edu.capstone.vms.persistence.service.excel;

import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.service.IAccessHistoryService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class ExportAccessHistory {

    private static final String PATH_FILE = "/jasper/access-history.jrxml";
    private final IAccessHistoryService accessHistoryService;
    private final ModelMapper mapper;

    public ByteArrayResource export(IAccessHistoryController.AccessHistoryFilter filter) throws JRException {
        Pageable pageable = PageRequest.of(0, 99999);
        Page<CustomerTicketMap> customerTicketMapPage = accessHistoryService.accessHistory(pageable, filter.getKeyword(), filter.getStatus(), filter.getFormCheckInTime(), filter.getToCheckInTime(), filter.getFormCheckOutTime(), filter.getToCheckOutTime(), filter.getSites());
        List<IAccessHistoryController.AccessHistoryResponseDTO> accessHistoryResponseDTOS = mapper.map(customerTicketMapPage.getContent(), new TypeToken<List<IAccessHistoryController.AccessHistoryResponseDTO>>() {
        }.getType());
        Page<IAccessHistoryController.AccessHistoryResponseDTO> listData = new PageImpl<>(accessHistoryResponseDTOS, pageable, customerTicketMapPage.getTotalElements());
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(PATH_FILE));

            JRBeanCollectionDataSource listDataSource = new JRBeanCollectionDataSource(
                listData.getContent().size() == 0 ? Collections.singletonList(new IAccessHistoryController.AccessHistoryResponseDTO()) : listData.getContent());
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("tableDataset", listDataSource);
            parameters.put("exporter", SecurityUtils.fullName());
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
