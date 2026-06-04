package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.InMemoryItemRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryItemRepositoryTest {
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository = new InMemoryItemRepository();
    }

    @Test
    void create_shouldSaveItemAndGenerateId() {
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Ударная дрель");
        item.setAvailable(true);

        Item savedItem = itemRepository.create(item);

        assertNotNull(savedItem.getId());
        assertEquals(1L, savedItem.getId());
        assertTrue(savedItem.isAvailable());
    }

    @Test
    void search_shouldFindAvailableItemsByTextInCaseInsensitiveMode() {
        Item item1 = new Item();
        item1.setName("Аккумуляторная Дрель");
        item1.setDescription("Мощный инструмент");
        item1.setAvailable(true);
        itemRepository.create(item1);

        Item item2 = new Item();
        item2.setName("Отвертка");
        item2.setDescription("Обычная дрель-шуруповерт");
        item2.setAvailable(false);
        itemRepository.create(item2);

        Collection<Item> foundItems = itemRepository.search("дРеЛь");

        assertEquals(1, foundItems.size(), "Должна быть найдена только 1 вещь (доступная)");
        Item foundItem = foundItems.iterator().next();
        assertEquals("Аккумуляторная Дрель", foundItem.getName());
    }

}
