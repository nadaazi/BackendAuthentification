package com.example.eventsphere.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // Envoi de l'OTP 6 chiffres (image 5 du front - "Check your inbox")
    public void envoyerOtpVerification(String destinataire, String nomComplet, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(destinataire);
            helper.setSubject("EventSphere - Vérification de votre email");

            String html = """
                <div style="font-family: Arial; max-width: 500px; margin: auto;">
                    <h2 style="color: #FF6B35;">EventSphere</h2>
                    <h3>Bonjour %s,</h3>
                    <p>Voici votre code de vérification :</p>
                    <div style="background: #f5f5f5; padding: 20px; text-align: center;
                                font-size: 36px; font-weight: bold; letter-spacing: 10px;
                                color: #FF6B35; border-radius: 8px;">
                        %s
                    </div>
                    <p style="color: #888; margin-top: 15px;">
                        Ce code expire dans <strong>10 minutes</strong>.
                    </p>
                </div>
                """.formatted(nomComplet, otpCode);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email OTP : " + e.getMessage());
        }
    }

    // Envoi du lien de reset password (image 4 du front - "Send Login Link")
    public void envoyerLienResetPassword(String destinataire, String nomComplet, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(destinataire);
            helper.setSubject("EventSphere - Réinitialisation de votre mot de passe");

            String lienReset = frontendUrl + "/reset-password?token=" + token;

            String html = """
                <div style="font-family: Arial; max-width: 500px; margin: auto;">
                    <h2 style="color: #FF6B35;">EventSphere</h2>
                    <h3>Bonjour %s,</h3>
                    <p>Vous avez demandé à réinitialiser votre mot de passe.</p>
                    <p>Cliquez sur le bouton ci-dessous :</p>
                    <a href="%s"
                       style="display: inline-block; background: linear-gradient(to right, #FF6B35, #FF8E53);
                              color: white; padding: 12px 30px; border-radius: 6px;
                              text-decoration: none; font-size: 16px; margin: 15px 0;">
                        Réinitialiser le mot de passe
                    </a>
                    <p style="color: #888;">Ce lien expire dans <strong>1 heure</strong>.</p>
                    <p style="color: #888;">Si vous n'avez pas fait cette demande, ignorez cet email.</p>
                </div>
                """.formatted(nomComplet, lienReset);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email reset : " + e.getMessage());
        }
    }
}
