// CustomException.java
package com.careercoach.careercoachapi.exception;

import com.careercoach.careercoachapi.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

public class CustomException extends RuntimeException {

    private final int statusCode;

    public CustomException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public CustomException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public CustomException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

