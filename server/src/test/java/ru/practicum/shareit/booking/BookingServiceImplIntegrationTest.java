package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager em;

    @Test
    void findAllByBooker_shouldReturnFutureBookings() {
        User owner = User.builder()
                .name("Владелец")
                .email("owner_booking@mail.ru")
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор")
                .email("booker_booking@mail.ru")
                .build();
        em.persist(booker);

        Item item = Item.builder()
                .name("Стремянка")
                .description("Высокая стремянка")
                .available(true)
                .owner(owner)
                .build();
        em.persist(item);

        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        em.persist(booking);

        em.flush();

        Collection<BookingDto> result = bookingService.findAllByBooker(booker.getId(), "FUTURE");

        assertNotNull(result);
        assertEquals(1, result.size(), "Должно вернуться ровно 1 бронирование");

        BookingDto returnedDto = result.iterator().next();
        assertEquals(booking.getId(), returnedDto.getId());
        assertEquals(BookingStatus.APPROVED, returnedDto.getStatus());
        assertEquals("Стремянка", returnedDto.getItem().getName());
        assertEquals(booker.getId(), returnedDto.getBooker().getId());
    }
}
