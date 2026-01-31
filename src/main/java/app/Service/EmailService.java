package app.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String defaultFrom;

    @Async
    public void sendEmailWithTemplate(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(subject);

            // Process template with Thymeleaf
            String htmlContent = templateEngine != null
                    ? templateEngine.process(templateName, context)
                    : generateFallbackEmail(context);
            helper.setText(htmlContent, true);

            System.out.println("Sending Email....");
            mailSender.send(message);
            System.out.println("Sent the Email");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private String generateFallbackEmail(Context context) {
        String userName = (String) context.getVariable("userName");
        String code = (String) context.getVariable("activationCode");

        return String.format(
                "<html><body><h2>Hello %s</h2><p>Your activation code is: <strong>%s</strong></p></body></html>",
                userName, code
        );
    }
}
