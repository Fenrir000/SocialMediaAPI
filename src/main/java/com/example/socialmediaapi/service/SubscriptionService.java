package com.example.socialmediaapi.service;

import com.example.socialmediaapi.entity.Subscription;
import com.example.socialmediaapi.entity.User;
import com.example.socialmediaapi.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    public Subscription findByUsers(User subscriber, User targetUser){
        log.info(" findByUsers "+subscriber.getId().toString() +"   "+targetUser.getId().toString());
        return subscriptionRepository.findSubscriptionBySubscriberIdAndTargetUserId(subscriber.getId(),targetUser.getId()).orElse(null);
    }
    public void subscribe(Subscription s){
        subscriptionRepository.save(s);
    }
    public void unsubscribe(Subscription s){
        subscriptionRepository.delete(s);
    }
    public List<User> getTargetUsersFromSubscriptions(User subscriber) {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriber(subscriber);
        return subscriptions.stream()
                .map(Subscription::getTargetUser)
                .collect(Collectors.toList());
    }
    }
