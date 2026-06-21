package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        validateItem(itemDto);

        User owner = getUserOrThrow(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item savedItem = itemRepository.create(item);

        log.info("Успешно создана вещь с ID: {} для владельца с ID: {}", savedItem.getId(), userId);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        Item oldItem = getItemOrThrow(itemId);

        if (!oldItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID {} пытается обновить чужую вещь с ID {}", userId, itemId);
            throw new NotFoundException("Вещь с ID " + itemId + " не принадлежит пользователю с ID " + userId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            oldItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            oldItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.updateItem(oldItem);

        log.info("Вещь с ID {} успешно обновлена владельцем с ID {}", itemId, userId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = getItemOrThrow(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> findAllByOwnerId(Long userId) {
        getUserOrThrow(userId);
        return itemRepository.findAll()
                .stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        } else {
            return itemRepository.search(text)
                    .stream()
                    .map(ItemMapper::toItemDto)
                    .toList();
        }
    }

    @Override
    public Collection<ItemDto> findAll() {
        return itemRepository.findAll()
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден в системе", userId);
                    return new NotFoundException("Пользователь с ID " + userId + " не найден");
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Item с ID {} не найден в системе", itemId);
                    return new NotFoundException("Item с ID " + itemId + " не найден");
                });
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.error("Ошибка валидации: название вещи не может быть пустым");
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.error("Ошибка валидации: описание вещи не может быть пустым");
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            log.error("Ошибка валидации: статус доступности вещи не указан");
            throw new ValidationException("Статус доступности вещи должен быть указан");
        }
    }
}
