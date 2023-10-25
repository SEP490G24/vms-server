package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IRoomController;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("Room Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    RoomRepository roomRepository;
    RoomServiceImpl roomService;

    @BeforeAll
    public void init() {
        roomRepository = mock(RoomRepository.class);
        roomService = new RoomServiceImpl(roomRepository, new ModelMapper());
    }

    @Test
    @DisplayName("when list room, then rooms are retrieved")
    void whenListRooms_ThenRoomsRetrieved() {

        //given
        Room room1 = new Room().builder().name("Room1").code("R1").build();
        Room room2 = new Room().builder().name("Room2").code("R2").build();
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

        //given
        String existingRoomId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
        Room room = new Room().builder().name("Room2").code("R2").build();
        when(roomRepository.findById(UUID.fromString(existingRoomId))).thenReturn(Optional.of(room));

        //when
        Room roomActual = roomService.findById(UUID.fromString(existingRoomId));

        // then
        assertNotNull(roomActual.getName());
        assertEquals("Room2", roomActual.getName());
        assertEquals("R2", roomActual.getCode());
    }

    @Test
    @DisplayName("given room id, when find non existing room, then exception is thrown")
    void givenRoomId_whenFindNonExistingRoom_ThenExceptionThrown() {

        //given
        String nonExistingRoomId = "A";
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
        Room room = new Room().builder().name("Room2").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();
        IRoomController.RoomDto roomDto = new IRoomController.RoomDto().builder().name("Room2").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();

        //when
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        Room roomActual = roomService.create(roomDto);

        //then
        assertEquals(room.getName(), roomActual.getName());
        assertNotNull(roomActual);
    }

    @Test
    @DisplayName("given Room incomplete data, when create new Room, then exception is thrown")
    void givenAdIncompleteData_whenCreateAd_ThenExceptionIsThrown() {

        //given
        Room room = new Room().builder().name("Room2").build();
        IRoomController.RoomDto roomDto = new IRoomController.RoomDto().builder().name("Room2").build();
        String errorMsg = "Unable to save an incomplete entity : " + roomDto;

        //when
        when(roomRepository.save(room)).thenThrow(new RuntimeException(errorMsg));
        RuntimeException throwException = assertThrows(RuntimeException.class, () -> roomService.create(roomDto));

        // then
        assertEquals(errorMsg, throwException.getMessage());
    }

//    @Test
//    @DisplayName("given Room id, when delete Room, then Room is retrieved")
//    void givenRoomId_whenDeleteRoom_ThenRoomRetrieved() {
//
//        //given
//        String existingRoomId = "134";
//        Room room = Room.builder().name("a").code("a").build();
//        when(roomRepository.findById(UUID.fromString(existingRoomId))).thenReturn(Optional.of(room));
//
//        //when
//        RoomDto authorDto1 = roomService.delete(UUID.fromString(existingRoomId));
//
//        //then
//        assertNotNull(authorDto1);
//        assertNotNull(authorDto1.getId());
//        assertEquals(author1.getId(), author1.getId());
//    }
//
//    @Test
//    @DisplayName("given Room id, when delete non existing Room, then exception is thrown")
//    void givenRoomId_whenDeleteNonExistingRoom_ThenExceptionThrown() {
//
//        //given
//        Long nonExistingRoomId = 404L;
//        String errorMsg = "Room Not Found : "+nonExistingRoomId;
//        when(authorRepositoryMock.findById(nonExistingRoomId)).thenThrow(new EntityNotFoundException(errorMsg));
//
//        //when
//        EntityNotFoundException throwException = assertThrows(EntityNotFoundException.class, () ->  authorService.delete(nonExistingRoomId));
//
//        // then
//        assertEquals(errorMsg, throwException.getMessage());
//    }

}
