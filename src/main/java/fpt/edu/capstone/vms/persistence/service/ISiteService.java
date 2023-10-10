package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.UUID;


public interface ISiteService extends IGenericService<Site, UUID> {

    Site updateSite(ISiteController.updateSiteInfo updateSite, UUID id);
}
