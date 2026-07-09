package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemDto inputDto = ItemDto.builder()
                .name("Отвертка")
                .description("Обычная отвертка")
                .available(true)
                .build();

        ItemDto outputDto = ItemDto.builder()
                .id(1L)
                .name("Отвертка")
                .description("Обычная отвертка")
                .available(true)
                .build();

        Mockito.when(itemService.create(any(ItemDto.class), anyLong()))
                .thenReturn(outputDto);

        mvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(inputDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Отвертка")))
                .andExpect(jsonPath("$.description", is("Обычная отвертка")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void findById_shouldReturnItemWithComments() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Ударная дрель")
                .available(true)
                .comments(List.of())
                .build();

        Mockito.when(itemService.findById(anyLong(), anyLong()))
                .thenReturn(itemDto);

        mvc.perform(get("/items/1")
                        .header(userIdHeader, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")));
    }

    @Test
    void createComment_shouldReturnCreatedComment() throws Exception {
        CommentDto inputDto = CommentDto.builder()
                .text("Отличный инструмент")
                .build();

        CommentDto outputDto = CommentDto.builder()
                .id(1L)
                .text("Отличный инструмент")
                .authorName("Иван")
                .created(LocalDateTime.of(2026, 6, 25, 12, 0, 0))
                .build();

        Mockito.when(itemService.createComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(outputDto);

        mvc.perform(post("/items/1/comment")
                        .header(userIdHeader, 1L)
                        .content(mapper.writeValueAsString(inputDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Отличный инструмент")))
                .andExpect(jsonPath("$.authorName", is("Иван")));
    }
}
