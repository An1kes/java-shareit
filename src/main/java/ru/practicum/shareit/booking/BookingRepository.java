package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    // ALL (все бронирования пользователя)
    List<Booking> findAllByBooker_Id(Long bookerId, Sort sort);

    // CURRENT (текущие бронирования: уже начались, но еще не закончились)
    List<Booking> findAllByBooker_IdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // PAST (завершенные бронирования: время конца в прошлом)
    List<Booking> findAllByBooker_IdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    // FUTURE (будущие бронирования: время начала в будущем)
    List<Booking> findAllByBooker_IdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    // WAITING или REJECTED (бронирования по конкретному статусу)
    List<Booking> findAllByBooker_IdAndStatus(Long bookerId, BookingStatus status, Sort sort);


    // ALL (все бронирования вещей конкретного владельца)
    List<Booking> findAllByItem_Owner_Id(Long ownerId, Sort sort);

    // CURRENT (текущие бронирования вещей владельца)
    List<Booking> findAllByItem_Owner_IdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // PAST (прошлые бронирования вещей владельца)
    List<Booking> findAllByItem_Owner_IdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    // FUTURE (будущие бронирования вещей владельца)
    List<Booking> findAllByItem_Owner_IdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    // WAITING или REJECTED для вещей владельца
    List<Booking> findAllByItem_Owner_IdAndStatus(Long ownerId, BookingStatus status, Sort sort);


    // Получить все бронирования для конкретной вещи (понадобится при просмотре вещей)
    List<Booking> findAllByItem_Id(Long itemId, Sort sort);

    // Альтернативный быстрый поиск всех бронирований для списка вещей (для избежания проблемы N+1)
    List<Booking> findAllByItem_IdIn(List<Long> itemIds, Sort sort);

    boolean existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
            Long bookerId,
            Long itemId,
            BookingStatus status,
            LocalDateTime now
    );
}

