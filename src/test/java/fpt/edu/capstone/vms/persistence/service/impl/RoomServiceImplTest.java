package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Device;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DeviceRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
@DisplayName("Room Service Unit Tests")
class RoomServiceImplTest {

    RoomRepository roomRepository;
    RoomServiceImpl roomService;
    Pageable pageable;
    AuditLogRepository auditLogRepository;
    SiteRepository siteRepository;
    ModelMapper mapper;
    SecurityContext securityContext;
    Authentication authentication;
    DeviceRepository deviceRepository;
    TicketRepository ticketRepository;

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
        ticketRepository = mock(TicketRepository.class);
        mapper = mock(ModelMapper.class);
        roomService = new RoomServiceImpl(roomRepository, new ModelMapper(), auditLogRepository, siteRepository, deviceRepository, ticketRepository);
    }

    @Test
    @DisplayName("when list room, then rooms are retrieved")
    void whenListRooms_ThenRoomsRetrieved() {

        //given
        Room room1 = Room.builder().name("Room1").code("R1").build();
        Room room2 = Room.builder().name("Room2").code("R2").build();
        List<Room> mockRooms = Arrays.asList(room1, room2);

        //when
        when(roomRepository.findAll()).thenReturn(mockRooms);
        List<Room> rooms = roomService.findAll();

        //then
        assertEquals(2, rooms.size());
        assertEquals("Room1", rooms.get(0).getName());
        assertEquals("Room2", rooms.get(1).getName());
        assertNotNull(rooms);
        assertFalse(rooms.isEmpty());

        // Verify
        Mockito.verify(roomRepository, Mockito.times(1)).findAll();
    }

    @Test
    @DisplayName("given room id, when find existing room, then room are retrieved")
    void givenRoomId_whenFindExistingRoom_ThenRoomRetrieved() {
        // Given
        UUID roomId = UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        Room expectedRoom = new Room();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(expectedRoom));

        // When
        Room actualRoom = roomService.findById(roomId);

        // Then
        assertNotNull(actualRoom);
    }

    @Test
    @DisplayName("given room id, when find non existing room, then exception is thrown")
    void givenRoomId_whenFindNonExistingRoom_ThenExceptionThrown() {

        //given
        String nonExistingRoomId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
        String errorMsg = "Room Not Found : " + nonExistingRoomId;
        when(roomRepository.findById(UUID.fromString(nonExistingRoomId))).thenThrow(new EntityNotFoundException(errorMsg));

        //when
        EntityNotFoundException throwException = assertThrows(EntityNotFoundException.class, () -> roomService.findById((UUID.fromString(nonExistingRoomId))));

        // then
        assertEquals(errorMsg, throwException.getMessage());
    }

    @Test
    @DisplayName("given room data, when create new Room, then Room id is returned")
    void givenRoomData_whenCreateRoom_ThenRoomReturned() {

        //given
        Room room = Room.builder().name("Room2").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();
        IRoomController.RoomDto roomDto = IRoomController.RoomDto.builder().name("Room2").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();

        room.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

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
        //when
        when(SecurityUtils.checkSiteAuthorization(siteRepository, room.getSiteId().toString())).thenReturn(true);
        when(siteRepository.findById(room.getSiteId())).thenReturn(Optional.of(site));
        when(roomRepository.existsByCodeAndSiteId(roomDto.getCode(), room.getSiteId())).thenReturn(false);

        // When
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        Room roomActual = roomService.create(roomDto);

        //then
        assertEquals(room.getName(), roomActual.getName());
        assertNotNull(roomActual);
    }

    @Test
    @DisplayName("given RoomDto is null, when create room, then exception is thrown")
    void givenRoomDtoIsNull_whenCreateRoom_ThenThrowHttpClientErrorException() {
        // Given
        IRoomController.RoomDto roomDto = null;
        // When and Then
        assertThrows(CustomException.class, () -> roomService.create(roomDto));
    }


    @Test
    @DisplayName("given Room is found, when update room, then Room id is returned")
    void givenRoomData_whenUpdateRoom_ThenRoomReturned() {
        // Given
        UUID roomId = UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        Room roomInfo = new Room();
        roomInfo.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        roomInfo.setEnable(true);
        Room existingRoom = new Room();
        existingRoom.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        existingRoom.setId(roomId);

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

        when(SecurityUtils.checkSiteAuthorization(siteRepository, existingRoom.getSiteId().toString())).thenReturn(true);
        // When
        Room updatedRoom = roomService.update(roomInfo, roomId);

        // Then
        assertNotNull(updatedRoom);

        // Add more assertions to check specific details of the updatedRoom object if needed.
        //verify(roomRepository, times(1)).findById(roomId);
        verify(roomRepository, times(1)).save(existingRoom.update(roomInfo));
    }

    @Test
    @DisplayName("given Room is not found, when update room,  then exception is thrown")
    void givenRoomNotFound_whenUpdateRoom_thenThrowHttpClientErrorException() {
        // Given
        UUID nonExistingRoomId = UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d1");
        Room roomInfo = new Room();
        String expectedErrorMessage = "404 Can't found room";

        when(roomRepository.findById(nonExistingRoomId)).thenReturn(Optional.empty());

        // When and Then
        assertThrows(CustomException.class, () -> roomService.update(roomInfo, nonExistingRoomId));
    }

    @Test
    void filter() {
        // Given
        List<String> names = Arrays.asList("Room1", "Room2");
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
        List<Room> expectedRooms = List.of();
        when(roomRepository.filter(names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null)).thenReturn(expectedRooms);

        // When
        List<Room> filteredRooms = roomService.filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword, null);

        // Then
        assertNotNull(filteredRooms);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(roomRepository, times(1)).filter(names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null);
    }

    @Test
    void filterPageable() {
        // Given
        List<String> names = Arrays.asList("Room1", "Room2");
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
        Page<Room> expectedRooms = new PageImpl<>(List.of());
        when(roomRepository.filter(pageable, names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null)).thenReturn(expectedRooms);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());        // When
        Page<Room> filteredRoomPage = roomService.filter(pageableSort, names, siteId, createdOnStart, createdOnEnd, enable, keyword, null);

        // Then
        assertEquals(null, filteredRoomPage);
        // Add assertions to check the content of the filteredRoomPage, depending on the expected behavior
        verify(roomRepository, times(1)).filter(pageable, names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase(), null);
    }

    @Test
    void testFinAllBySiteId() {

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
        // Mock input data
        String siteId = "63139e5c-3d0b-46d3-8167-fe59cf46d3d5";

        // Mock room repository behavior
        Room room = new Room();
        when(roomRepository.findAllBySiteIdAndEnableIsTrue(any(UUID.class))).thenReturn(Collections.singletonList(room));

        // Call the method
        List<Room> result = roomService.finAllBySiteId(siteId);

        // Verify the interactions
        verify(roomRepository, times(1)).findAllBySiteIdAndEnableIsTrue(UUID.fromString(siteId));
        assertEquals(1, result.size());
        assertEquals(room, result.get(0));
    }

    @Test
    void testFinAllBySiteIdWithNoPermission() {
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
        // Mock input data
        String siteId = "63139e5c-3d0b-46d3-8167-fe59cf46d3d4";

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> roomService.finAllBySiteId(siteId));
    }

    @Test
    void testDeleteRoom() {
        // Mock input data
        UUID roomId = UUID.randomUUID();

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
        // Mock input data
        String siteId = "63139e5c-3d0b-46d3-8167-fe59cf46d3d5";

        // Mock room repository behavior
        Room room = new Room();
        room.setId(roomId);
        room.setSiteId(UUID.fromString(siteId)); // Set a valid siteId for authorization check
        when(roomRepository.findById(roomId)).thenReturn(java.util.Optional.of(room));

        Site site = new Site();
        site.setId(UUID.fromString(siteId));
        site.setOrganizationId(UUID.fromString(siteId));
        // Mock site repository behavior
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(site));

        // Call the method
        assertDoesNotThrow(() -> roomService.deleteRoom(roomId));
    }

    @Test
    void testDeleteRoomWithNoPermission() {
        // Mock input data
        UUID roomId = UUID.randomUUID();

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
        // Mock input data
        String siteId = "63139e5c-3d0b-46d3-8167-fe59cf46d3d4";

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> roomService.deleteRoom(roomId));
    }

    @Test
    void testCreateRoomWithDuplicateCode() {
        // Similar to the previous test, but set up roomRepository.existsByCodeAndSiteId to return true
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

        // Mock input data
        IRoomController.RoomDto roomDto = new IRoomController.RoomDto();
        roomDto.setCode("Room123");
        roomDto.setSiteId(UUID.randomUUID());

        // Mock site repository behavior
        when(siteRepository.findById(roomDto.getSiteId())).thenReturn(java.util.Optional.of(new Site()));

        // Mock room repository behavior
        when(roomRepository.existsByCodeAndSiteId(roomDto.getCode(), roomDto.getSiteId())).thenReturn(true);

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> roomService.create(roomDto));
    }

    @Test
    void testCreateRoomWithInvalidDevice() {
        // Set up a scenario where the specified device is not found
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

        // Mock input data
        IRoomController.RoomDto roomDto = new IRoomController.RoomDto();
        roomDto.setCode("Room123");
        roomDto.setSiteId(UUID.randomUUID());
        roomDto.setDeviceId(1); // Assuming the device ID is provided

        // Mock site repository behavior
        when(siteRepository.findById(roomDto.getSiteId())).thenReturn(java.util.Optional.of(new Site()));

        Device device = new Device();
        // Mock device repository behavior
        when(deviceRepository.findById(roomDto.getDeviceId())).thenReturn(java.util.Optional.of(device));

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> roomService.create(roomDto));
    }
}
