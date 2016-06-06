package de.mg.stock.server.alert;

import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.Config;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class AlertMailSender {

    private static Logger logger = Logger.getLogger(AlertMailSender.class.getName());

    @Inject
    private Config config;

    public void send(Map<StocksEnum, Long> changePercent, String subject) {

        String msg = "";
        for (StocksEnum stock : changePercent.keySet()) {
            msg += stock.getName() + " --> " + changePercent.get(stock) + "%\n";
        }
        send(msg, subject);
    }

    public void sendStartupMail() {
        send("alert mail sending initialized", "Initialized...");
    }

    private void send(String msg, String subject) {

        logger.info(subject + ": " + msg);

        if (!config.isAlertMailInitialized()) {
            logger.warning("alert mail not initialized");
            return;
        }

        logger.info("going to send mail:\n" + msg);

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
            message.setText(msg);
            Transport.send(message);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "mail sending problem", e);
        }
    }
}
