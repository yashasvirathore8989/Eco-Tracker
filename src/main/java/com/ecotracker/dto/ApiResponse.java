package com.ecotracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Long id;
    private String status;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String message) {
        this.message = message;
    }

    public ApiResponse(Long id, String status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    public ApiResponse(Long id, String status, String message, T data) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> resp = new ApiResponse<>(message);
        resp.setData(data);
        return resp;
    }

    public static <T> ApiResponse<T> created(Long id, String status, String message) {
        return new ApiResponse<>(id, status, message);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
