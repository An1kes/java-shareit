package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testItemDtoSerialization() throws Exception {
        ItemDto.BookingShortDto lastBooking = new ItemDto.BookingShortDto(1L, 2L);
        ItemDto.BookingShortDto nextBooking = new ItemDto.BookingShortDto(3L, 4L);

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Отвертка")
                .description("Аккумуляторная отвертка")
                .available(true)
                .requestId(10L)
                .comments(List.of())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Отвертка");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Аккумуляторная отвертка");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(10);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(3);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(4);
    }

    @Test
    void testItemDtoDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "    \"name\": \"Дрель\",\n" +
                "    \"description\": \"Мощная дрель\",\n" +
                "    \"available\": true,\n" +
                "    \"requestId\": 5\n" +
                "}";

        ItemDto itemDto = json.parse(jsonContent).getObject();

        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(5L);
    }
}
