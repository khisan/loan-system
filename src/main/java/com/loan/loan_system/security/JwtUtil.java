package com.loan.loan_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  public String generateToken(String email, String role) {
    return generateToken(email, role, null);
  }

  /** Includes the user id so audit logs never need an extra lookup per request. */
  public String generateToken(String email, String role, UUID userId) {
    return Jwts.builder()
        .setSubject(email)
        .claim("role", role)
        .claim("userId", userId == null ? null : userId.toString())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims extractClaim(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public String extractEmail(String token) {
    return extractClaim(token).getSubject();
  }

  public String extractRole(String token) {
    return extractClaim(token).get("role", String.class);
  }

  /** Null for tokens issued before the actor claim was introduced. */
  public UUID extractUserId(String token) {
    Object claim = extractClaim(token).get("userId");
    if (claim == null) {
      return null;
    }
    try {
      return UUID.fromString(claim.toString());
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  public boolean isTokenExpired(String token) {
    return extractClaim(token).getExpiration().before(new Date());
  }

  public boolean validateToken(String token, String email) {
    try {
      return extractEmail(token).equals(email) && !isTokenExpired(token);
    } catch (ExpiredJwtException e) {
      return false;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}