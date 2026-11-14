package com.io.github.cawodevelopment.authentication.auth;

import com.io.github.cawodevelopment.authentication.auth.dto.AuthRequest;
import com.io.github.cawodevelopment.authentication.auth.security.CustomUserDetails;
import com.io.github.cawodevelopment.authentication.user.User;
import com.io.github.cawodevelopment.authentication.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(AuthRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "An account with that email already exists");
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return jwtService.generateToken(
                new CustomUserDetails(savedUser),
                savedUser.getId()
        );
    }

    public String login(AuthRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return jwtService.generateToken(
                new CustomUserDetails(user),
                user.getId()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
