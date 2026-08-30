package com.aceli.mock.exception;

import org.springframework.http.HttpStatus;

public class FundingRequestException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public FundingRequestException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
}
