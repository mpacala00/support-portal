package com.github.mpacala00.supportportal.service;

import com.sun.mail.smtp.SMTPTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static com.github.mpacala00.supportportal.constant.EmailConstant.*;

@Service
@Slf4j
public class EmailService {

    //todo better way to do this may be using gmail api wiht oauth2
    //https://kinsta.com/blog/gmail-smtp-server/

    public void sentNewPasswordEmail(String firstName, String password, String email) throws MessagingException {
        Message msg = null;
        try {
            msg = createMessage(firstName, password, email);
        } catch (MessagingException e) { e.printStackTrace(); }

        if(msg != null) {
            SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(MAIL_TRANSER_PROTOCOL);
            smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
            smtpTransport.sendMessage(msg, msg.getAllRecipients());
            smtpTransport.close();
        } else {
            log.error("Password email could not be sent: message (msg) is null");
        }

    }

    private Message createMessage(String firstName, String password, String email) throws MessagingException {
        String emailText = "Hello " + firstName + ",\n\nYour new account password is: " + password
                + "\n\nThe Support Team";

        Message msg = new MimeMessage(getEmailSession());
        msg.setFrom(new InternetAddress(FROM_EMAIL));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false)); //this is empty
        msg.setSubject(EMAIL_SUBJECT);
        msg.setText(emailText);
        msg.setSentDate(new Date());
        msg.saveChanges();

        return msg;
    }

    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);

        return Session.getInstance(properties, null);
    }
}
