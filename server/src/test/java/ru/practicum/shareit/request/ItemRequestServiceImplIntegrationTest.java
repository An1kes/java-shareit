package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private EntityManager em;

    @Test
    void findAllByRequestor_shouldReturnRequestsWithReplies() {
        User requestor = User.builder()
                .name("Алексей")
                .email("alex@mail.ru")
                .build();
        em.persist(requestor);

        User owner = User.builder()
                .name("Дмитрий")
                .email("dima@mail.ru")
                .build();
        em.persist(owner);

        ItemRequest request = ItemRequest.builder()
                .description("Ищу перфоратор")
                .requestor(requestor)
                .created(LocalDateTime.now().minusHours(1))
                .build();
        em.persist(request);

        Item item = Item.builder()
                .name("Перфоратор Bosch")
                .description("Мощный перфоратор, 800 Вт")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
        em.persist(item);

        em.flush();

        Collection<ItemRequestDto> result = requestService.findAllByRequestor(requestor.getId());

        assertNotNull(result);
        assertEquals(1, result.size());

        ItemRequestDto returnedRequestDto = result.iterator().next();
        assertEquals("Ищу перфоратор", returnedRequestDto.getDescription());

        assertNotNull(returnedRequestDto.getItems());
        assertEquals(1, returnedRequestDto.getItems().size());

        ItemDto reply = returnedRequestDto.getItems().iterator().next();


        assertEquals("Перфоратор Bosch", reply.getName());
        assertEquals(request.getId(), reply.getRequestId());

    }
}
