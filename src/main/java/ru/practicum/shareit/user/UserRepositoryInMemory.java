package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepositoryInMemory implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long userId) {
        User user = users.get(userId);
        return Optional.ofNullable(user);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
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
