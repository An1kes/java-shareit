package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 6, 25, 15, 0, 0);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Ищу перфоратор")
                .created(created)
                .items(List.of())
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ищу перфоратор");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-06-25T15:00:00");
    }

    @Test
    void testItemRequestDtoDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "    \"description\": \"Ищу стремянку\"\n" +
                "}";

        ItemRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getDescription()).isEqualTo("Ищу стремянку");
    }
}
