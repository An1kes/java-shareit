package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {


    User create(User user);

    User updateUser(User user);

    Optional<User> findById(Long userId);

    Collection<User> findAll();

    boolean deleteUser(Long userId);
}
