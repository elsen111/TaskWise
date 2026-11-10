package com.todobackend.service.impl;

import com.todobackend.config.properties.EmailChangeProperties;
import com.todobackend.config.properties.PasswordResetProperties;
import com.todobackend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final PasswordResetProperties properties;
    private final EmailChangeProperties emailChangeProperties;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendPasswordResetOtp(String recipient, String otp) {

        Context context = new Context();

        context.setVariable("otp", otp);
        context.setVariable(
                "expirationMinutes",
                properties.getOtpExpiration().toMinutes()
        );

        String htmlContent = templateEngine.process(
                "email/password-reset-otp",
                context
        );

        var message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(senderEmail);
            helper.setTo(recipient);
            helper.setSubject("Reset your password");

            helper.setText(
                    "Your password reset code is: " + otp
                            + ". It expires in "
                            + properties.getOtpExpiration().toMinutes()
                            + " minutes.",
                    htmlContent
            );

            mailSender.send(message);

        } catch (MessagingException exception) {
            throw new IllegalStateException(
                    "Failed to send password reset email",
                    exception
            );
        }
    }

    @Override
    public void sendEmailChangeVerification(
            String recipient,
            String rawToken
    ) {
        long expirationMinutes =
                emailChangeProperties.getTokenExpiration().toMinutes();

        String verificationUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/email/confirm")
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();

        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("expirationMinutes", expirationMinutes);

        String htmlContent = processTemplate(
                "email/email-change-verification",
                context
        );

        String textContent = """
                You requested to change your email address.

                Verify your new email address using this link:
                %s

                This link expires in %d minutes.
                If you did not request this change, you can ignore this email.
                """.formatted(verificationUrl, expirationMinutes);

        sendEmail(
                recipient,
                "Verify your new email address",
                textContent,
                htmlContent
        );
    }

    @Override
    public void sendEmailChangeNotification(
            String oldEmail,
            String newEmail
    ) {
        Context context = new Context();
        context.setVariable("oldEmail", oldEmail);
        context.setVariable("newEmail", newEmail);

        String htmlContent = processTemplate(
                "email/email-change-notification",
                context
        );

        String textContent = """
                Your account email address has been changed.

                Previous email: %s
                New email: %s

                If you did not make this change, please contact support.
                """.formatted(oldEmail, newEmail);

        sendEmail(
                oldEmail,
                "Your account email address was changed",
                textContent,
                htmlContent
        );
    }

    private String processTemplate(String templateName, Context context) {
        return templateEngine.process(templateName, context);
    }

    private void sendEmail(
            String recipient,
            String subject,
            String textContent,
            String htmlContent
    ) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(senderEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(textContent, htmlContent);

            mailSender.send(message);

        } catch (MessagingException exception) {
            throw new IllegalStateException(
                    "Failed to send email to: " + recipient,
                    exception
            );
        }
    }
}