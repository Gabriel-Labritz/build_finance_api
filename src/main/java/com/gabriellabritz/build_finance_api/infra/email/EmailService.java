package com.gabriellabritz.build_finance_api.infra.email;

import com.gabriellabritz.build_finance_api.domain.auth.account_verification.EmailVerificationToken;
import com.gabriellabritz.build_finance_api.domain.user.User;
import com.gabriellabritz.build_finance_api.infra.exceptions.email.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    @Value("${app.email.origin}")
    private String ORIGIN_EMAIL;

    @Value("${app.email.sender-name}")
    private  String SEND_NAME;

    @Value("${app.url.site}")
    private String URL_SITE;

    @Value("classpath:templates/email-verification-template.html")
    private Resource emailVerificationTemplate;

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendEmail(String userEmail, String subject, String bodyEmail) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(ORIGIN_EMAIL, SEND_NAME);
            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(bodyEmail, true);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            throw new EmailSendException("Erro ao enviar email", ex.getCause());
        }

        javaMailSender.send(message);
    }

    public void sendEmailVerification(User user, EmailVerificationToken emailVerificationToken) {
        String verificationUrl = URL_SITE.concat("/account/verify-account?token=").concat(emailVerificationToken.getToken());
        String subject = "Build Finance - Verifique sua conta";

        String bodyEmail = loadTemplate(emailVerificationTemplate)
                .replace("{userName}", user.getName())
                .replace("{verificationUrl}", verificationUrl);

        sendEmail(user.getEmail(), subject, bodyEmail);
    }

    private String loadTemplate(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new EmailSendException("Erro ao carregar template", ex.getCause());
        }
    }
}
