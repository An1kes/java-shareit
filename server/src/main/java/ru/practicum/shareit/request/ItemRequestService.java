package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId);

    Collection<ItemRequestDto> findAllByRequestor(Long userId);

    Collection<ItemRequestDto> findAllByOthers(Long userId);

    ItemRequestDto findById(Long requestId, Long userId);
}