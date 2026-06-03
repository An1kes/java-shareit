package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long itemId, ItemDto itemDto, Long userId);

    ItemDto findById(Long itemId);

    Collection<ItemDto> findAllByOwnerId(Long userId);

    Collection<ItemDto> search(String text);

    Collection<ItemDto> findAll();
}
