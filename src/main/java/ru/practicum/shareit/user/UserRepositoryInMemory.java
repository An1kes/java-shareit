package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserRepositoryInMemory {

    private final Map<Long, User> users = new HashMap<>();

    public User create(User user) {
        user.setId(getId());
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long userId) {
        User user = users.get(userId);
        return Optional.ofNullable(user);
    }

    public Collection<User> findAll() {
        return users.values();
    }

    public boolean deleteUser(Long userId) {
        return users.remove(userId) != null;
    }

    private long getId() {
        long lastId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++lastId;
    }
}
