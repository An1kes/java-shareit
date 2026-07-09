package ru.practicum.shareit.booking;

import java.util.Collection;

public interface BookingService {

    BookingDto create(BookingDto bookingDto, Long userId);

    BookingDto approve(Long bookingId, Boolean approved, Long userId);

    BookingDto findById(Long bookingId, Long userId);

    Collection<BookingDto> findAllByBooker(Long userId, String state);

    Collection<BookingDto> findAllByOwner(Long userId, String state);
}