package com.bci.userservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private List<ErrorDetail> error;

    public ErrorResponse(List<ErrorDetail> error) {
        this.error = error;
    }

    public List<ErrorDetail> getError() { return error; }
    public void setError(List<ErrorDetail> error) { this.error = error; }

    public static class ErrorDetail {
        private LocalDateTime timestamp;
        private int codigo;
        private String detail;

        public ErrorDetail(LocalDateTime timestamp, int codigo, String detail) {
            this.timestamp = timestamp;
            this.codigo = codigo;
            this.detail = detail;
        }

        // Getters y Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public int getCodigo() { return codigo; }
        public void setCodigo(int codigo) { this.codigo = codigo; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }
}