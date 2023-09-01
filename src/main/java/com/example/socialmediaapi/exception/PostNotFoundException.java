package com.example.socialmediaapi.exception;

public class PostNotFoundException  extends RuntimeException {
    public PostNotFoundException(String message) {
        super(message);
    }
}
