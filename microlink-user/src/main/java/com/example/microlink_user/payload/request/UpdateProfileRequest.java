package com.example.microlink_user.payload.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 50)
    private String nickname;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 255)
    private String bio;

    @Size(max = 20)
    private String phoneNumber;
}
