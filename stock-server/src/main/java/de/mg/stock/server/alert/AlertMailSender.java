package de.mg.stock.server.alert;

import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.Config;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
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

    public void send(Map<StocksEnum, Long> changePercent) {

        String msg = "";
        for (StocksEnum stock : changePercent.keySet()) {
            msg += stock.getName() + " --> " + changePercent.get(stock) + "%\n";
        }
        send(msg);
    }

    public void sendStartupMail() {
        send("alert mail sending initialized");
    }

    private void send(String msg) {

        if (!config.isAlertMailInitialized()) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", config.getSmtpHost());
        Session session = Session.getDefaultInstance(properties);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getAlertMailFrom()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(config.getAlertMailTo()));
            message.setSubject("Stock Alert");
            message.setText(msg);
            Transport.send(message);
            logger.info("sent stock alert:\n" + msg);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "mail sending problem", e);
        }
    }
}
