package com.loan.loan_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.List;

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
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              email, null, authorities);
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
