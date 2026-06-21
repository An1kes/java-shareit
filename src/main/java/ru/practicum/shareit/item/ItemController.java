package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос POST /items от пользователя с ID {} на добавление вещи: {}", userId, itemDto);
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос PATCH /items/{} от пользователя с ID {} на обновление вещи: {}", itemId, userId, itemDto);
        return itemService.update(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId) {
        log.info("Получен запрос GET /items/{} на получение информации о вещи", itemId);
        return itemService.findById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> findAllByOwnerId(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос GET /items от пользователя с ID {} на получение списка всех его вещей", userId);
        return itemService.findAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        log.info("Получен запрос GET /items/search с поисковым запросом: \"{}\"", text);
        return itemService.search(text);
    }
}
