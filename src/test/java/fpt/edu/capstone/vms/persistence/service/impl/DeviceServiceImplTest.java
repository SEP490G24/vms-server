package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDeviceController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Device;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DeviceRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("Device Service Unit Tests")
class DeviceServiceImplTest {

    RoomRepository roomRepository;
    DeviceServiceImpl deviceService;
    Pageable pageable;
    AuditLogRepository auditLogRepository;
    SiteRepository siteRepository;
    ModelMapper mapper;
    SecurityContext securityContext;
    Authentication authentication;
    DeviceRepository deviceRepository;

    @BeforeAll
    public void init() {
        MockitoAnnotations.openMocks(this);
        pageable = mock(Pageable.class);
        roomRepository = mock(RoomRepository.class);
        siteRepository = mock(SiteRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        deviceRepository = mock(DeviceRepository.class);
        mapper = mock(ModelMapper.class);
        deviceService = new DeviceServiceImpl(roomRepository, deviceRepository, mapper, auditLogRepository, siteRepository);

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    @DisplayName("given Device data, when create new Device, then Device id is returned")
    void givenDeviceData_whenCreateDevice_ThenDeviceReturned() {

        //given
        Device device = Device.builder().name("Device2").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();
        IDeviceController.DeviceDto deviceDto = IDeviceController.DeviceDto.builder().name("Device").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();

        device.setId(1);
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(mapper.map(deviceDto, Device.class)).thenReturn(device);
        when(deviceRepository.findByMacIp(device.getMacIp())).thenReturn(device);
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        when(siteRepository.findById(device.getSiteId())).thenReturn(Optional.of(site));
        when(SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())).thenReturn(true);
        //when
        when(SecurityUtils.checkSiteAuthorization(siteRepository, device.getSiteId().toString())).thenReturn(true);
        when(siteRepository.findById(device.getSiteId())).thenReturn(Optional.of(site));
        when(deviceRepository.existsByCodeAndSiteId(deviceDto.getCode(), UUID.randomUUID())).thenReturn(true);
        // When
        when(deviceRepository.save(any(Device.class))).thenReturn(device);
        Device device1 = deviceService.create(deviceDto);

        //then
        assertEquals(device.getName(), device1.getName());
        assertNotNull(device1);
    }

    @Test
    @DisplayName("given DeviceDto is null, when create Device, then exception is thrown")
    void givenDeviceDtoIsNull_whenCreateDevice_ThenThrowHttpClientErrorException() {
        // Given
        IDeviceController.DeviceDto deviceDto = null;

        // When and Then
        assertThrows(CustomException.class, () -> deviceService.create(deviceDto));
    }


    @Test
    @DisplayName("given device is found, when update device, then device id is returned")
    void givenDeviceData_whenUpdateDevice_ThenDeviceReturned() {
        // Given
        Integer deviceId = 1;
        Device device = new Device();
        device.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        device.setEnable(true);
        device.setDeviceType(Constants.DeviceType.SCAN_CARD);
        device.setMacIp("E8:DB:84:ED:6E:EA");
        Device existingDevice = new Device();
        existingDevice.setDeviceType(Constants.DeviceType.SCAN_CARD);
        existingDevice.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));
        existingDevice.setId(deviceId);

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        //when
        when(siteRepository.findById(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"))).thenReturn(Optional.of(site));

        when(SecurityUtils.checkSiteAuthorization(siteRepository, existingDevice.getSiteId().toString())).thenReturn(true);
        // When
        Device updatedRoom = deviceService.update(device, deviceId);

        // Then
        assertNotNull(updatedRoom);

        // Add more assertions to check specific details of the updatedDevice object if needed.
        //verify(roomRepository, times(1)).findById(DeviceId);
        verify(deviceRepository, times(1)).save(existingDevice.update(device));
    }

    @Test
    @DisplayName("given Device is not found, when update Device,  then exception is thrown")
    void givenDeviceNotFound_whenUpdateDevice_thenThrowHttpClientErrorException() {
        // Given
        Integer deviceId = 1;
        Device device = new Device();
        String expectedErrorMessage = "404 Can't found device";

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When and Then
        assertThrows(CustomException.class, () -> deviceService.update(device, deviceId));

    }

    @Test
    void filter() {
        // Given
        List<String> names = Arrays.asList("Device1", "Device2");
        List<String> siteId = Arrays.asList("06eb43a7-6ea8-4744-8231-760559fe2c08", "06eb43a7-6ea8-4744-8231-760559fe2c07");
        List<UUID> siteIds = Arrays.asList(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"), UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));

        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "example";

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);
        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c07")).thenReturn(true);
        List<Device> expectedDevices = List.of();
        when(deviceRepository.filter(names, siteIds, null, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null)).thenReturn(expectedDevices);

        // When
        List<Device> filteredRooms = deviceService.filter(names, siteId, null, createdOnStart, createdOnEnd, enable, keyword, null);

        // Then
        assertNotNull(filteredRooms);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(deviceRepository, times(1)).filter(names, siteIds, null, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null);
    }

    @Test
    void filterPageable() {
        // Given
        List<String> names = Arrays.asList("Device1", "Device2");
        List<String> siteId = Arrays.asList("06eb43a7-6ea8-4744-8231-760559fe2c08", "06eb43a7-6ea8-4744-8231-760559fe2c07");
        List<UUID> siteIds = Arrays.asList(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"), UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));

        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "example";

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);
        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c07")).thenReturn(true);
        Page<Device> expectedDevices = new PageImpl<>(List.of());
        when(deviceRepository.filter(pageable, names, siteIds, null, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null)).thenReturn(expectedDevices);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());        // When
        Page<Device> filteredRoomPage = deviceService.filter(pageableSort, names, siteId, null, createdOnStart, createdOnEnd, enable, keyword, null);

        // Then
        assertEquals(null, filteredRoomPage);
        // Add assertions to check the content of the filteredRoomPage, depending on the expected behavior
        verify(deviceRepository, times(1)).filter(pageable, names, siteIds, null, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null);
    }

    @Test
    void testDeleteDevice() {
        // Mock input data
        Integer deviceId = 1;

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock device repository behavior
        Device device = new Device();
        device.setId(deviceId);
        device.setSiteId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5")); // Set a valid siteId for authorization check
        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.of(device));

        Site site = new Site();
        site.setId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        site.setOrganizationId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        when(siteRepository.findById(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"))).thenReturn(Optional.of(site));
        device.setSite(site);


        // Call the method
        assertDoesNotThrow(() -> deviceService.deleteDevice(deviceId));
    }

    @Test
    void testDeleteDeviceWithNoPermission() {

        // Mock input data
        Integer deviceId = 1;

        // Mock device repository behavior
        Device device = new Device();
        device.setId(deviceId);
        device.setSiteId(UUID.randomUUID()); // Set an invalid siteId for authorization check
        when(deviceRepository.findById(deviceId)).thenReturn(java.util.Optional.of(device));

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> deviceService.deleteDevice(deviceId));


    }

}
