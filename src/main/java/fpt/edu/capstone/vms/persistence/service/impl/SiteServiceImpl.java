package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.CommuneRepository;
import fpt.edu.capstone.vms.persistence.repository.DistrictRepository;
import fpt.edu.capstone.vms.persistence.repository.ProvinceRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ISiteService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import fpt.edu.capstone.vms.util.Utils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SiteServiceImpl extends GenericServiceImpl<Site, UUID> implements ISiteService {

    private final SiteRepository siteRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final Utils utils;
    private final ModelMapper mapper;

    public SiteServiceImpl(SiteRepository siteRepository, ProvinceRepository provinceRepository, DistrictRepository districtRepository, CommuneRepository communeRepository, Utils utils, ModelMapper mapper) {
        this.siteRepository = siteRepository;
        this.provinceRepository = provinceRepository;
        this.districtRepository = districtRepository;
        this.communeRepository = communeRepository;
        this.utils = utils;
        this.mapper = mapper;
        this.init(siteRepository);
    }

    @Override
    public Site save(Site entity) {
        try {
            if (StringUtils.isEmpty(entity.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code is null");
            }
            if (siteRepository.existsByCode(entity.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of site is exist");
            }
            if (StringUtils.isEmpty(SecurityUtils.getOrgId())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "OrganizationId is null");
            }

            checkAddress(entity.getProvinceId(), entity.getDistrictId(), entity.getCommuneId());
            entity.setOrganizationId(UUID.fromString(SecurityUtils.getOrgId()));
            entity.setEnable(true);
            return siteRepository.save(entity);
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(), e.getMessage());
        }
    }

    @Override
    public Site updateSite(ISiteController.UpdateSiteInfo updateSite, UUID id) {

        if (!StringUtils.isEmpty(updateSite.getCode())) {
            if (siteRepository.existsByCode(updateSite.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of site is exist");
            }
        }

        var siteEntity = siteRepository.findById(id).orElse(null);
        var update = mapper.map(updateSite, Site.class);
        if (ObjectUtils.isEmpty(siteEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found site by id: " + id);
        if (!UUID.fromString(SecurityUtils.getOrgId()).equals(siteEntity.getOrganizationId())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in organization with organizationId = " +siteEntity.getOrganization());
        }
        siteRepository.save(siteEntity.update(update));
        return siteEntity;
    }

    @Override
    public Page<Site> filter(Pageable pageable, List<String> names, UUID orgId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return siteRepository.filter(
            pageable,
            names,
            orgId,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }

    @Override
    public List<Site> filter( List<String> names, UUID orgId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return siteRepository.filter(
            names,
            orgId,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }

    @Override
    public List<Site> findAllByOrganizationId(String organizationId) {
        return siteRepository.findAllByOrganizationId(UUID.fromString(organizationId));
    }


    private void checkAddress(Integer provinceId, Integer districtId, Integer communeId) {

        if (ObjectUtils.isEmpty(provinceId)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Province is null");
        }
        if (ObjectUtils.isEmpty(districtId)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "District is null");
        }
        if (ObjectUtils.isEmpty(communeId)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Commune is null");
        }

        var province = provinceRepository.findById(provinceId).orElse(null);

        if (ObjectUtils.isEmpty(province)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found province");
        }

        var district = districtRepository.findById(districtId).orElse(null);

        if (ObjectUtils.isEmpty(district)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found district");
        }

        if (district.getProvinceId() != province.getId()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "the district not in province please check it again");
        }

        var commune = communeRepository.findById(communeId).orElse(null);

        if (ObjectUtils.isEmpty(commune)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found commune");
        }

        if (commune.getDistrictId() != district.getId()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "the commune not in district please check it again");
        }
    }

}
