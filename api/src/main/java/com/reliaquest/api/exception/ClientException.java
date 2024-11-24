package com.reliaquest.api.exception;

public class ClientException extends APIException {
    public ClientException(int code, String message) {
        super(code, message);
    }
}
