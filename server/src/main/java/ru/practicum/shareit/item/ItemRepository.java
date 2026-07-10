package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {


    @Query("select i from Item i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            "and i.available = true")
    Collection<Item> search(String text);

    Collection<Item> findAllByOwnerId(Long ownerId);

    Collection<Item> findAllByRequest_IdIn(Collection<Long> requestIds);

    Collection<Item> findAllByRequest_Id(Long requestId);
}
