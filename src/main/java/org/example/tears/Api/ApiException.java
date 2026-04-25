package org.example.tears.Api;
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(String message) {
        super(message);
        this.status = 400;
    }

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}