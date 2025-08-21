package com.careercoach.careercoachapi.exception;

import com.careercoach.careercoachapi.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            MethodArgumentNotValidException e) {

        log.warn("입력 검증 실패", e);

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400)
                .body(ApiResponse.error("입력값이 올바르지 않습니다: " + errorMessage, 400));
    }

    /**
     * CompletableFuture 관련 예외 처리
     */
    @ExceptionHandler({ExecutionException.class, InterruptedException.class, TimeoutException.class})
    public ResponseEntity<ApiResponse<String>> handleCompletableFutureException(Exception e) {
        log.error("비동기 작업 처리 중 오류", e);

        if (e instanceof TimeoutException) {
            return ResponseEntity.status(408)
                    .body(ApiResponse.error("요청 처리 시간이 초과되었습니다.", 408));
        }

        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("요청 처리가 중단되었습니다.", 500));
        }

        return ResponseEntity.status(500)
                .body(ApiResponse.error("서비스 처리 중 오류가 발생했습니다.", 500));
    }

    /**
     * 일반적인 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
        log.error("런타임 예외 발생", e);
        return ResponseEntity.status(500)
                .body(ApiResponse.error("서비스 처리 중 오류가 발생했습니다.", 500));
    }

    /**
     * 모든 예외의 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("예상치 못한 예외 발생", e);
        return ResponseEntity.status(500)
                .body(ApiResponse.error("시스템 오류가 발생했습니다.", 500));
    }
}