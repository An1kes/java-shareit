package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testBookingDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 6, 25, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 26, 14, 30, 0);

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .itemId(5L)
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-06-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-06-26T14:30:00");

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(5);
    }

    @Test
    void testBookingDtoDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "    \"itemId\": 10,\n" +
                "    \"start\": \"2026-07-01T10:00:00\",\n" +
                "    \"end\": \"2026-07-02T18:00:00\"\n" +
                "}";

        BookingDto bookingDto = json.parse(jsonContent).getObject();

        assertThat(bookingDto.getItemId()).isEqualTo(10L);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2026, 7, 1, 10, 0, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2026, 7, 2, 18, 0, 0));
    }
}
