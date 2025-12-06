package com.autopilot.worker.notification;

import com.autopilot.config.logging.AppLogger;
import com.autopilot.models.payload.Contact;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final AppLogger log = new AppLogger(LoggerFactory.getLogger(EmailService.class));
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    // Recipient (admin) - change via application.yml or environment
    @Value("${app.mail.to}")
    private String toEmail;

    @Value("${app.mail.to-name}")
    private String toName;

    /**
     * Send an email using simple inline HTML template built from Contact.
     * Keeps same method signature as before.
     */
    public void sendEmail(Contact contact) {
        String senderName = safe(contact.getName());
        String senderEmail = safe(contact.getEmail());
        String subject = safe(contact.getSubject());
        String message = safe(contact.getMessage());

        log.info("Preparing to send email from '{}' <{}>", senderName, senderEmail);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = getMessageHelper(mime, subject);

            // Simple HTML body (you can replace with Thymeleaf/FreeMarker later)
            String html = buildHtmlBody(senderName, senderEmail, subject, message);

            helper.setText(html, true); // true = HTML

            mailSender.send(mime);

            log.info("Email sent successfully to {} (from: {} <{}>)", toEmail, fromName, fromEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}. Error: {}", toEmail, e.getMessage(), e);
            // Choose behavior: swallow and log (ack), or throw to let listener handle requeue/DLQ.
            // Here we throw a runtime exception so the Rabbit listener can decide (requeue/DLQ).
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private MimeMessageHelper getMessageHelper(MimeMessage mime, String subject) throws MessagingException, UnsupportedEncodingException {
        MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");

        // set From with friendly name if possible
        try {
            helper.setFrom(new InternetAddress(fromEmail, fromName));
        } catch (Exception ex) {
            // fallback to simple from
            helper.setFrom(fromEmail);
        }

        helper.setTo(new InternetAddress(toEmail, toName));
        helper.setSubject("Contact Message: " + (subject.isEmpty() ? "(no subject)" : subject));
        return helper;
    }

    // small helpers
    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String nl2br(String s) {
        return s == null ? "" : s.replace("\n", "<br/>").replace("\r", "");
    }

    private String buildHtmlBody(String name, String email, String subject, String message) {
        String safeName = escapeHtml(safe(name));
        String safeEmail = escapeHtml(safe(email));
        String safeSubject = escapeHtml(safe(subject));
        String safeMessage = nl2br(escapeHtml(safe(message)));
        String mailtoSubject = urlEncode(safeSubject);

        StringBuilder sb = new StringBuilder(4096);
        sb.append("<!doctype html>")
                .append("<html lang=\"en\">")
                .append("<head>")
                .append("<meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
                .append("<style>")
                .append("body { margin:0; padding:0; background:#f2f5f9; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; color:#111; }")
                .append("a { color: #1f6feb; text-decoration: none; }")
                .append(".wrap { width:100%; padding:24px 12px; box-sizing:border-box; display:flex; justify-content:center; }")
                .append(".card { width:100%; max-width:680px; background:#ffffff; border-radius:14px; overflow:hidden; box-shadow:0 10px 30px rgba(20,30,50,0.08); }")
                .append(".hero { background: linear-gradient(90deg,#6b8cff 0%, #8a6bff 50%, #00c1ff 100%); color:#fff; padding:20px 28px; display:flex; gap:16px; align-items:center; }")
                .append(".badge { width:56px; height:56px; border-radius:12px; background:rgba(255,255,255,0.12); display:flex; align-items:center; justify-content:center; font-size:22px; font-weight:700; box-shadow:0 4px 14px rgba(11,22,60,0.12); }")
                .append(".hero-title { font-size:18px; font-weight:700; margin:0; }")
                .append(".hero-sub { margin:0; font-size:13px; opacity:0.95; }")
                .append(".content { padding:22px 28px; }")
                .append(".meta { display:flex; gap:12px; flex-wrap:wrap; margin-bottom:14px; }")
                .append(".meta .item { background:#f6f8fb; padding:8px 12px; border-radius:8px; font-size:13px; color:#333; box-shadow: inset 0 -1px 0 rgba(255,255,255,0.6); }")
                .append(".subject { font-size:15px; font-weight:600; margin:6px 0 14px; color:#0b2040; }")
                .append(".message-box { background: linear-gradient(180deg,#ffffff,#fbfdff); border-radius:10px; padding:18px; border:1px solid rgba(14,30,60,0.04); font-size:15px; line-height:1.6; color:#1b2b4a; white-space:pre-wrap; }")
                .append(".cta-wrap { display:flex; justify-content:flex-end; margin-top:18px; }")
                .append(".btn { background: linear-gradient(90deg,#3b82f6,#7c3aed); color:#fff; padding:10px 16px; border-radius:8px; font-weight:600; font-size:14px; display:inline-block; box-shadow:0 6px 18px rgba(59,130,246,0.18); }")
                .append(".footer { padding:16px 22px; font-size:12px; color:#75809a; background:#fbfdff; border-top:1px solid rgba(11,22,60,0.03); text-align:center; }")
                .append(".brand { font-weight:700; color:#0b2040; }")
                .append("@media (max-width:480px) { .hero { padding:16px; gap:12px; } .hero-title { font-size:16px; } .content { padding:16px; } .badge { width:48px; height:48px; font-size:20px; border-radius:10px; } }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"wrap\">")
                .append("<div class=\"card\" role=\"article\" aria-label=\"New contact message\">")
                .append("<div class=\"hero\"><div class=\"badge\">✉️</div><div><p class=\"hero-title\">New contact message received</p><p class=\"hero-sub\">A visitor sent a message via your website contact form</p></div></div>")
                .append("<div class=\"content\">")
                .append("<div class=\"meta\" aria-hidden=\"true\">")
                .append("<div class=\"item\"><strong>From</strong><div>").append(safeName).append("</div></div>")
                .append("<div class=\"item\"><strong>Email</strong><div>").append(safeEmail).append("</div></div>")
                .append("</div>")
                .append("<div class=\"subject\">📌 ").append(safeSubject).append("</div>")
                .append("<div class=\"message-box\">").append(safeMessage).append("</div>")
                .append("<div class=\"cta-wrap\"><a class=\"btn\" href=\"mailto:").append(safeEmail).append("?subject=").append(mailtoSubject).append("\">Reply to sender</a></div>")
                .append("</div>")
                .append("<div class=\"footer\"><div><span class=\"brand\">SoloPilot</span> • New message notification</div><div style=\"margin-top:6px;\">If this message looks suspicious, please verify sender before replying.</div></div>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return sb.toString();
    }

    /* helper: simple url-encode for subject in mailto (keeps minimal deps) */
    private static String urlEncode(String s) {
        if (s == null) return "";
        try {
            return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }

}
