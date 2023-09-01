package com.example.socialmediaapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String text;
    @Lob
    @JsonIgnore
    private byte[] image;

    @ManyToOne
    @JoinColumn(name = "_users")
    @JsonIgnore
    private User user;

    private LocalDateTime createdAt;


}