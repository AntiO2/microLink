package com.example.microlink_user.controller;

import com.example.microlink_user.model.User;
import com.example.microlink_user.payload.request.LoginRequest;
import com.example.microlink_user.payload.request.SignupRequest;
import com.example.microlink_user.payload.response.ApiResponse;
import com.example.microlink_user.payload.response.JwtResponse;
import com.example.microlink_user.payload.response.MessageResponse;
import com.example.microlink_user.repository.UserRepository;
import com.example.microlink_user.security.jwt.JwtUtils;
import com.example.microlink_user.security.services.UserDetailsImpl;
import com.example.microlink_user.service.ProcessService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ProcessService processService;

    @Value("${app.jwtExpirationMs}")
    private long jwtExpirationMs;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Update last login time
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
        }

        JwtResponse jwtResponse = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                jwtExpirationMs / 1000); // Return in seconds

        return ResponseEntity.ok(ApiResponse.success(jwtResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));
        
        user.setNickname(signUpRequest.getNickname());

        Set<String> strRoles = signUpRequest.getRole();
        Set<String> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add("ROLE_USER");
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        roles.add("ROLE_ADMIN");
                        break;
                    default:
                        roles.add("ROLE_USER");
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Auto trigger onboarding process
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("applicant", savedUser.getUsername());
            processService.startProcess("user-onboarding", String.valueOf(savedUser.getId()), variables);
        } catch (Exception e) {
            // Log error but do not fail registration
            System.err.println("Failed to start onboarding process: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("User registered successfully!", savedUser));
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(409).body(ApiResponse.error(409, "Username is already taken"));
        }
        return ResponseEntity.ok(ApiResponse.success("Username is available", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(ApiResponse.success("Log out successful!", null));
    }
}
