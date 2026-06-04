package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item create(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        Item item = items.get(itemId);
        return Optional.ofNullable(item);
    }

    @Override
    public Collection<Item> findAll() {
        return items.values();

    }

    @Override
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
