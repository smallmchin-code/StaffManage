package com.sm.leave.exception;

public class LeaveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 錯誤代碼
     */
    private String errorCode;

    /**
     * 預設建構子
     */
    public LeaveException() {
        super();
    }

    /**
     * 建構子
     * 
     * @param message 例外訊息
     */
    public LeaveException(String message) {
        super(message);
    }

    /**
     * 建構子
     * 
     * @param message 例外訊息
     * @param cause   原因例外
     */
    public LeaveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 建構子
     * 
     * @param cause 原因例外
     */
    public LeaveException(Throwable cause) {
        super(cause);
    }

    /**
     * 建構子（含錯誤代碼）
     * 
     * @param message   例外訊息
     * @param errorCode 錯誤代碼
     */
    public LeaveException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 建構子（含錯誤代碼和原因）
     * 
     * @param message   例外訊息
     * @param errorCode 錯誤代碼
     * @param cause     原因例外
     */
    public LeaveException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 取得錯誤代碼
     * 
     * @return 錯誤代碼
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 設定錯誤代碼
     * 
     * @param errorCode 錯誤代碼
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}
