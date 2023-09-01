package com.example.socialmediaapi.entity;

import lombok.Data;
import org.springframework.http.HttpHeaders;

@Data
public  class PostResponse {
    private final String title;
    private final String text;
    private final String imageUrl;


    public PostResponse(String title, String text, String imageUrl) {
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;

    }

}