// ApiResponse.java - 개선 버전
package com.careercoach.careercoachapi.dto.response;

import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;         // 요청 성공 여부 (true/false)
    private String message;          // 응답 메시지 ("성공", "오류 메시지" 등)
    private T data;                  // 실제 응답 데이터 (제네릭 타입)
    private int statusCode;          // HTTP 상태 코드 (200, 400, 500 등)

    // 성공 응답 생성 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data, 200);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, 200);
    }

    // 실패 응답 생성 메서드
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, 500);
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return new ApiResponse<>(false, message, null, statusCode);
    }
}
