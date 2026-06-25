package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    public BookingDto create(BookingDto bookingDto, Long userId) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        checkBookingFeasibility(item, userId);
        validateBookingDates(bookingDto.getStart(), bookingDto.getEnd());

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Успешно создано бронирование с ID {} для пользователя {}", savedBooking.getId(), userId);

        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto approve(Long bookingId, Boolean approved, Long userId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID {} пытается изменить статус чужого бронирования с ID {}", userId, bookingId);
            throw new ValidationException("Изменять статус бронирования может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Попытка изменить уже установленный статус бронирования с ID {}", bookingId);
            throw new ValidationException("Статус бронирования уже изменен");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Статус бронирования с ID {} изменен на {} владельцем с ID {}", bookingId, booking.getStatus(), userId);

        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto findById(Long bookingId, Long userId) {
        Booking booking = getBookingOrThrow(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            log.warn("Пользователь с ID {} пытается просмотреть чужое бронирование с ID {}", userId, bookingId);
            throw new NotFoundException("Просматривать бронирование может только его автор или владелец вещи");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> findAllByBooker(Long userId, String state) {
        getUserOrThrow(userId);
        BookingState stateDto = parseState(state);

        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;

        switch (stateDto) {
            case CURRENT:
                bookings = bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, sortByStartDesc);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBooker_IdAndEndBefore(userId, now, sortByStartDesc);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBooker_IdAndStartAfter(userId, now, sortByStartDesc);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc);
                break;
            case ALL:
            default:
                bookings = bookingRepository.findAllByBooker_Id(userId, sortByStartDesc);
                break;
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public Collection<BookingDto> findAllByOwner(Long userId, String state) {
        getUserOrThrow(userId);
        BookingState stateDto = parseState(state);

        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;

        switch (stateDto) {
            case CURRENT:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartBeforeAndEndAfter(userId, now, now, sortByStartDesc);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItem_Owner_IdAndEndBefore(userId, now, sortByStartDesc);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartAfter(userId, now, sortByStartDesc);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc);
                break;
            case ALL:
            default:
                bookings = bookingRepository.findAllByItem_Owner_Id(userId, sortByStartDesc);
                break;
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с ID {} не найдено в БД", bookingId);
                    return new NotFoundException("Бронирование с ID " + bookingId + " не найдено");
                });
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (start == null || end == null
                || start.isAfter(end)
                || start.equals(end)
                || start.isBefore(now)
                || end.isBefore(now)) {

            log.warn("Некорректные даты бронирования: start={}, end={}, now={}", start, end, now);
            throw new ValidationException("Некорректные даты начала и окончания бронирования");
        }
    }

    private void checkBookingFeasibility(Item item, Long userId) {
        if (item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID {} пытается забронировать собственную вещь с ID {}", userId, item.getId());
            throw new NotFoundException("Владелец не может бронировать собственную вещь");
        }

        if (!item.isAvailable()) {
            log.warn("Попытка бронирования недоступной вещи с ID {}", item.getId());
            throw new ValidationException("Вещь с ID " + item.getId() + " недоступна для бронирования");
        }
    }

    private BookingState parseState(String stateStr) {
        if (stateStr == null) {
            return BookingState.ALL;
        }
        try {
            return BookingState.valueOf(stateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Передан неподдерживаемый статус бронирования: {}", stateStr);
            throw new ValidationException("Unknown state: " + stateStr);
        }
    }

    private enum BookingState {
        ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED
    }
}
