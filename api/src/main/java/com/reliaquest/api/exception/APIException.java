package com.reliaquest.api.exception;

public class APIException extends RuntimeException {

    public APIException(int code, String message) {
        super(String.format("Client Error Received. code: %s message: %s", code, message));
    }
}
