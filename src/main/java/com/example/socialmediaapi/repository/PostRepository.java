package com.example.socialmediaapi.repository;

import com.example.socialmediaapi.entity.Post;
import com.example.socialmediaapi.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
Optional<Post> getPostById(Long id);
    List<Post> getPostsByUserId(Long userId);
    List<Post> findByUserInOrderByCreatedAtDesc(List<User> users, Pageable pageable);

}

