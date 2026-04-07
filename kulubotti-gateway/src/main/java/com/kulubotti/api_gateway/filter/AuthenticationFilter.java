package com.kulubotti.api_gateway.filter;

import com.kulubotti.api_gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ALWAYS let CORS Preflight OPTIONS requests pass through immediately!
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // 2. Let login/register pass without a token
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // For all other requests, demand a token
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("STOP: Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            //  Verify the token and get the username
            jwtUtil.validateToken(token);
            String username = jwtUtil.extractUsername(token);

            //  Wrap the request to inject the secure X-Logged-In-User header
            HttpServletRequestWrapper mutatedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("X-Logged-In-User".equalsIgnoreCase(name)) {
                        return username;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-Logged-In-User".equalsIgnoreCase(name)) {
                        return Collections.enumeration(Collections.singletonList(username));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    names.add("X-Logged-In-User");
                    return Collections.enumeration(names);
                }
            };

            //  Forward the safely mutated request to the MVC router
            filterChain.doFilter(mutatedRequest, response);

        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("STOP: Invalid or expired VIP token");
        }
    }
}