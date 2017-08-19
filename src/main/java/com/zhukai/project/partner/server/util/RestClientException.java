package com.zhukai.project.partner.server.util;

public class RestClientException extends Exception {

    public RestClientException() {
        super();
    }

    public RestClientException(Throwable cause) {
        super(cause);
    }

    public RestClientException(String message) {
        super(message);
    }

    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
