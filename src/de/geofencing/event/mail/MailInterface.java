package de.geofencing.event.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.geofencing.log.LogEntry;
import de.geofencing.system.SystemConfiguration;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.system.exceptions.NotFoundException;

/** Provides methods to send mails
 * Uses JavaMail (Needs mail.jar Version 1.4.7 or newer)
 * 
 * @author Markus Thral
 *
 */
public class MailInterface {
	
	/** Mail encryption method TLS or SSL
	 */
	public enum EncryptMethod {
		SSL, TLS
	};

	/** Sets up configuration for sending mails with set values in the Configuration
	 * 
	 * @return Properties object with configuration
	 * @throws ConfigurationException if configuration not found or necessary values not set
	 */
	protected static Properties setupMailserverConfig() throws ConfigurationException {
		int port = Integer.parseInt(SystemConfiguration.getValue("mailPort"));
		EncryptMethod method = EncryptMethod.valueOf(SystemConfiguration.getValue("mailEncryptMethod"));

		Properties mailServerConfig = new Properties();
		mailServerConfig.put("mail.smtp.auth", "true");
		mailServerConfig.put("mail.smtp.host", SystemConfiguration.getValue("mailHost"));
		mailServerConfig.put("mail.smtp.port", port);

		if (method == EncryptMethod.TLS) {
			mailServerConfig.put("mail.smtp.starttls.enable", "true");
		}
		if (method == EncryptMethod.SSL) {
			mailServerConfig.put("mail.smtp.socketFactory.port", port);
			mailServerConfig.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}
		return mailServerConfig;
	}

	/** Generates all fields necessary for the mail configuration
	 * 
	 */
	public static void generateMailConfig() {
		SystemConfiguration.setValue("serviceURL", "");
		SystemConfiguration.setValue("mailHost", "");
		SystemConfiguration.setValue("mailPort", "");
		SystemConfiguration.setValue("mailEncryptMethod", "");
		SystemConfiguration.setValue("mailUsername", "");
		SystemConfiguration.setValue("mailPassword", "");
		SystemConfiguration.setValue("senderAddress", "");
		SystemConfiguration.setValue("senderName", "");
		SystemConfiguration.setValue("confirmationTimeout", "");
		SystemConfiguration.setValue("deleteTimeout", "");
		
		LogEntry.c("Added firebase configuration");
	}

	// Mail Sending

	/** Sends mail. Address has to be confirmed
	 * 
	 * @param recipient Mail address of the recipient
	 * @param subject Subject of the mail
	 * @param text Body of the mail
	 * @return true if mail is sent
	 * @throws NotFoundException if given address is not confirmed
	 * @throws MailException if mail couldn't be sent, see server log
	 */
	public static boolean sendToConfirmedRecipient(String recipient, String subject, String text)
			throws NotFoundException, MailException {
		MailDBConnector.isMailAddressConfirmed(recipient);
		return send(recipient, subject, text);
	}

	/** Sends mail to recipient
	 * 
	 * @param recipient Mail address of the recipient
	 * @param subject Subject of the mail
	 * @param text Body of the mail
	 * @return true if mail is sent
	 * @throws MailException if mail couldn't be sent, see server log
	 */
	protected static boolean send(String recipient, String subject, String text) throws MailException {
		try {
			String username = SystemConfiguration.getValue("mailUsername");
			String password = SystemConfiguration.getValue("mailPassword");
			Session session = Session.getInstance(setupMailserverConfig(), new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(SystemConfiguration.getValue("senderAddress"),
					SystemConfiguration.getValue("senderName")));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			message.setSubject(subject);
			message.setText(text);
			Transport.send(message);

			return true;

		} catch (MessagingException e) {
			LogEntry.c(e);
		} catch (UnsupportedEncodingException e) {
			LogEntry.c(e);
		} catch(ConfigurationException e){
			if(e.getError() == ConfigurationException.Error.FileNotExisiting
					|| e.getError() == ConfigurationException.Error.ValueNotFound) {
				generateMailConfig();
			} else{
				LogEntry.c(e);
			}
		}
		throw new MailException();
	}
	
	/** Sends verification mail to given recipient and updates database entries
	 * 
	 * @param recipient Mail address of the recipient
	 * @param subject Subject of the mail
	 * @param verificationText Body of the verification mail
	 * @return true if mail is sent, false if there's a valid entry for the address
	 * @throws ConfigurationException if configuration not found or necessary values not set
	 * @throws MailException if mail couldn't be sent, see server log
	 * @throws AddressException if address of the recipient is malformed
	 */
	public static boolean sendVerificationMail(String recipient, String subject, String verificationText) 
			throws ConfigurationException, MailException, AddressException{
		if(MailDBConnector.addAddressOnHold(recipient)){
			try{
				return send(recipient, subject, verificationText);
			}catch(MailException e){
				try {
					MailDBConnector.removeOnHoldMail(recipient);
				} catch (NotFoundException e1) {}
				throw new MailException();
			}
		}
		return false;
	}

	/** Sends default verification mail to given recipient
	 * 
	 * @param recipient Mail address of the recipient
	 * @return true if mail is sent, false if there's a valid entry for the address
	 * @throws ConfigurationException if configuration not found or necessary values not set
	 * @throws MailException if mail couldn't be sent, see server log
	 * @throws AddressException if address of the recipient is malformed
	 */
	public static boolean sendDefaultVerificationMail(String recipient) 
			throws ConfigurationException, MailException, AddressException {
		int confirmationTimeout = Integer.parseInt(SystemConfiguration.getValue("confirmationTimeout"))/60;
		String subject = "E-Mail Verification Request";
		String verificationText = "Please click the following link to verifiy your mail address (" + recipient
				+ "): \n\n\n" + getVerificationLink(recipient) + "\n\n\n"
				+ "This Link expiers in " + confirmationTimeout + " minutes"
				+ "\n\n Unsubscribe here: " + getUnsubscribeLink(recipient);

		return sendVerificationMail(recipient, subject, verificationText);
	}
	
	/** Creates link to verify mail address
	 * 
	 * @param recipient Mail address of the recipient
	 * @return Link to verifiy the given mail address
	 * @throws ConfigurationException if configuration not found or necessary values not set
	 */
	public static String getVerificationLink(String recipient) throws ConfigurationException{
		return getServiceURL() + "verification/" + recipient;
	}
	
	/** Creates link to unsubscribe an mail address
	 * 
	 * @param recipient Mail address of the recipient
	 * @return Link to unsubscribe the given mail address
	 * @throws ConfigurationException if configuration not found or necessary values not set
	 */
	public static String getUnsubscribeLink(String recipient) throws ConfigurationException{
		return getServiceURL() + "unsubscribe/" + recipient;
	}
	
	/** Gets URL of the web service from the Configuration
	 * 
	 * @return address of the service
	 * @throws ConfigurationException if configuration not found or necessary values not set
	 */
	public static String getServiceURL() throws ConfigurationException {
		String serviceURL = SystemConfiguration.getValue("serviceURL");
		if(serviceURL.charAt(serviceURL.length()-1) == '/'){
			serviceURL = serviceURL + "/";
		}
		return serviceURL;
	}
}
