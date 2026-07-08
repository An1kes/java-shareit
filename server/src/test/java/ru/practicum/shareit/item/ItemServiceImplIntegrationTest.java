package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private EntityManager em;

    @Test
    void findAllByOwnerId_shouldReturnItemsWithBookings() {
        User owner = User.builder()
                .name("Владелец")
                .email("owner@mail.ru")
                .build();
        em.persist(owner);

        User booker = User.builder()
                .name("Арендатор")
                .email("booker@mail.ru")
                .build();
        em.persist(booker);

        Item item = Item.builder()
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .owner(owner)
                .build();
        em.persist(item);

        Booking lastBooking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        em.persist(lastBooking);

        em.flush();

        Collection<ItemDto> result = itemService.findAllByOwnerId(owner.getId());

        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1, result.size(), "Должна вернуться ровно 1 вещь");

        ItemDto returnedItemDto = result.iterator().next();
        assertEquals("Дрель", returnedItemDto.getName());

        assertNotNull(result.iterator().next().getLastBooking(), "Поле lastBooking должно быть заполнено");
        assertEquals(lastBooking.getId(), returnedItemDto.getLastBooking().getId());
        assertEquals(booker.getId(), returnedItemDto.getLastBooking().getBookerId());
    }
}