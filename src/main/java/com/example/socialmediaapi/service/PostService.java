package com.example.socialmediaapi.service;

import com.example.socialmediaapi.entity.Post;
import com.example.socialmediaapi.entity.PostResponse;
import com.example.socialmediaapi.entity.User;
import com.example.socialmediaapi.exception.PostNotFoundException;
import com.example.socialmediaapi.exception.UserNotFoundException;
import com.example.socialmediaapi.repository.PostRepository;

import com.example.socialmediaapi.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final SubscriptionService subscriptionService;

    public Post createPost(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public Post updatePost(Long postId, Post updatedPost) {
        Post existingPost = postRepository.findById(postId).orElse(null);
        if (existingPost != null) {
            existingPost.setTitle(updatedPost.getTitle());
            existingPost.setText(updatedPost.getText());

            if (updatedPost.getImage() != null) {
                existingPost.setImage(updatedPost.getImage());
            }

            return postRepository.save(existingPost);
        }

        return null;
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }


    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("Post not found"));
    }
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.getPostsByUserId(userId);
    }

    public List<PostResponse> createPostResponses(List<Post> posts) {

       return posts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getFeedForUser(User user, Pageable pageable) {
        List<User> followedUsers = subscriptionService.getTargetUsersFromSubscriptions(user);
        followedUsers.add(user); // Include user's own posts

        return postRepository.findByUserInOrderByCreatedAtDesc(followedUsers, pageable)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    private PostResponse convertToResponse(Post post) {
        String imageUrl = post.getImage() != null ?
                String.format("/api/v1/post/%d/image", post.getId()) : null;
        return new PostResponse(post.getTitle(), post.getText(), imageUrl);
    }
}
