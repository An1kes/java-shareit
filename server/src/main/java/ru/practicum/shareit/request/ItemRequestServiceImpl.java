package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        User requestor = getUserOrThrow(userId);

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now()); // Фиксируем время создания запроса

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Успешно создан запрос вещи с ID {} для пользователя {}", savedRequest.getId(), userId);

        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public Collection<ItemRequestDto> findAllByRequestor(Long userId) {
        getUserOrThrow(userId);

        List<ItemRequest> requests = requestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId);
        log.info("Получен список запросов пользователя с ID {} (всего запросов: {})", userId, requests.size());

        return enrichRequestsWithReplies(requests);
    }

    @Override
    public Collection<ItemRequestDto> findAllByOthers(Long userId) {
        getUserOrThrow(userId);

        List<ItemRequest> requests = requestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(userId);
        log.info("Получен список чужих запросов для пользователя с ID {} (всего запросов: {})", userId, requests.size());

        return enrichRequestsWithReplies(requests);
    }

    @Override
    public ItemRequestDto findById(Long requestId, Long userId) {
        getUserOrThrow(userId); // Любой авторизованный пользователь может посмотреть детали запроса

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден"));

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

        List<ItemDto> replies = itemRepository.findAllByRequest_Id(requestId).stream()
                .map(ItemMapper::toItemDto)
                .toList();

        dto.setItems(replies);
        return dto;
    }


    // Метод, который за один запрос к БД находит все ответы ко всем запросам из списка
    private Collection<ItemRequestDto> enrichRequestsWithReplies(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();

        Collection<Item> allReplies = itemRepository.findAllByRequest_IdIn(requestIds);

        Map<Long, Collection<ItemDto>> repliesByRequestId = allCommentsToMap(allReplies);

        List<ItemRequestDto> dtos = new ArrayList<>();
        for (ItemRequest request : requests) {
            ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);

            Collection<ItemDto> requestReplies = repliesByRequestId.getOrDefault(request.getId(), new ArrayList<>());
            dto.setItems(requestReplies);

            dtos.add(dto);
        }

        return dtos;
    }

    private Map<Long, Collection<ItemDto>> allCommentsToMap(Collection<Item> allReplies) {
        Map<Long, Collection<ItemDto>> map = new HashMap<>();
        for (Item item : allReplies) {
            Long requestId = item.getRequest().getId();
            ItemDto itemDto = ItemMapper.toItemDto(item);

            if (!map.containsKey(requestId)) {
                map.put(requestId, new ArrayList<>());
            }
            map.get(requestId).add(itemDto);
        }
        return map;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }
}