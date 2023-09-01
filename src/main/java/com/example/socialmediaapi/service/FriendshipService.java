package com.example.socialmediaapi.service;

import com.example.socialmediaapi.entity.Friendship;
import com.example.socialmediaapi.entity.FriendshipStatus;
import com.example.socialmediaapi.entity.Subscription;
import com.example.socialmediaapi.entity.User;
import com.example.socialmediaapi.exception.FriendshipNotFoundException;
import com.example.socialmediaapi.exception.SubscriptionNotFoundException;
import com.example.socialmediaapi.exception.UserNotFoundException;
import com.example.socialmediaapi.repository.FriendshipRepository;
import com.example.socialmediaapi.repository.SubscriptionRepository;
import com.example.socialmediaapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    public void sendFriendRequest(User sender, Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new UserNotFoundException("User not found"));
        if ( !friendshipRepository.existsByUser1AndUser2(sender, targetUser)) {
            Subscription subscription= subscriptionService.findByUsers(sender,targetUser);
            if (subscription == null){subscription= Subscription.builder().subscriber(sender).targetUser(targetUser).build();
                subscriptionService.subscribe(subscription);}


            Friendship friendship = Friendship.builder().user1(sender).user2(targetUser).status(FriendshipStatus.EXPECTATION).build();
            friendshipRepository.save(friendship);

            log.info("предложение отправлено");
        }
    }
    public void acceptFriendRequest(Long newFriendId, User receiver) {
        User newFriend= userRepository.findById(newFriendId).orElseThrow(() -> new UserNotFoundException("User not found"));
        Friendship friendship=findFriendshipByTwoIds(newFriend,receiver);
        if (friendship==null){
            throw new FriendshipNotFoundException("Friendship not found");
        }
        Subscription subscription= subscriptionService.findByUsers(newFriend,receiver);
        if (subscription==null){
            throw new SubscriptionNotFoundException("Subscription not found");
        }
        if ( friendship.getUser2().getId().equals(receiver.getId()) ) {
            subscription= Subscription.builder().subscriber(receiver).targetUser(newFriend).build();
                subscriptionService.subscribe(subscription);

            log.info(subscription.getSubscriber().toString());
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.save(friendship);
        }

    }
    public void declineFriendRequest(Long friendRequestId, User receiver) {
        Friendship friendship=findFriendshipByTwoIds(userRepository.findById(friendRequestId).orElse(null),receiver);
        if (friendship==null){
            throw new FriendshipNotFoundException("Friendship not found");
        }
        if ( friendship.getUser2().getId().equals(receiver.getId()) && friendship.getStatus().equals(FriendshipStatus.EXPECTATION)) {
            friendship.setStatus(FriendshipStatus.DECLINED);
            friendshipRepository.save(friendship);
            log.info("заявка принята");
        }
    }
    public void removeFriend(User user, Long friendId) {
        Friendship friendship = findFriendshipByTwoIds(user, userRepository.findById(friendId).
                orElseThrow(() -> new UserNotFoundException("User not found")));
        Subscription subscription= subscriptionService.findByUsers(user,userRepository.findById(friendId).
                orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found")));
        if (friendship != null ) {
            friendshipRepository.delete(friendship);
            subscriptionService.unsubscribe(subscription);
        } else{
            throw new FriendshipNotFoundException("Friendship not found");
        }
    }
    public Friendship findFriendshipByTwoIds(User user1,User user2){
        Friendship friendship = friendshipRepository.findByUser1AndUser2(user1, user2);
        if (friendship == null) {
            friendship = friendshipRepository.findByUser1AndUser2(user2, user1);
        }
        return friendship;
    }

}
