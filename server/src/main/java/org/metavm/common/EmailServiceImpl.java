package org.metavm.common;

//import jakarta.mail.*;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeBodyPart;
//import jakarta.mail.internet.MimeMessage;
//import jakarta.mail.internet.MimeMultipart;

import org.metavm.entity.natives.StdFunction;
import org.metavm.context.Component;
import org.metavm.context.Value;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class EmailServiceImpl implements EmailService {

    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public EmailServiceImpl(@Value("${smtp.host}") String host,
                            @Value("${smtp.port}") int port,
                            @Value("${smtp.username}") String username,
                            @Value("${smtp.password}") String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        StdFunction.setEmailSender(((recipient, subject, content) -> {
            EXECUTOR.execute(() -> send(recipient, subject, content));
        }));
    }

    @Override
    public void send(String recipient, String subject, String content) {
//        Properties prop = new Properties();
//        prop.put("mail.smtp.auth", "true");
//        prop.put("mail.smtp.starttls.enable", "true");
//        prop.put("mail.smtp.host", host);
//        prop.put("mail.smtp.port", Integer.toString(port));
//        prop.put("mail.smtp.ssl.trust", host);
//        prop.put("mail.smtp.socketFactory.port", Integer.toString(port));
//        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//
//        Session session = Session.getInstance(prop, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(username, password);
//            }
//        });
//
//        try {
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(username));
//            message.setRecipients(
//                    Message.RecipientType.TO, InternetAddress.parse(recipient));
//            message.setSubject(subject);
//            MimeBodyPart mimeBodyPart = new MimeBodyPart();
//            mimeBodyPart.setContent(content, "text/html; charset=utf-8");
//            Multipart multipart = new MimeMultipart();
//            multipart.addBodyPart(mimeBodyPart);
//            message.setContent(multipart);
//            Transport.send(message);
//        } catch (MessagingException e) {
//            throw new InternalException("Fail to send email", e);
//        }
    }

}
