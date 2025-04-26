package com.onur.bootcamp;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Standard error response")
public class ErrorResponse {
    @Schema(description = "Error message", example = "Note not found")
    public String error;
    @Schema(description = "Status", example = "400")
    public int status;

    public ErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
    }
}