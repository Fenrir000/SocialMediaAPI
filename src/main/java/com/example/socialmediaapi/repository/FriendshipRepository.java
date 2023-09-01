package com.example.socialmediaapi.repository;

import com.example.socialmediaapi.entity.Friendship;
import com.example.socialmediaapi.entity.Post;
import com.example.socialmediaapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    boolean existsByUser1AndUser2(User sender, User targetUser);

    Friendship findByUser1AndUser2(User user, User user2);
}
