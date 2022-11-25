package com.stocks.controllers;

import com.stocks.entities.exception.ErrorMessage;
import com.stocks.entities.exception.ResourceNotFoundException;
import com.stocks.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class ExceptionHandlerController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerController.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorMessage> serverExceptionHandler(Exception ex) {
        String errorMessage = CommonUtils.formatException(ex);
        logger.error(errorMessage);
        return Mono.just(new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(ZoneOffset.UTC),
                ex.getMessage(),
                errorMessage));
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ErrorMessage resourceNotFoundException(ResourceNotFoundException ex) {
        String errorMessage = CommonUtils.formatException(ex);
        return new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(ZoneOffset.UTC),
                ex.getMessage(),
                errorMessage);
    }

}
