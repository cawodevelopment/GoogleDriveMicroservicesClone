package com.io.github.cawodevelopment.authentication.auth;

import com.io.github.cawodevelopment.authentication.auth.dto.AuthRequest;
import com.io.github.cawodevelopment.authentication.auth.security.CustomUserDetails;
import com.io.github.cawodevelopment.authentication.user.UserRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.PrivateKey;
import java.util.Date;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final long expirationMs;

    public JwtService(
            PrivateKey privateKey,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.privateKey = privateKey;
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails, Long userId) {
        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userId)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
}
