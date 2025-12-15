package kr.co.inter_musica.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.inter_musica.auth.jwt.AuthFilter;
import kr.co.inter_musica.auth.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public JwtProvider jwtProvider(
            ObjectMapper objectMapper, /* java object - json 직렬화, 역직렬화 */
            @Value("${jwt.secret:change-me-please}") String secret,
            @Value("${jwt.expiration-seconds:3600}") long expirationSeconds
            // 디폴트 값을 @Value 로 굳이 넣은 이유? 추후 토큰 규약이 달라질 수 있으므로 jwtProvider 에 값 명시
    ) {
        return new JwtProvider(objectMapper, secret, expirationSeconds);
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        FilterRegistrationBean<AuthFilter> bean = new FilterRegistrationBean<AuthFilter>();

        bean.setFilter(new AuthFilter(jwtProvider, objectMapper)); // 토큰 검증 시 AuthFilter 실행
        bean.setOrder(1); // 필터 실행 순서
        bean.addUrlPatterns("/*"); // 모든 과정에 필터를 두고, shouldNotFilter 로 필터 검증 예외 적용

        return bean;
    }
}
