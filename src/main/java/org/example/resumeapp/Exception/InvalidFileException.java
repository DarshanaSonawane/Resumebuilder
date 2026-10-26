package org.example.resumeapp.Exception;

public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }
}