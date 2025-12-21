package com.example.microlink_user.controller;

import com.example.microlink_user.model.User;
import com.example.microlink_user.payload.request.UpdateProfileRequest;
import com.example.microlink_user.payload.response.ApiResponse;
import com.example.microlink_user.repository.UserRepository;
import com.example.microlink_user.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Not authenticated"));
        }
        
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                User user = userRepository.findById(userDetails.getId()).orElse(null);
                return ResponseEntity.ok(ApiResponse.success(user));
            } else {
                 return ResponseEntity.status(401).body(ApiResponse.error(401, "Anonymous user"));
            }
        } catch (ClassCastException e) {
             return ResponseEntity.status(401).body(ApiResponse.error(401, "Anonymous user"));
        }
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
             return ResponseEntity.status(401).body(ApiResponse.error(401, "Not authenticated"));
        }

        try {
            Object principal = authentication.getPrincipal();
             if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                User user = userRepository.findById(userDetails.getId()).orElse(null);
                
                if (user == null) {
                    return ResponseEntity.status(404).body(ApiResponse.error(404, "User not found"));
                }

                if (request.getNickname() != null) user.setNickname(request.getNickname());
                if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
                if (request.getBio() != null) user.setBio(request.getBio());
                if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

                userRepository.save(user);
                return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
             }
        } catch (Exception e) {
             return ResponseEntity.status(500).body(ApiResponse.error(500, e.getMessage()));
        }
        return ResponseEntity.status(401).body(ApiResponse.error(401, "Not authenticated"));
    }
}
