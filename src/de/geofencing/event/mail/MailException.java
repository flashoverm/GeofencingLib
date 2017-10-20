package de.geofencing.event.mail;

/** Could not send mail. For further information see the server log.
 * 
 * @author Markus Thral
 *
 */
public class MailException extends Exception {

	private static final long serialVersionUID = 1L;

	public MailException(){
		super("Mail not sent - See server log");
	}
}

