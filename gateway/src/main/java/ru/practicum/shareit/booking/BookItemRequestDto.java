package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
	private long itemId;

    @NotNull(message = "Дата начала не может быть пустой")
	@FutureOrPresent
	private LocalDateTime start;

    @NotNull(message = "Дата конца не может быть пустой")
	@Future
	private LocalDateTime end;
}
