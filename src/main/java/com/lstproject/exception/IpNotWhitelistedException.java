package com.lstproject.exception;

public class IpNotWhitelistedException extends RuntimeException {
    public IpNotWhitelistedException(String message) {
        super(message);
    }
}
