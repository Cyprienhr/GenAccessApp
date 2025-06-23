package com.example.genaccessapp.security.jwt;

import com.example.genaccessapp.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Token blacklist to store invalidated tokens
    private final ConcurrentHashMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    @Value("${genaccessapp.jwt.secret}")
    private String jwtSecret;

    @Value("${genaccessapp.jwt.expirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            // Check if token is blacklisted
            if (isTokenBlacklisted(authToken)) {
                logger.error("JWT token is blacklisted");
                return false;
            }
            
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
    
    
    //  Invalidate a JWT token by adding it to the blacklist
     
    public void invalidateToken(String token) {
        try {
            // Get token expiration date
            Date expiration = Jwts.parserBuilder().setSigningKey(key()).build()
                    .parseClaimsJws(token).getBody().getExpiration();
            
            // Add to blacklist
            blacklistedTokens.put(token, expiration);
            logger.info("Token added to blacklist");
            
            // Clean up expired tokens from blacklist periodically
            cleanupBlacklist();
        } catch (Exception e) {
            logger.error("Error invalidating token: {}", e.getMessage());
        }
    }
    
    
    //  Check if a token is blacklisted
    //  param token The token to check
    //  return true if blacklisted, false otherwise
     
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }
    
    
    //  Clean up expired tokens from the blacklist
    private void cleanupBlacklist() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}