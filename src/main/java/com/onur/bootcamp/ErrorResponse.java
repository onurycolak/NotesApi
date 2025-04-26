package com.onur.bootcamp;

public class ErrorResponse {
    public String error;
    public int status;

    public ErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
    }
}