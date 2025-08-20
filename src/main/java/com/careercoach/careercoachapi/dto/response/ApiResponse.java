
// ApiResponse.java
package com.careercoach.careercoachapi.dto.response;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private int statusCode;

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

    // 생성자
    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, int statusCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    // Getter/Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}