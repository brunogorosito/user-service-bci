// GlobalExceptionHandler.java
package com.bci.userservice.exception;

import com.bci.userservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse.ErrorDetail error = new ErrorResponse.ErrorDetail(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(Collections.singletonList(error));
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse.ErrorDetail error = new ErrorResponse.ErrorDetail(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(Collections.singletonList(error));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        ErrorResponse.ErrorDetail error = new ErrorResponse.ErrorDetail(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(Collections.singletonList(error));
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Datos inválidos";

        ErrorResponse.ErrorDetail error = new ErrorResponse.ErrorDetail(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), message);
        ErrorResponse response = new ErrorResponse(Collections.singletonList(error));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}