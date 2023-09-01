package com.example.socialmediaapi.controller;

import com.example.socialmediaapi.entity.Message;
import com.example.socialmediaapi.entity.MessageResponse;
import com.example.socialmediaapi.entity.User;
import com.example.socialmediaapi.exception.UserNotFoundException;
import com.example.socialmediaapi.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/send/{receiverId}")
    @Operation(description = "Api for  authorized user to send message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> sendMessage(@PathVariable Long receiverId, @RequestBody String content) {
        User sender = getCurrentAuthenticatedUser();
        try {
            messageService.sendMessage(sender, receiverId, content);
            return ResponseEntity.ok("Message sent successfully.");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }

    @GetMapping("/chat/{friendId}")
    @Operation(description = "Api for  authorized user to see his chat with other user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getChatMessages(@PathVariable Long friendId) {
        User user = getCurrentAuthenticatedUser();
      try{  List<MessageResponse> messages = messageService.getChatMessages(user, friendId);
        return ResponseEntity.ok(messages);}
      catch (UserNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
                  .body("User not found");
      }
    }


    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}
