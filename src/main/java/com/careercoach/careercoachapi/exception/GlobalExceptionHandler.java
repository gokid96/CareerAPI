package com.careercoach.careercoachapi.exception;

import com.careercoach.careercoachapi.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 400 - 잘못된 요청 파라미터 타입
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("잘못된 파라미터 타입: {}", e.getName());
        return ResponseEntity.status(400)
                .body(ApiResponse.error("잘못된 파라미터 형식입니다.", 400));
    }

    /**
     * 400 - 필수 요청 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<String>> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("필수 파라미터 누락: {}", e.getParameterName());
        return ResponseEntity.status(400)
                .body(ApiResponse.error("필수 파라미터가 누락되었습니다: " + e.getParameterName(), 400));
    }

    /**
     * 400 - Validation 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("입력 검증 실패: {}", e.getBindingResult().getFieldErrors());

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400)
                .body(ApiResponse.error("입력값이 올바르지 않습니다: " + errorMessage, 400));
    }

    /**
     * 404 - 존재하지 않는 엔드포인트
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFoundException(NoHandlerFoundException e) {
        log.warn("존재하지 않는 엔드포인트 호출: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResponseEntity.status(404)
                .body(ApiResponse.error("요청하신 리소스를 찾을 수 없습니다.", 404));
    }

    /**
     * 405 - 지원하지 않는 HTTP 메서드
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("지원하지 않는 HTTP 메서드: {}", e.getMethod());
        return ResponseEntity.status(405)
                .body(ApiResponse.error("지원하지 않는 HTTP 메서드입니다.", 405));
    }

    /**
     * 500 - 일반적인 런타임 예외
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
        log.error("런타임 예외 발생", e);
        return ResponseEntity.status(500)
                .body(ApiResponse.error("서비스 처리 중 오류가 발생했습니다.", 500));
    }

    /**
     * 500 - 모든 예외의 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("예상치 못한 예외 발생", e);
        return ResponseEntity.status(500)
                .body(ApiResponse.error("시스템 오류가 발생했습니다.", 500));
    }
}