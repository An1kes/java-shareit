package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    Collection<UserDto> findAll();

    Boolean delete(Long userId);

    UserDto findById(Long userId);
}
