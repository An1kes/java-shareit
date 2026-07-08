package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        validateItem(itemDto);

        User owner = getUserOrThrow(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
            item.setRequest(request); // Привязываем запрос к вещи
        }

        Item savedItem = itemRepository.save(item);

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

        Item updatedItem = itemRepository.save(oldItem);

        log.info("Вещь с ID {} успешно обновлена владельцем с ID {}", itemId, userId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findAllByItem_Id(
                    itemId,
                    Sort.by(Sort.Direction.ASC, "start")
            );
            enrichItemWithBookings(itemDto, bookings);
        }
        List<CommentDto> comments = commentRepository.findAllByItem_Id(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();
        itemDto.setComments(comments);

        return itemDto;
    }

    @Override
    public Collection<ItemDto> findAllByOwnerId(Long userId) {
        getUserOrThrow(userId);
        log.info("Получен запрос на получение всех вещей владельца с ID {}", userId);

        Collection<Item> items = itemRepository.findAllByOwnerId(userId);

        List<ItemDto> itemDtos = new ArrayList<>();
        List<Long> itemIds = new ArrayList<>();

        for (Item item : items) {
            itemDtos.add(ItemMapper.toItemDto(item));
            itemIds.add(item.getId());
        }

        List<Comment> allComments = commentRepository.findAllByItem_IdIn(itemIds);
        Map<Long, List<CommentDto>> commentsByItemId = new HashMap<>();

        for (Comment comment : allComments) {
            Long itemId = comment.getItem().getId();
            CommentDto commentDto = CommentMapper.toCommentDto(comment);

            if (!commentsByItemId.containsKey(itemId)) {
                commentsByItemId.put(itemId, new ArrayList<>());
            }
            commentsByItemId.get(itemId).add(commentDto);
        }

        List<Booking> allBookings = bookingRepository.findAllByItem_IdInAndStatusOrderByStartAsc(itemIds, BookingStatus.APPROVED);
        Map<Long, List<Booking>> bookingsByItemId = groupBookingsByItemId(allBookings);

        for (ItemDto itemDto : itemDtos) {
            List<Booking> itemBookings = bookingsByItemId.getOrDefault(itemDto.getId(), new ArrayList<>());
            enrichItemWithBookings(itemDto, itemBookings);

            List<CommentDto> itemComments = commentsByItemId.get(itemDto.getId());
            if (itemComments == null) {
                itemComments = new ArrayList<>();
            }
            itemDto.setComments(itemComments);
        }

        return itemDtos;
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

    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        LocalDateTime now = LocalDateTime.now();

        boolean hasActiveBooking = bookingRepository.existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, now
        );

        if (!hasActiveBooking) {
            log.warn("Пользователь с ID {} пытается оставить отзыв на вещь с ID {}, которую он не арендовал (или аренда не завершена)", userId, itemId);
            throw new ValidationException("Оставить отзыв может только тот пользователь, у которого завершился срок аренды этой вещи");
        }

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now); // Фиксируем точное время написания отзыва

        Comment savedComment = commentRepository.save(comment);
        log.info("Пользователь с ID {} успешно оставил отзыв с ID {} к вещи с ID {}", userId, savedComment.getId(), itemId);

        return CommentMapper.toCommentDto(savedComment);
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

    private void enrichItemWithBookings(ItemDto itemDto, List<Booking> itemBookings) {
        if (itemBookings == null || itemBookings.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = null;
        Booking nextBooking = null;

        for (Booking booking : itemBookings) {
            if (booking.getStatus() == BookingStatus.APPROVED) {
                if (booking.getStart().isBefore(now) || booking.getStart().equals(now)) {
                    lastBooking = booking;
                } else if (booking.getStart().isAfter(now)) {
                    nextBooking = booking;
                    break;
                }
            }
        }

        if (lastBooking != null) {
            itemDto.setLastBooking(new ItemDto.BookingShortDto(lastBooking.getId(), lastBooking.getBooker().getId()));
        }
        if (nextBooking != null) {
            itemDto.setNextBooking(new ItemDto.BookingShortDto(nextBooking.getId(), nextBooking.getBooker().getId()));
        }
    }

    private Map<Long, List<Booking>> groupBookingsByItemId(List<Booking> allBookings) {
        Map<Long, List<Booking>> bookingsByItemId = new HashMap<>();

        for (Booking booking : allBookings) {
            Long itemId = booking.getItem().getId();

            if (!bookingsByItemId.containsKey(itemId)) {
                bookingsByItemId.put(itemId, new ArrayList<>());
            }
            bookingsByItemId.get(itemId).add(booking);
        }

        return bookingsByItemId;
    }
}
