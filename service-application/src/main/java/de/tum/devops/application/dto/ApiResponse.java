package de.tum.devops.application.dto;

import java.time.LocalDateTime;

/**
 * Unified API response wrapper
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private int code;

    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data, int code) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    // Static factory methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 200);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "Created successfully", data, 201);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(false, message, null, 400);
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(false, message, null, 401);
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(false, message, null, 403);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, message, null, 404);
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(false, message, null, 500);
    }

    // Getters and Setters
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}