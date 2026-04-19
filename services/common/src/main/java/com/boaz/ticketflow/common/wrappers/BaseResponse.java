package com.boaz.ticketflow.common.wrappers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String path;
    private String message;
    private int status;
    
    @Builder.Default
    private T content = null;

    /* ================= SUCCESS ================= */
    
    /** Succès principal avec message et contenu */
    public static <T> BaseResponse<T> success(
        String message, 
        T content, 
        int status
    ) {
        return BaseResponse.<T>builder()
            .timestamp(LocalDateTime.now())
            .path(getCurrentPath())
            .message(message)
            .status(status)
            .content(content)
            .build();
    }

    /** Succès avec contenu et message par défaut */
    public static <T> BaseResponse<T> success(T content) {
        return success("Operation successful", content, HttpStatus.OK.value());
    }

    /** Succès avec message personnalisé et contenu facultatif */
    public static <T> BaseResponse<T> successMessage(String message, T content) {
        return success(message, content, HttpStatus.OK.value());
    }

    /** Succès avec seulement message */
    public static BaseResponse<Void> successMessage(String message) {
        return success(message, null, HttpStatus.OK.value());
    }

    /** Succès sans contenu ni message personnalisé */
    public static BaseResponse<Void> success() {
        return success("Operation successful", null, HttpStatus.OK.value());
    }

    /* ================= ERROR ================= */

    // Error complet
    public static <T> BaseResponse<T> error(
        String message,
        int status,
        T errorDetails
    ) {
        return BaseResponse.<T>builder()
            .message(message)
            .status(status)
            .content(errorDetails)
            .path(getCurrentPath())
            .timestamp(LocalDateTime.now())
            .build();
    }

    // Error avec seulement message et status
    public static BaseResponse<Void> error(
        String message,
        int status
    ) {
        return error(message, status, null);
    }

    // Error avec seulement message
    public static BaseResponse<Void> error(
        String message
    ) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    // Error avec seulement status
    public static BaseResponse<Void> error(
        int status
    ) {
        return error("An error occurred", status);
    }

    // Error avec HttpStatus
    public static BaseResponse<Void> error(
        String message,
        HttpStatus status
    ) {
        return error(message, status.value());
    }

    /* ================= UTILITY METHODS ================= */
    
    private static String getCurrentPath() {
        HttpServletRequest request = getCurrentHttpRequest();
        return request != null ? request.getRequestURI() : "camergo-app";
    }

    private static HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /* ================= BUILDER METHODS ================= */
    
    public BaseResponse<T> withMessage(String message) {
        this.message = message;
        return this;
    }

    public BaseResponse<T> withStatus(int status) {
        this.status = status;
        return this;
    }

    public BaseResponse<T> withcontent(T content) {
        this.content = content;
        return this;
    }

    public BaseResponse<T> withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String toJson() {
        // Simple JSON representation for demonstration purposes
        return String.format(
            "{\"message\":\"%s\",\"status\":%d,\"content\":%s,\"path\":\"%s\",\"timestamp\":\"%s\"}",
            message,
            status,
            content != null ? content.toString() : "null",
            path,
            timestamp.toString()
        );
    }
}