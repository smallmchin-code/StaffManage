package com.sm.leave.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 統一 API 回應格式
 * 所有 REST API 的標準回應結構
 *
 * @param <T> 回應資料的泛型類型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String errorCode,
        LocalDateTime timestamp) {

    /**
     * 建立成功回應（含資料）
     *
     * @param data 回應資料
     * @param <T>  資料類型
     * @return 成功的 ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, null, LocalDateTime.now());
    }

    /**
     * 建立成功回應（含訊息和資料）
     *
     * @param message 成功訊息
     * @param data    回應資料
     * @param <T>     資料類型
     * @return 成功的 ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    /**
     * 建立成功回應（僅訊息，無資料）
     *
     * @param message 成功訊息
     * @return 成功的 ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, LocalDateTime.now());
    }

    /**
     * 建立錯誤回應
     *
     * @param message   錯誤訊息
     * @param errorCode 錯誤代碼
     * @return 錯誤的 ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, LocalDateTime.now());
    }

    /**
     * 建立錯誤回應（含資料）
     *
     * @param message   錯誤訊息
     * @param errorCode 錯誤代碼
     * @param data      額外資料
     * @param <T>       資料類型
     * @return 錯誤的 ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, T data) {
        return new ApiResponse<>(false, message, data, errorCode, LocalDateTime.now());
    }
}
