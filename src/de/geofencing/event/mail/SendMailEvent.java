package de.geofencing.event.mail;

import javax.mail.internet.AddressException;

import de.geofencing.event.Trigger;
import de.geofencing.event.notification.NotificationEvent;
import de.geofencing.log.LogEntry;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.system.exceptions.NotFoundException;

/** Event which send mail to address if the condition of the Trigger is fulfilled.
 * 
 * On adding the Event to the Geofence a verification mail is sent to the recipient.
 * Only if the recipient confirms the usage of the mail address, mails are sent.
 * 
 * @author Markus Thral
 *
 */
public class SendMailEvent extends NotificationEvent {

	private static final long serialVersionUID = 1L;
	
	private final String recipient;
	
	/** Constructor for serializing.
	 * 
	 */
	public SendMailEvent(){
		super();
		this.recipient = null;
	}
	
	/** Creates Event which sends a mail to the given recipient
	 * 
	 * @param description Description of the Event
	 * @param minor Minor of the Geofence
	 * @param trigger Trigger object which describes the behavior of the device to trigger the Event
	 * @param recipient Recipient of the mail
	 * @param title Subject of the mail
	 * @param message Message body of the mail
	 */
	public SendMailEvent(String description, int minor, Trigger trigger, String recipient, String title, String message){
		super(description, minor, trigger, title, message);
		this.recipient = recipient;
	}

	public String getRecipient() {
		return recipient;
	}

	@Override
	public boolean onAddToGeofence(){
		try{
			return MailInterface.sendDefaultVerificationMail(recipient);
		} catch (ConfigurationException | MailException | AddressException e) {
			LogEntry.c(e);
			return false;
		}
	}

	@Override
	protected void trigger(int deviceID) throws NotFoundException {
		try {
			MailInterface.sendToConfirmedRecipient(
					recipient, 
					title, 
					message);
		} catch (MailException e) {
			LogEntry.c(e);
		}
	}
}
