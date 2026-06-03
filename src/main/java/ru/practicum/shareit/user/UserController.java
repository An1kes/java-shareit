package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;


import java.util.Collection;


@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;


    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Получен запрос GET /users на получение всех пользователей");
        return userService.findAll();
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Получен запрос POST /users на создание пользователя: {}", userDto);
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto updatedUserDto) {
        log.info("Получен запрос PATCH /users/{} на обновление данных пользователя: {}", userId, updatedUserDto);
        return userService.updateUser(userId, updatedUserDto);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        log.info("Получен запрос GET /users/{} на получение пользователя по ID", userId);
        return userService.findById(userId);
    }

    @DeleteMapping("/{userId}")
    public Boolean delete(@PathVariable Long userId) {
        log.info("Получен запрос DELETE /users/{} на удаление пользователя", userId);
        return userService.delete(userId);
    }
}
