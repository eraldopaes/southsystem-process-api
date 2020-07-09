package br.com.southsystem.process.exceptionhandler;

import lombok.Getter;

@Getter
public class FileProcessException extends RuntimeException {

    private final String message;
    private final String line;

    public FileProcessException(String message) {
        this.message = message;
        this.line = "";
    }

    public FileProcessException(String message, String line) {
        this.message = message;
        this.line = line;
    }
}
