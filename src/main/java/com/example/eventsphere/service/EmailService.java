package com.example.eventsphere.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API = "https://api.brevo.com/v3/smtp/email";

    @Value("${app.brevo.api-key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final RestTemplate rest = new RestTemplate();

    private void send(String to, String subject, String html) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        String body = String.format("""
            {
              "sender": {"name": "EventSphere", "email": "%s"},
              "to": [{"email": "%s"}],
              "subject": "%s",
              "htmlContent": %s
            }""",
            fromEmail, to, subject, toJsonString(html));

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = rest.postForEntity(BREVO_API, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Brevo erreur: " + resp.getBody());
        }
        log.info("Email envoyé via Brevo à {} : {}", to, subject);
    }

    private String toJsonString(String html) {
        return "\"" + html
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "") + "\"";
    }

    // ── OTP Verification Email ─────────────────────────────────────────
    public void envoyerOtpVerification(String destinataire, String nomComplet, String otpCode) throws Exception {
        log.info("══ OTP pour : {}  CODE : {}", destinataire, otpCode);
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f8f7ff;font-family:Segoe UI,Arial,sans-serif;'><div style='max-width:480px;margin:32px auto;background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 4px 32px rgba(0,0,0,0.08);'><div style='background:linear-gradient(135deg,#FF8C42,#FF6F91);padding:32px;text-align:center;'><h1 style='color:#fff;margin:0;font-size:22px;font-weight:800;'>EventSphere</h1><p style='color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:13px;'>Verification de votre compte</p></div><div style='padding:32px;'><p style='color:#111;font-size:16px;font-weight:600;margin:0 0 8px;'>Bonjour " + nomComplet + ",</p><p style='color:#666;font-size:14px;line-height:1.7;margin:0 0 24px;'>Utilisez le code ci-dessous pour verifier votre adresse email et activer votre compte EventSphere.</p><div style='background:linear-gradient(135deg,#fff9f6,#fff3ed);border:2px solid rgba(255,140,66,0.2);border-radius:16px;padding:28px;text-align:center;margin-bottom:24px;'><p style='color:#999;font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:1px;margin:0 0 12px;'>Votre code</p><div style='font-size:42px;font-weight:800;letter-spacing:14px;color:#FF8C42;font-family:Courier New,monospace;'>" + otpCode + "</div><p style='color:#FF8C42;font-size:12px;font-weight:600;margin:12px 0 0;'>Valable 10 minutes</p></div><p style='color:#999;font-size:13px;line-height:1.6;margin:0;'>Si vous n'avez pas cree de compte EventSphere, ignorez cet email.</p></div><div style='background:#fafafa;border-top:1px solid #f0f0f0;padding:20px 32px;text-align:center;'><p style='color:#bbb;font-size:12px;margin:0;'>2025 EventSphere</p></div></div></body></html>";
        send(destinataire, "EventSphere - Votre code de verification : " + otpCode, html);
    }

    // ── Google Welcome Email ───────────────────────────────────────────
    public void envoyerBienvenueGoogle(String destinataire, String nomComplet) throws Exception {
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f8f7ff;font-family:Segoe UI,Arial,sans-serif;'><div style='max-width:480px;margin:32px auto;background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 4px 32px rgba(0,0,0,0.08);'><div style='background:linear-gradient(135deg,#FF8C42,#FF6F91);padding:32px;text-align:center;'><h1 style='color:#fff;margin:0;font-size:22px;font-weight:800;'>EventSphere</h1><p style='color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:13px;'>Bienvenue !</p></div><div style='padding:32px;'><p style='color:#111;font-size:16px;font-weight:600;margin:0 0 8px;'>Bonjour " + nomComplet + ",</p><p style='color:#666;font-size:14px;line-height:1.7;margin:0 0 24px;'>Votre compte EventSphere a ete cree avec succes via Google. Vous pouvez desormais decouvrir et reserver des evenements !</p><div style='text-align:center;margin-bottom:24px;'><a href='" + frontendUrl + "/events' style='display:inline-block;background:linear-gradient(90deg,#FF8C42,#FF6F91);color:#fff;text-decoration:none;padding:14px 36px;border-radius:12px;font-size:15px;font-weight:700;'>Decouvrir les evenements</a></div></div><div style='background:#fafafa;border-top:1px solid #f0f0f0;padding:20px 32px;text-align:center;'><p style='color:#bbb;font-size:12px;margin:0;'>2025 EventSphere</p></div></div></body></html>";
        send(destinataire, "EventSphere - Bienvenue " + nomComplet + " !", html);
    }

    // ── Organizer Credentials Email ────────────────────────────────────
    public void envoyerCredentialsOrganisateur(String destinataire, String nomComplet, String motDePasse) {
        try {
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f8f7ff;font-family:Segoe UI,Arial,sans-serif;'><div style='max-width:480px;margin:32px auto;background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 4px 32px rgba(0,0,0,0.08);'><div style='background:linear-gradient(135deg,#FF8C42,#FF6F91);padding:32px;text-align:center;'><h1 style='color:#fff;margin:8px 0 0;font-size:22px;font-weight:800;'>EventSphere</h1><p style='color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:13px;'>Compte Organisateur</p></div><div style='padding:32px;'><p style='color:#111;font-size:16px;font-weight:600;margin:0 0 8px;'>Bonjour " + nomComplet + ",</p><p style='color:#666;font-size:14px;line-height:1.7;margin:0 0 24px;'>Votre compte organisateur EventSphere a ete cree. Voici vos identifiants :</p><div style='background:#fff9f6;border:2px solid rgba(255,140,66,0.2);border-radius:16px;padding:20px;margin-bottom:24px;'><p style='margin:0 0 10px;color:#666;font-size:13px;'><strong>Email :</strong> <span style='color:#FF8C42;'>" + destinataire + "</span></p><p style='margin:0;color:#666;font-size:13px;'><strong>Mot de passe :</strong> <span style='color:#FF8C42;font-family:monospace;font-size:15px;'>" + motDePasse + "</span></p></div><div style='text-align:center;'><a href='" + frontendUrl + "/login' style='display:inline-block;background:linear-gradient(90deg,#FF8C42,#FF6F91);color:#fff;text-decoration:none;padding:14px 36px;border-radius:12px;font-size:15px;font-weight:700;'>Se connecter</a></div></div><div style='background:#fafafa;border-top:1px solid #f0f0f0;padding:20px 32px;text-align:center;'><p style='color:#bbb;font-size:12px;margin:0;'>2025 EventSphere</p></div></div></body></html>";
            send(destinataire, "EventSphere - Votre compte organisateur a ete cree", html);
        } catch (Exception e) {
            log.error("Erreur envoi credentials organisateur [{}] : {}", destinataire, e.getMessage());
        }
    }

    // ── Password Reset Email ───────────────────────────────────────────
    public void envoyerLienResetPassword(String destinataire, String nomComplet, String token) {
        String lienReset = frontendUrl + "/reset?token=" + token;
        log.info("══ RESET PASSWORD pour : {}  LIEN : {}", destinataire, lienReset);
        try {
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f8f7ff;font-family:Segoe UI,Arial,sans-serif;'><div style='max-width:480px;margin:32px auto;background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 4px 32px rgba(0,0,0,0.08);'><div style='background:linear-gradient(135deg,#FF8C42,#FF6F91);padding:32px;text-align:center;'><h1 style='color:#fff;margin:0;font-size:22px;font-weight:800;'>EventSphere</h1><p style='color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:13px;'>Reinitialisation du mot de passe</p></div><div style='padding:32px;'><p style='color:#111;font-size:16px;font-weight:600;margin:0 0 8px;'>Bonjour " + nomComplet + ",</p><p style='color:#666;font-size:14px;line-height:1.7;margin:0 0 24px;'>Cliquez sur le bouton ci-dessous pour reinitialiser votre mot de passe EventSphere.</p><div style='text-align:center;margin-bottom:24px;'><a href='" + lienReset + "' style='display:inline-block;background:linear-gradient(90deg,#FF8C42,#FF6F91);color:#fff;text-decoration:none;padding:14px 36px;border-radius:12px;font-size:15px;font-weight:700;'>Reinitialiser mon mot de passe</a></div><div style='background:#fafafa;border-radius:10px;padding:14px;margin-bottom:20px;'><p style='color:#999;font-size:12px;margin:0 0 6px;font-weight:600;'>Ou copiez ce lien :</p><p style='color:#FF8C42;font-size:11px;word-break:break-all;margin:0;'>" + lienReset + "</p></div><p style='color:#999;font-size:13px;line-height:1.6;margin:0;'>Ce lien expire dans 1 heure.</p></div><div style='background:#fafafa;border-top:1px solid #f0f0f0;padding:20px 32px;text-align:center;'><p style='color:#bbb;font-size:12px;margin:0;'>2025 EventSphere</p></div></div></body></html>";
            send(destinataire, "EventSphere - Reinitialisation de votre mot de passe", html);
        } catch (Exception e) {
            log.error("Erreur envoi reset password [{}] : {}", destinataire, e.getMessage());
        }
    }
}
