package com.example.socialmediaapi.repository;

import com.example.socialmediaapi.entity.Friendship;
import com.example.socialmediaapi.entity.Subscription;
import com.example.socialmediaapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
     Optional<Subscription> findSubscriptionBySubscriberIdAndTargetUserId(Long subscriberId,Long targetUserId);

    List<Subscription> findBySubscriber(User subscriber);

}
