package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        checkEmailUnique(userDto.getEmail(), -1L);
        User user = UserMapper.toUser(userDto);

        userRepository.save(user);

        log.info("Пользователь успешно создан с ID: {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User oldUser = getUserOrThrow(userId);

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            oldUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            if (!userDto.getEmail().equals(oldUser.getEmail())) {
                if (!userDto.getEmail().contains("@") || userDto.getEmail().isBlank()) {
                    throw new ValidationException("Некорректный формат email");
                }
                checkEmailUnique(userDto.getEmail(), oldUser.getId());
                oldUser.setEmail(userDto.getEmail());
            }
        }

        userRepository.save(oldUser);
        log.info("Пользователь успешно обновлен с ID: {}", oldUser.getId());
        return UserMapper.toUserDto(oldUser);
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();

    }

    @Override
    public Boolean delete(Long userId) {
        getUserOrThrow(userId);
        log.info("Пользователь успешно удален с ID: {}", userId);
        userRepository.deleteById(userId);
        return true;
    }

    @Override
    public UserDto findById(Long userId) {
        User findUser = getUserOrThrow(userId);
        return UserMapper.toUserDto(findUser);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден в системе", userId);
                    return new NotFoundException("Пользователь с ID " + userId + " не найден");
                });
    }

    private void checkEmailUnique(String email, Long userId) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            // Если email найден у ДРУГОГО пользователя, бросаем ошибку 409
            if (!user.getId().equals(userId)) {
                log.warn("Попытка зарегистрировать уже занятый email: {}", email);
                throw new ConflictException("Email " + email + " уже занят другим пользователем");
            }
        });
    }

}
