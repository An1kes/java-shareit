package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class) // Указываем, какой контроллер тестируем
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc; // Инструмент для отправки виртуальных HTTP-запросов

    @Autowired
    private ObjectMapper mapper; // Утилита для конвертации объектов в JSON-строки

    @MockBean
    private ItemRequestService itemRequestService; // Подменяем реальный сервис моком

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Нужна ударная дрель")
                .build();

        ItemRequestDto outputDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна ударная дрель")
                .created(LocalDateTime.of(2026, 6, 25, 12, 0, 0))
                .items(List.of())
                .build();

        Mockito.when(itemRequestService.create(any(ItemRequestDto.class), anyLong()))
                .thenReturn(outputDto);
        mvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(inputDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ожидаем код 200/201 (в зависимости от вашей настройки)
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна ударная дрель")))
                .andExpect(jsonPath("$.created", is("2026-06-25T12:00:00")));
    }

    @Test
    void getRequestsByRequestor_shouldReturnList() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.of(2026, 6, 25, 12, 0, 0))
                .items(List.of())
                .build();

        Mockito.when(itemRequestService.findAllByRequestor(anyLong()))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests")
                        .header(userIdHeader, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Проверяем, что вернулся список из 1 элемента
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")));
    }
}
