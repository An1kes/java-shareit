package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Get items for user {}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(USER_ID_HEADER) long userId,
                                          @PathVariable long itemId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_HEADER) long userId,
                                             @RequestBody @Valid ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) long userId,
                                             @PathVariable long itemId,
                                             @RequestBody ItemDto itemDto) {
        log.info("Updating item {} for user {}, data={}", itemId, userId, itemDto);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        if (text == null || text.isBlank()) {
            log.info("Получен пустой поисковый запрос на шлюзе. Возвращаем пустой список.");
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }

        log.info("Search items with text={}", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(USER_ID_HEADER) long userId,
                                                @PathVariable long itemId,
                                                @RequestBody @Valid CommentDto commentDto) {
        log.info("Creating comment for item {}, userId={}, data={}", itemId, userId, commentDto);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}