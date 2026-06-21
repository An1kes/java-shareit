package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserRepositoryInMemory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryInMemoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Каждый тест начинает работу с чистым репозиторием в памяти
        userRepository = new UserRepositoryInMemory();
    }

    @Test
    void create_shouldSaveUserAndGenerateId() {
        User user = new User();
        user.setName("Иван");
        user.setEmail("ivan@mail.ru");

        User savedUser = userRepository.create(user);

        assertNotNull(savedUser.getId(), "ID пользователя не должен быть null после сохранения");
        assertEquals(1L, savedUser.getId(), "Первый ID должен быть равен 1");
        assertEquals("Иван", savedUser.getName());
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        User user = new User();
        user.setName("Петр");
        user.setEmail("petr@mail.ru");
        userRepository.create(user);

        Optional<User> foundUserOpt = userRepository.findById(1L);

        assertTrue(foundUserOpt.isPresent(), "Пользователь должен быть найден");
        assertEquals("Петр", foundUserOpt.get().getName());
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenNotExists() {
        Optional<User> foundUserOpt = userRepository.findById(99L);

        assertTrue(foundUserOpt.isEmpty(), "Optional должен быть пустым для несуществующего ID");
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        User user = new User();
        user.setName("Анна");
        user.setEmail("anna@mail.ru");
        userRepository.create(user);

        boolean deleted = userRepository.deleteUser(1L);

        assertTrue(deleted, "Метод должен вернуть true при успешном удалении");
        assertTrue(userRepository.findById(1L).isEmpty(), "Пользователь не должен находиться после удаления");
    }
}
