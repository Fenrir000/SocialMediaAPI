package com.example.socialmediaapi.controller;

import com.example.socialmediaapi.entity.Post;
import com.example.socialmediaapi.entity.PostResponse;
import com.example.socialmediaapi.entity.User;
import com.example.socialmediaapi.exception.PostNotFoundException;
import com.example.socialmediaapi.exception.UserNotFoundException;
import com.example.socialmediaapi.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private final PostService postService;


    @GetMapping("/feed")
    @Operation(description = "Api for  authorized user get his subscriptions posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
    })
    public ResponseEntity<List<PostResponse>> getFeed(@RequestParam int page, @RequestParam int size) {
        User currentUser = getCurrentAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<PostResponse> feed = postService.getFeedForUser(currentUser, pageable);
        return ResponseEntity.ok(feed);
    }

    @PostMapping
    @Operation(description = "Api for  authorized user to create post with title and text as json and image as multipart/form-data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Image error")
    })
    public ResponseEntity<?> createPost(@RequestPart("post") Post post,
                                        @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        User currentUser = getCurrentAuthenticatedUser();
        if (imageFile != null) {
            try {
                post.setImage(imageFile.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Image error");
            }
        }

        post.setUser(currentUser);
        Post createdPost = postService.createPost(post);

        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")

    @Operation(description = "Api for  authorized user to update his post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posted updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Image error")
    })
    public ResponseEntity<?> updatePost(@RequestPart("post") Post post,
                                        @RequestPart(value = "image", required = false) MultipartFile imageFile, @PathVariable Long postId) {
        Post existingPost = postService.getPostById(postId);
        if (!existingPost.getUser().getId().equals(getCurrentAuthenticatedUser().getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Post updatedPost = postService.updatePost(postId, post);
        if (imageFile != null) {
            try {
                updatedPost.setImage(imageFile.getBytes());

                updatedPost = postService.updatePost(postId, post);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Image error");
            }

        }
        String imageUrl = String.format("/api/v1/post/%d/image", postId); // URL для изображения
        PostResponse postResponse = new PostResponse(updatedPost.getTitle(), updatedPost.getText(), imageUrl);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);


    }

    @GetMapping("/{postId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Image error")
    })
    @Operation(description = "Api for  authorized user to get post")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        try {
            Post post = postService.getPostById(postId);
            String imageUrl = String.format("/api/v1/post/%d/image", postId);
            PostResponse postResponse = new PostResponse(post.getTitle(), post.getText(), imageUrl);
            return new ResponseEntity<>(postResponse, HttpStatus.OK);
        } catch (PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Post not found");
        }


    }

    @GetMapping("/{postId}/image")
    @Operation(description = "Api for  authorized user to get post image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "403", description = "Not found")
    })
    public ResponseEntity<?> getPostImage(@PathVariable Long postId) {
        try {
            Post post = postService.getPostById(postId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(post.getImage().length);
            return new ResponseEntity<>(post.getImage(), headers, HttpStatus.OK);
        } catch (PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Post not found");
        }


    }

    @GetMapping("/user/{userId}")
    @Operation(description = "Api for  authorized user to get all  posts by any user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
    })
    public ResponseEntity<?> getPostsByUser(@PathVariable Long userId) {
        List<Post> userPosts = postService.getPostsByUserId(userId);
        if (userPosts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<PostResponse> postResponses = postService.createPostResponses(userPosts);

        return new ResponseEntity<>(postResponses, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    @Operation(description = "Api for  authorized user to delete his post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
    })
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {

        Post existingPost = postService.getPostById(postId);
        if (!existingPost.getUser().getId().equals(getCurrentAuthenticatedUser().getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        postService.deletePost(postId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

}