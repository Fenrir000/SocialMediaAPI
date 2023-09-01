package com.example.socialmediaapi.service;

import com.example.socialmediaapi.entity.*;
import com.example.socialmediaapi.exception.UserNotFoundException;
import com.example.socialmediaapi.repository.FriendshipRepository;
import com.example.socialmediaapi.repository.MessageRepository;
import com.example.socialmediaapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;


    private final  UserRepository userRepository;

    private  final FriendshipRepository friendshipRepository;
    public void sendMessage(User sender, Long receiverId, String content) {
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (receiver != null) {
            if (areFriends(sender, receiver)) {
                Message message = new Message();
                message.setSender(sender);
                message.setReceiver(receiver);
                message.setContent(content);
                messageRepository.save(message);
            } else {
                throw new IllegalArgumentException("You can only send messages to friends.");
            }
        }
    }


    public List<MessageResponse> getChatMessages(User user, Long friendId) {
        User friend = userRepository.findById(friendId).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (friend != null && areFriends(user, friend)) {
            List<Message> messages = messageRepository.findBySenderAndReceiver(user, friend);
            messages.addAll(messageRepository.findBySenderAndReceiver(friend, user));
            messages.sort(Comparator.comparing(Message::getId));

            return messages.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean areFriends(User user1, User user2) {
        Friendship friendship = friendshipRepository.findByUser1AndUser2(user1, user2);
        if (friendship==null){
            friendship = friendshipRepository.findByUser1AndUser2(user2, user1);
        }
        return friendship != null && friendship.getStatus() == FriendshipStatus.ACCEPTED;
    }
    private MessageResponse convertToResponse(Message message) {
        return MessageResponse.builder().senderUsername(message.getSender().getName()).content(message.getContent()).build();
    }
}
