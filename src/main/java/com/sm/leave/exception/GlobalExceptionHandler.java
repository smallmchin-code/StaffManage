package com.sm.leave.exception;

import com.sm.leave.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    

    /**
     * 處理考試異常
     * 
     * @param e       考試異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(LeaveException.class)
    public ResponseEntity<ApiResponse<Void>> handleExamException(
            LeaveException e, HttpServletRequest request) {

        logger.warn("考試操作失敗，請求路徑: {}, 錯誤訊息: {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "EXAM_OPERATION_FAILED"));
    }

    /**
     * 處理參數驗證異常（@Valid）
     * 
     * @param e       參數驗證異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        logger.warn("參數驗證失敗，請求路徑: {}", request.getRequestURI());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("參數驗證失敗", "VALIDATION_FAILED", fieldErrors));
    }

    /**
     * 處理綁定異常
     * 
     * @param e       綁定異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
            BindException e, HttpServletRequest request) {

        logger.warn("資料綁定失敗，請求路徑: {}", request.getRequestURI());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("資料綁定失敗", "BINDING_FAILED", fieldErrors));
    }

    /**
     * 處理方法參數類型不匹配異常
     * 
     * @param e       參數類型不匹配異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        logger.warn("參數類型不匹配，請求路徑: {}, 參數名: {}, 期望類型: {}",
                request.getRequestURI(), e.getName(), e.getRequiredType());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        String.format("參數 '%s' 的類型不正確", e.getName()),
                        "PARAMETER_TYPE_MISMATCH"));
    }

    /**
     * 處理非法參數異常
     * 
     * @param e       非法參數異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {

        logger.warn("非法參數異常，請求路徑: {}, 錯誤訊息: {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("參數錯誤: " + e.getMessage(), "ILLEGAL_ARGUMENT"));
    }

    /**
     * 處理空指標異常
     * 
     * @param e       空指標異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(
            NullPointerException e, HttpServletRequest request) {

        logger.error("空指標異常，請求路徑: {}", request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統內部錯誤，請稍後再試", "NULL_POINTER_EXCEPTION"));
    }

    /**
     * 處理運行時異常
     * 
     * @param e       運行時異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {

        logger.error("運行時異常，請求路徑: {}, 錯誤訊息: {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統錯誤，請稍後再試", "RUNTIME_EXCEPTION"));
    }

    /**
     * 處理所有其他未捕獲的異常
     * 
     * @param e       異常
     * @param request HTTP 請求
     * @return 錯誤回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception e, HttpServletRequest request) {

        logger.error("未處理的異常，請求路徑: {}, 異常類型: {}, 錯誤訊息: {}",
                request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統發生未預期的錯誤，請聯繫管理員", "UNKNOWN_ERROR"));
    }
}
