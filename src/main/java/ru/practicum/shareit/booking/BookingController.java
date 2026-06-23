package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;


@RestController
@RequestMapping(path = "/bookings")

@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                             @Valid @RequestBody BookingDto bookingDto) {
        log.info("Получен запрос POST /bookings от пользователя с ID {} на создание бронирования: {}", userId, bookingDto);
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info("Получен запрос PATCH /bookings/{}?approved={} от пользователя с ID {}", bookingId, approved, userId);
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader(USER_ID_HEADER) Long userId,
                               @PathVariable Long bookingId) {
        log.info("Получен запрос GET /bookings/{} от пользователя с ID {} на получение данных о бронировании", bookingId, userId);
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingDto> findAllByBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен запрос GET /bookings?state={} от пользователя с ID {}", state, userId);
        return bookingService.findAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> findAllByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен запрос GET /bookings/owner?state={} от владельца с ID {}", state, userId);
        return bookingService.findAllByOwner(userId, state);
    }
}
