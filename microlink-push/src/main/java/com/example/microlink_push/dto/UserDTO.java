package com.example.microlink_push.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO representing a simplified User, suitable for embedding in other responses.
 * It contains only the public-facing information needed by the frontend.
 */
@Data
public class UserDTO implements Serializable {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
}
