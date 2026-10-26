package org.example.resumeapp.Exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) { super(message); }
}