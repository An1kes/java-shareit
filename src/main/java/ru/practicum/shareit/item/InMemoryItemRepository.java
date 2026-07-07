package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    public Item create(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long itemId) {
        Item item = items.get(itemId);
        return Optional.ofNullable(item);
    }

    public Collection<Item> findAll() {
        return items.values();

    }

    public Collection<Item> search(String text) {

        String query = text.toLowerCase();
        return items.values().stream()
                .filter(Item::isAvailable)
                .filter(item -> item.getName().toLowerCase().contains(query)
                        || item.getDescription().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }

    private long getId() {
        long lastId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++lastId;
    }
}
