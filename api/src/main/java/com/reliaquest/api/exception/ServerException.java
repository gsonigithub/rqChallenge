package com.reliaquest.api.exception;

public class ServerException extends APIException {

    public ServerException(int code, String message) {
        super(code, message);
    }
}
