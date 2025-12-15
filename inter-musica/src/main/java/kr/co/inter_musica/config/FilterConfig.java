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
            ObjectMapper objectMapper,
            @Value("${jwt.secret:change-me-please}") String secret,
            @Value("${jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        return new JwtProvider(objectMapper, secret, expirationSeconds);
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        FilterRegistrationBean<AuthFilter> bean = new FilterRegistrationBean<AuthFilter>();
        bean.setFilter(new AuthFilter(jwtProvider, objectMapper));
        bean.setOrder(1);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
