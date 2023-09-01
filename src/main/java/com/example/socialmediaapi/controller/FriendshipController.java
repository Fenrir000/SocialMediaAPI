package com.example.socialmediaapi.controller;

import com.example.socialmediaapi.entity.User;
import com.example.socialmediaapi.exception.ForbiddenException;
import com.example.socialmediaapi.exception.FriendshipNotFoundException;
import com.example.socialmediaapi.exception.SubscriptionNotFoundException;
import com.example.socialmediaapi.exception.UserNotFoundException;
import com.example.socialmediaapi.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/friendship")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FriendshipController {


    private  final FriendshipService friendshipService;

    @PostMapping("/send-request/{targetUserId}")
    @Operation(description = "Send friend request to another user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> sendFriendRequest(@PathVariable Long targetUserId) {
        User sender = getCurrentAuthenticatedUser();

        try {
            friendshipService.sendFriendRequest(sender, targetUserId);
            return ResponseEntity.ok("Friend request sent successfully.");
        }  catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }

    @PostMapping("/accept-request/{newFriendId}")
    @Operation(description = "Api for  authorized user to accept friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request accepted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "404", description = "Friendship not found"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long newFriendId) {
        User receiver = getCurrentAuthenticatedUser();
        try {
            friendshipService.acceptFriendRequest(newFriendId, receiver);
            return ResponseEntity.ok("Friend request accepted.");
        }catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        } catch (FriendshipNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Friendship not found");
        } catch (SubscriptionNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subscription not found");
        }
    }

    @PostMapping("/decline-request/{friendRequestId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request declined."),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
    })
    @Operation(description = "Api for  authorized user to decline friend request")
    public ResponseEntity<String> declineFriendRequest(@PathVariable Long friendRequestId) {
        User receiver = getCurrentAuthenticatedUser();
        try{friendshipService.declineFriendRequest(friendRequestId, receiver);
        return ResponseEntity.ok("Friend request declined.");}
        catch (FriendshipNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Friendship not found");
        }
    }

    @PostMapping("/remove-friend/{friendId}")
    @Operation(description = "Api for  authorized user to remove friend ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend removed."),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "404", description = "Friendship not found"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<String> removeFriend(@PathVariable Long friendId) {
        User user = getCurrentAuthenticatedUser();
        try {
        friendshipService.removeFriend(user, friendId);
        return ResponseEntity.ok("Friend removed.");
        }catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        } catch (FriendshipNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Friendship not found");
        } catch (SubscriptionNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Subscription not found");
        }
    }

    // Метод для получения текущего аутентифицированного пользователя
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}