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

        // 1) 정적 리소스, 테스트용 index.html
        if ("/".equals(uri) || "/index.html".equals(uri)) return true;
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")) return true;
        if ("/favicon.ico".equals(uri)) return true;

        // 2) signup, login, logout 에 대하여
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
            printUnauthorized(response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()); // Bearer 이후의 문자열 = 토큰

        // 토큰 유효성 검사
        if (!jwtProvider.validate(token)) {
            printUnauthorized(response);
            return;
        }

        Long userId = jwtProvider.getUserId(token);

        if (userId == null) {
            printUnauthorized(response);
            return;
        }

        // 컨트롤러 또는 서비스가 로그인 유저를 알 수 있게 request attribute로 전달하는 방식
        // 추후 컨트롤러에서는 @RequestAttribute("currentUserId") Long userId 같은 식으로 꺼내 써서 중복 검증 과정을 줄인다
        request.setAttribute("currentUserId", userId);
        filterChain.doFilter(request, response);
    }

    private void printUnauthorized(HttpServletResponse response) throws IOException {
        ErrorCode errorcode = ErrorCode.AUTH_UNAUTHORIZED; // 401

        response.setStatus(errorcode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // application/json

        ApiErrorResponse body = new ApiErrorResponse(
                new ApiError(errorcode.getCode(), errorcode.getMessage())
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
