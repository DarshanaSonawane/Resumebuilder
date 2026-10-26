package org.example.resumeapp.Exception;

public class ResourceExistsException extends RuntimeException {

    public ResourceExistsException(String message) {
        super(message);
    }
}