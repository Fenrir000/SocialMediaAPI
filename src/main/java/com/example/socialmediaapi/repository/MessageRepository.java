package com.example.socialmediaapi.repository;

import com.example.socialmediaapi.entity.Message;
import com.example.socialmediaapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndReceiver(User sender, User receiver);
}