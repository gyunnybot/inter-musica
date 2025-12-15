package kr.co.inter_musica.auth.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.inter_musica.dto.ApiError;
import kr.co.inter_musica.dto.ApiErrorResponse;
import kr.co.inter_musica.exception.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public AuthFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1) 정적 리소스 / 화면은 인증 제외
        if ("/".equals(uri) || "/index.html".equals(uri)) return true;
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")) return true;
        if ("/favicon.ico".equals(uri)) return true;

        // 파일 확장자 있는 요청은 보통 정적 리소스 (ex: .html .css .js .png ...)
        if (uri.contains(".")) return true;

        // 2) 공개 API
        if (uri.startsWith("/auth/")) return true;

        // 3) 공개 GET /teams: "검색 / 상세 / 멤버 목록"만 공개
        if ("GET".equals(method)) {
            // 팀 검색: GET /teams
            if ("/teams".equals(uri)) return true;

            // 팀 상세: GET /teams/{teamId}
            if (uri.matches("^/teams/\\d+$")) return true;

            // 팀 멤버 목록: GET /teams/{teamId}/members (공개 유지 시)
            if (uri.matches("^/teams/\\d+/members$")) return true;
        }

        // 그 외는 모두 인증 필요
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        if (!jwtProvider.validate(token)) {
            writeUnauthorized(response);
            return;
        }

        Long userId = jwtProvider.getUserId(token);
        if (userId == null) {
            writeUnauthorized(response);
            return;
        }

        request.setAttribute("currentUserId", userId);
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        ErrorCode ec = ErrorCode.AUTH_UNAUTHORIZED;
        response.setStatus(ec.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse body = new ApiErrorResponse(new ApiError(ec.getCode(), ec.getMessage()));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
