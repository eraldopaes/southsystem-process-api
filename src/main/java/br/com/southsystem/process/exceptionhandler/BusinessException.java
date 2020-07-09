package br.com.southsystem.process.exceptionhandler;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String message;

    public BusinessException(String message) {
        this.message = message;
    }
}
