package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Item create(Item item);

    Item updateItem(Item item);

    Optional<Item> findById(Long itemId);

    Collection<Item> findAll();

    Collection<Item> search(String text);
}
