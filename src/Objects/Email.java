package Objects;


import Exceptions.MessageNotSentException;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;


/**
 * Created by Myles on 10/6/17.
 */
public class Email {

    private String emailAddress;
    private String emailPassword;

    private final int SMTP = 0;
    private final int IMAP = 1;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public void openCredentials()
    {
        ObjectIO objectIO = new ObjectIO(new File("credentials.crd"));
        String[] credentials = (String[])objectIO.readObject();
        setEmailAddress(credentials[0]);
        setEmailPassword(credentials[1]);
    }

    public void writeCredentials()
    {
        ObjectIO objectIO = new ObjectIO(new File("credentials.crd"));
        String[] credentials = {getEmailAddress(), getEmailPassword()};
        objectIO.writeObject(credentials);
        emailAddress = credentials[0];
        emailPassword = credentials[1];
    }

    public Session login(int smtpOrImap)
    {
        Properties props = new Properties();

        if(smtpOrImap == SMTP) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.office365.com");
            props.put("mail.smtp.port", "587");
        }
        else if(smtpOrImap == IMAP)
        {
            props.put("mail.imap.auth", "true");
            props.put("mail.imap.starttls.enable", "true");
            props.put("mail.imap.host", "outlook.office365.com");
            props.put("mail.smtp.port", "993");
        }

        javax.mail.Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(getEmailAddress(), getEmailPassword());
                    }
                });
        return session;
    }

    public void sendEmail(ArrayList<String> emailAddressArrayList) throws MessageNotSentException {
        try {
            InternetAddress[] addresses = new InternetAddress[emailAddressArrayList.size()];
            for (int i = 0; i < emailAddressArrayList.size(); i++) {
                addresses[i] = new InternetAddress(emailAddressArrayList.get(i));
            }
            Session session = login(SMTP);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("postoffice@kennesaw.edu", "KSU Post Office"));
            message.setRecipients(Message.RecipientType.TO,
                    addresses);
            message.setSubject("Letter");
            //message.setContent("You have a letter", "text/html");
            message.setText("");
            Transport.send(message);
            storeMail(message);
        } catch (AddressException e) {
            e.printStackTrace();
            throw new MessageNotSentException();
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            throw new MessageNotSentException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MessageNotSentException();
        }
    }

    public void storeMail(Message message) throws MessagingException {
        Session session = login(IMAP);
        Store store = session.getStore("imap");
        store.connect("outlook.office365.com", getEmailAddress(), getEmailPassword());
        Folder folder = store.getFolder("Sent");
        folder.open(Folder.READ_WRITE);
        message.setFlag(Flags.Flag.SEEN, true);
        folder.appendMessages(new Message[] {message});

        store.close();
    }



    public static void main(String[] args) throws MessageNotSentException {
        Email email = new Email();
        email.openCredentials();
        ArrayList<String> emails = new ArrayList<>(Arrays.asList("mandre3@students.kennesaw.edu"));
        email.sendEmail(emails);

    }
}
