package de.mg.stock.server.alert;

import de.mg.stock.server.Config;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class AlertMailSender {

    private static Logger logger = Logger.getLogger(AlertMailSender.class.getName());

    @Inject
    private Config config;

    public void sendStartupMail() {
        send("alert mail sending initialized", "Initialized...", false);
    }

    public void send(String msg, String subject, boolean html) {

        logger.info(subject + ": " + msg);

        if (!config.isAlertMailInitialized()) {
            logger.warning("alert mail not initialized");
            return;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getGmailUser(), config.getGmailPw());
                    }
                });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getGmailUser()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(config.getAlertMailTo()));
            message.setSubject(subject);
            if (html)
                message.setContent(msg, "text/html; charset=utf-8");
            else
                message.setText(msg);
            Transport.send(message);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "mail sending problem", e);
        }
    }
}
