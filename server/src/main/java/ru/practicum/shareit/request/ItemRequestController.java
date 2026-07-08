package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestService requestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    // 1. Добавить новый запрос вещи (POST /requests)
    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Получен запрос POST /requests от пользователя с ID {} на создание запроса вещи", userId);
        return requestService.create(itemRequestDto, userId);
    }

    // 2. Получить список СВОИХ запросов вместе с ответами (GET /requests)
    @GetMapping
    public Collection<ItemRequestDto> findAllByRequestor(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос GET /requests от пользователя с ID {} на получение списка своих запросов", userId);
        return requestService.findAllByRequestor(userId);
    }

    // 3. Получить список ЧУЖИХ запросов (GET /requests/all)
    @GetMapping("/all")
    public Collection<ItemRequestDto> findAllByOthers(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос GET /requests/all от пользователя с ID {} на просмотр чужих запросов", userId);
        return requestService.findAllByOthers(userId);
    }

    // 4. Получить данные об одном конкретном запросе по ID (GET /requests/{requestId})
    @GetMapping("/{requestId}")
    public ItemRequestDto findById(@RequestHeader(USER_ID_HEADER) Long userId,
                                   @PathVariable Long requestId) {
        log.info("Получен запрос GET /requests/{} от пользователя с ID {} на получение деталей запроса", requestId, userId);
        return requestService.findById(requestId, userId);
    }
}