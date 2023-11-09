package fpt.edu.capstone.vms.config.mapper;


import fpt.edu.capstone.vms.controller.*;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.*;
import fpt.edu.capstone.vms.persistence.repository.ProvinceRepository;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    private final ProvinceRepository provinceRepository;

    public ModelMapperConfig(ProvinceRepository provinceRepository) {
        this.provinceRepository = provinceRepository;
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // UserEntity => IUserResource.UserDto
        modelMapper.createTypeMap(User.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(User::getPhoneNumber, IUserResource.UserDto::setPhone));

        // UserRepresentation => IUserResource.UserDto
        modelMapper.createTypeMap(UserRepresentation.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(UserRepresentation::getId, IUserResource.UserDto::setOpenid));

        // IUserResource.UserDto => UserEntity
        modelMapper.createTypeMap(IUserResource.UserDto.class, User.class)
                .addMappings(mapping -> mapping.map(IUserResource.UserDto::getPhone, User::setPhoneNumber));

        // CreateUserInfo => IUserResource.UserDto
        modelMapper.createTypeMap(IUserController.CreateUserInfo.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(IUserController.CreateUserInfo::getPhoneNumber, IUserResource.UserDto::setPhone));

        // UpdateUserInfo => IUserResource.UserDto
        modelMapper.createTypeMap(IUserController.UpdateUserInfo.class, IUserResource.UserDto.class)
                .addMappings(mapping -> mapping.map(IUserController.UpdateUserInfo::getPhoneNumber, IUserResource.UserDto::setPhone));

        // department => departmentFilterDTO
        modelMapper.createTypeMap(Department.class, IDepartmentController.DepartmentFilterDTO.class)
            .addMappings(mapping -> mapping.map((department -> department.getSite().getName()), IDepartmentController.DepartmentFilterDTO::setSiteName));

        // site => siteFilterDTO
        modelMapper.createTypeMap(Site.class, ISiteController.SiteFilterDTO.class)
            .addMappings(mapping -> mapping.map((site -> site.getOrganization().getName()), ISiteController.SiteFilterDTO::setOrganizationName))
            .addMappings(mapping -> mapping.map(site -> site.getProvince().getName(), ISiteController.SiteFilterDTO::setProvinceName))
            .addMappings(mapping -> mapping.map(site -> site.getDistrict().getName(), ISiteController.SiteFilterDTO::setDistrictName))
            .addMappings(mapping -> mapping.map(site -> site.getCommune().getName(), ISiteController.SiteFilterDTO::setCommuneName));

        // customer => customerFilterDTO
        modelMapper.createTypeMap(Customer.class, ICustomerController.CustomerInfo.class)
            .addMappings(mapping -> mapping.map(site -> site.getProvince().getName(), ICustomerController.CustomerInfo::setProvinceName))
            .addMappings(mapping -> mapping.map(site -> site.getDistrict().getName(), ICustomerController.CustomerInfo::setDistrictName))
            .addMappings(mapping -> mapping.map(site -> site.getCommune().getName(), ICustomerController.CustomerInfo::setCommuneName));

        // room => roomDto
        modelMapper.createTypeMap(Room.class, IRoomController.RoomFilterResponse.class)
            .addMappings(mapping -> mapping.map((room -> room.getSite().getName()), IRoomController.RoomFilterResponse::setSiteName));

        // template => templateDto
        modelMapper.createTypeMap(Template.class, ITemplateController.TemplateDto.class)
            .addMappings(mapping -> mapping.map((template -> template.getSite().getName()), ITemplateController.TemplateDto::setSiteName));

        // RoleRepresentation => RoleDto
        modelMapper.createTypeMap(RoleRepresentation.class, IRoleResource.RoleDto.class)
            .addMappings(mapping -> mapping.map(RoleRepresentation::getName, IRoleResource.RoleDto::setCode));

        // ticket => TicketFilterDTO
        modelMapper.createTypeMap(Ticket.class, ITicketController.TicketFilterDTO.class)
            .addMappings(mapping -> mapping.map((ticket -> ticket.getRoom().getName()), ITicketController.TicketFilterDTO::setRoomName));

        // ticket => TicketFilterDTO
        modelMapper.createTypeMap(CustomerTicketMap.class, ITicketController.TicketByQRCodeResponseDTO.class)
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getId().getTicketId()), ITicketController.TicketByQRCodeResponseDTO::setTicketId))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getCode()), ITicketController.TicketByQRCodeResponseDTO::setTicketCode))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getName()), ITicketController.TicketByQRCodeResponseDTO::setTicketName))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getPurpose()), ITicketController.TicketByQRCodeResponseDTO::setPurpose))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getStatus()), ITicketController.TicketByQRCodeResponseDTO::setTicketStatus))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getStatus()), ITicketController.TicketByQRCodeResponseDTO::setTicketCustomerStatus))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getStartTime()), ITicketController.TicketByQRCodeResponseDTO::setStartTime))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getEndTime()), ITicketController.TicketByQRCodeResponseDTO::setEndTime))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getUsername()), ITicketController.TicketByQRCodeResponseDTO::setCreateBy))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getCreatedOn()), ITicketController.TicketByQRCodeResponseDTO::setCreatedOn))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getRoom().getId()), ITicketController.TicketByQRCodeResponseDTO::setRoomId))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getRoom().getName()), ITicketController.TicketByQRCodeResponseDTO::setRoomName))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity()), ITicketController.TicketByQRCodeResponseDTO::setCustomerInfo));
        return modelMapper;
    }
}
