package kr.co.inter_musica.presentation.security;

import kr.co.inter_musica.presentation.exception.ApiException;
import kr.co.inter_musica.presentation.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        Object principal = auth.getPrincipal();

        // JwtAuthenticationFilter에서 subject(userId)를 String으로 넣었으니 여기서 파싱
        try {
            return Long.parseLong(String.valueOf(principal));
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
        }
    }
}
