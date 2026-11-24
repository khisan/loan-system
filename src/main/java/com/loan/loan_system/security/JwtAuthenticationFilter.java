package com.loan.loan_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

import com.loan.loan_system.entity.User;
import com.loan.loan_system.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    final String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      
      final String token = header.substring(7);
      final String email = jwtUtil.extractEmail(token);
      final String role = jwtUtil.extractRole(token);

      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        if (jwtUtil.validateToken(token, email)) {
          List<SimpleGrantedAuthority> authorities = role == null || role.isBlank()
              ? List.of()
              : List.of(new SimpleGrantedAuthority(normalizeRole(role)));

          // Tokens issued before the userId claim existed are resolved by email.
          UUID userId = jwtUtil.extractUserId(token);
          if (userId == null) {
            userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
          }
          Actor actor = new Actor(userId, email, normalizeRole(role));
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              actor, null, authorities);
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

    } catch (Exception e) {
      logger.error("Gagal set user authentication: {}", e);
    }
    filterChain.doFilter(request, response);
  }

  private String normalizeRole(String role) {
    String normalizedRole = role.trim().toLowerCase(Locale.ROOT);
    return normalizedRole.startsWith("role_") ? normalizedRole.substring(5) : normalizedRole;
  }

}
