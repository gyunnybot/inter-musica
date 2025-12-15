package kr.co.inter_musica.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AppBeansConfig {

    // signup, login 시 사용할 BCryptPasswordEncoder 를 bean 으로 등록해서 DI
    // 나중에 다른 인코더로 교체한다면 @Bean 만 바꾸면 된다
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
