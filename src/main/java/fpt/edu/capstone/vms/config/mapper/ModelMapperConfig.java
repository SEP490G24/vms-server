package fpt.edu.capstone.vms.config.mapper;


import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.controller.IDeviceController;
import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.Device;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.ProvinceRepository;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

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

        // UpdateProfileUserInfo => IUserResource.UserDto
        modelMapper.createTypeMap(IUserController.UpdateProfileUserInfo.class, IUserResource.UserDto.class)
            .addMappings(mapping -> mapping.map(IUserController.UpdateProfileUserInfo::getPhoneNumber, IUserResource.UserDto::setPhone));


        // department => departmentFilterDTO
        modelMapper.createTypeMap(Department.class, IDepartmentController.DepartmentFilterDTO.class)
            .addMappings(mapping -> mapping.map((department -> department.getSite().getName()), IDepartmentController.DepartmentFilterDTO::setSiteName));

        // site => siteFilterDTO
        modelMapper.createTypeMap(Site.class, ISiteController.SiteFilterDTO.class)
            .addMappings(mapping -> mapping.map((site -> site.getOrganization().getName()), ISiteController.SiteFilterDTO::setOrganizationName))
            .addMappings(mapping -> mapping.map(site -> site.getProvince().getName(), ISiteController.SiteFilterDTO::setProvinceName))
            .addMappings(mapping -> mapping.map(site -> site.getDistrict().getName(), ISiteController.SiteFilterDTO::setDistrictName))
            .addMappings(mapping -> mapping.map(site -> site.getCommune().getName(), ISiteController.SiteFilterDTO::setCommuneName));

        // room => roomDto
        modelMapper.createTypeMap(Room.class, IRoomController.RoomFilterResponse.class)
            .addMappings(mapping -> mapping.map((room -> room.getSite().getName()), IRoomController.RoomFilterResponse::setSiteName))
            .addMappings(mapping -> mapping.map((room -> room.getDevice().getMacIp()), IRoomController.RoomFilterResponse::setMacIp))
            .addMappings(mapping -> mapping.map((room -> room.getDevice().getName()), IRoomController.RoomFilterResponse::setDeviceName))
            .addMappings(mapping -> mapping.map((room -> room.getDevice().getId()), IRoomController.RoomFilterResponse::setDeviceId));

        // device => DeviceFilterResponse
        modelMapper.createTypeMap(Device.class, IDeviceController.DeviceFilterResponse.class)
            .addMappings(mapping -> mapping.map((room -> room.getSite().getName()), IDeviceController.DeviceFilterResponse::setSiteName));

        // template => TemplateFilter
        modelMapper.createTypeMap(Template.class, ITemplateController.TemplateFilter.class)
            .addMappings(mapping -> mapping.map((template -> template.getSite().getName()), ITemplateController.TemplateFilter::setSiteName));

        // RoleRepresentation => RoleDto
        modelMapper.createTypeMap(RoleRepresentation.class, IRoleResource.RoleDto.class)
            .addMappings(mapping -> mapping.map(RoleRepresentation::getName, IRoleResource.RoleDto::setCode));

        // ticket => TicketFilterDTO
        modelMapper.createTypeMap(Ticket.class, ITicketController.TicketFilterDTO.class)
            .addMappings(mapping -> mapping.map((ticket -> ticket.getRoom().getName()), ITicketController.TicketFilterDTO::setRoomName))
            .addMappings(mapping -> mapping.map((ticket -> ticket.isBookmark()), ITicketController.TicketFilterDTO::setIsBookmark));


        // customerTicketMap => TicketByQRCodeResponseDTO
        modelMapper.createTypeMap(CustomerTicketMap.class, ITicketController.TicketByQRCodeResponseDTO.class)
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getSiteId()), ITicketController.TicketByQRCodeResponseDTO::setSiteId))
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
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getRoom().isSecurity()), ITicketController.TicketByQRCodeResponseDTO::setSecurity))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity()), ITicketController.TicketByQRCodeResponseDTO::setCustomerInfo));

        // customerTicketMap => AccessHistoryResponseDTO
        modelMapper.createTypeMap(CustomerTicketMap.class, IAccessHistoryController.AccessHistoryResponseDTO.class)
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getId().getTicketId()), IAccessHistoryController.AccessHistoryResponseDTO::setTicketId))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getCode()), IAccessHistoryController.AccessHistoryResponseDTO::setTicketCode))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getName()), IAccessHistoryController.AccessHistoryResponseDTO::setTicketName))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getStatus()), IAccessHistoryController.AccessHistoryResponseDTO::setTicketStatus))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getStatus()), IAccessHistoryController.AccessHistoryResponseDTO::setTicketCustomerStatus))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCheckInTime()), IAccessHistoryController.AccessHistoryResponseDTO::setCheckInTime))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCheckOutTime()), IAccessHistoryController.AccessHistoryResponseDTO::setCheckOutTime))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getUsername()), IAccessHistoryController.AccessHistoryResponseDTO::setCreateBy))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getCreatedOn()), IAccessHistoryController.AccessHistoryResponseDTO::setCreatedOn))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getRoom().getId()), IAccessHistoryController.AccessHistoryResponseDTO::setRoomId))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getRoom().getName()), IAccessHistoryController.AccessHistoryResponseDTO::setRoomName))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity().getId()), IAccessHistoryController.AccessHistoryResponseDTO::setCustomerId))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity().getVisitorName()), IAccessHistoryController.AccessHistoryResponseDTO::setVisitorName))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity().getIdentificationNumber()), IAccessHistoryController.AccessHistoryResponseDTO::setIdentificationNumber))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity().getEmail()), IAccessHistoryController.AccessHistoryResponseDTO::setEmail))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity().getPhoneNumber()), IAccessHistoryController.AccessHistoryResponseDTO::setPhoneNumber))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity().getGender()), IAccessHistoryController.AccessHistoryResponseDTO::setGender))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getCustomerEntity().getDescription()), IAccessHistoryController.AccessHistoryResponseDTO::setDescription))
            .addMappings(mapping -> mapping.map((customerTicketMap -> customerTicketMap.getTicketEntity().getSiteId()), IAccessHistoryController.AccessHistoryResponseDTO::setSiteId));

        //User => ProfileUser
        modelMapper.createTypeMap(User.class, IUserController.ProfileUser.class)
            .addMappings(mapping -> mapping.map((user -> user.getDepartment().getName()), IUserController.ProfileUser::setDepartmentName))
            .addMappings(mapping -> mapping.map((user -> user.getDepartment().getSite().getId()), IUserController.ProfileUser::setSiteId))
            .addMappings(mapping -> mapping.map((user -> user.getDepartment().getSite().getName()), IUserController.ProfileUser::setSiteName));

        // departmentInfo => Department
        modelMapper.createTypeMap(IDepartmentController.CreateDepartmentInfo.class, Department.class)
            .addMappings(mapping -> mapping.using(stringToUuidConverter)
                .map(departmentInfo -> departmentInfo.getSiteId(), Department::setSiteId));


        return modelMapper;
    }

    Converter<String, UUID> stringToUuidConverter = new Converter<String, UUID>() {
        @Override
        public UUID convert(MappingContext<String, UUID> context) {
            String source = context.getSource();
            return source != null ? UUID.fromString(source) : null;
        }
    };
}
