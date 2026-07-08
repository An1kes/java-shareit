package ru.practicum.shareit.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userNotOwnerExc(final NotOwnerException e) {
        log.error("Ошибка: пользователь не хозяин вещи. Детали ошибки: {}", e.getMessage(), e);
        return new ErrorResponse("Пользователь не владелец вещи." + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundExc(final NotFoundException e) {
        log.error("Ошибка 404: ресурс не найден. Сообщение: {}", e.getMessage(), e);
        return new ErrorResponse("Пользователь не найден. " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parameterNotValidExc(final ValidationException e) {
        log.error("Ошибка валидации данных. Детали: {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка валидации: " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflictEmailUniqueExc(final ConflictException e) {
        log.error("Email уже занят другим пользователем. Детали: {}", e.getMessage(), e);
        return new ErrorResponse("Email уже занят другим пользователем: " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.error("Возникла непредвиденная ошибка сервера: ", e);
        return new ErrorResponse("Произошла непредвиденная ошибка: " + e.getMessage());
    }

}
