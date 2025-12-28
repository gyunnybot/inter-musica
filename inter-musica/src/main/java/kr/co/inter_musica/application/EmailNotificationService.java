package kr.co.inter_musica.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${spring.mail.username:no-reply@inter-musica.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendTeamConfirmedEmail(String to, String teamName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(fromAddress);
        message.setSubject("[Inter Musica] 팀 합류 확정");
        message.setText("\"" + teamName + "\" 팀 합류가 확정되었습니다.\n\n" +
                "팀 톡방에서 팀원들과 대화를 시작해 보세요!");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Failed to send team confirmation email to {}", to, e);
        }
    }
}
