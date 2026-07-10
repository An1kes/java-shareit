package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        BookingDto inputDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto outputDto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .item(ItemDto.builder().id(1L).name("Дрель").build())
                .booker(UserDto.builder().id(2L).name("Алексей").build())
                .build();

        Mockito.when(bookingService.create(any(BookingDto.class), anyLong()))
                .thenReturn(outputDto);

        mvc.perform(post("/bookings")
                        .header(userIdHeader, 2L)
                        .content(mapper.writeValueAsString(inputDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.item.name", is("Дрель")))
                .andExpect(jsonPath("$.booker.name", is("Алексей")));
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() throws Exception {
        BookingDto outputDto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .item(ItemDto.builder().id(1L).name("Дрель").build())
                .booker(UserDto.builder().id(2L).name("Алексей").build())
                .build();

        Mockito.when(bookingService.approve(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(outputDto);

        mvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1L)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        BookingDto outputDto = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .item(ItemDto.builder().id(1L).name("Дрель").build())
                .booker(UserDto.builder().id(2L).name("Алексей").build())
                .build();

        Mockito.when(bookingService.findById(anyLong(), anyLong()))
                .thenReturn(outputDto);

        mvc.perform(get("/bookings/1")
                        .header(userIdHeader, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }
}