package com.example.socialmediaapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MessageResponse {
    @JsonIgnore
    private Long senderId;
    private String senderUsername;
    private String content;

}
