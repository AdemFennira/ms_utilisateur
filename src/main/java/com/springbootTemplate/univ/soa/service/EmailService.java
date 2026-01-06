package com.springbootTemplate.univ.soa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText(
                    "Bonjour,\n\n" +
                            "Réinitialisez votre mot de passe SmartDish en cliquant sur ce lien :\n\n" +
                            resetLink + "\n\n" +
                            "⏱️ Valide pendant 1 heure\n" +
                            "🔒 Vous n'avez rien demandé ? Ignorez cet email.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe SmartDish\n" +
                            "support@smartdish.com"
            );

            mailSender.send(message);
            log.info("✅ Email de réinitialisation envoyé à : {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation", e);
        }
    }
}