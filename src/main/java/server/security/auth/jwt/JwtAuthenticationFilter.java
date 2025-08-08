package server.security.auth.jwt;

import server.security.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final List<String> permitUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Pre-flight 요청은 허용
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtService.resolveAndValidateToken(request);
        Authentication authentication = jwtService.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return permitUrl.stream().anyMatch(uri -> requestURI.startsWith(uri.replace("/**", "")));
    }
}
