package de.geofencing.event.mail;

import javax.mail.internet.AddressException;

import de.geofencing.log.LogEntry;
import de.geofencing.service.GeofencingService;
import de.geofencing.system.GeofencingSystem;
import de.geofencing.system.exceptions.NotFoundException;

/** Provides methods to implement the web service send mails and manage mail addresses. 
 * Return values are wrapped in Response object or as HTTP Strings.
 * For access of secured data, the authentication header is evaluated.
 * Errors are handled with the HTTP status codes (Unauthorized, Not Found, ...)
 * 
 * @author Markus Thral
 *
 */
public class MailServiceExtension extends GeofencingService{
	
	/** Creates a new Instance of the service extension
	 * 
	 * @param system GeofencingSystem of this server
	 */
	public MailServiceExtension(GeofencingSystem system){
		super(system);
	}
	
	/** Verifies mail address if the verification mail isn't timed out
	 * 
	 * @param address Mail address to be verified
	 * @return HTTP String with a information message for the recipient
	 */
	public String verifyMail(String address){
		try {
			if(MailDBConnector.confirmMailaddress(address)){
				return "<h2>" + address + " verified" + "</h2>";
			}
			return "<h2>Verification attempt expired for " + address + "</h2>";

		}catch(NotFoundException e){
			return "<h2>No verification attempt found or link expired for " + address + "</h2>";
		}catch (AddressException e) {
			return "<h2>400 Bad Request </br></br> Address " + address + " in wrong format</h2>";
		}catch(Exception e){
			LogEntry.c(e);
			return "<h2>500 Internal Server Error</h2><br><br> Message: " + e.getMessage();
		}
	}
	
	/** Deletes mail address from the list of confirmed addresses
	 * 
	 * @param address Mail address to be removed
	 * @return HTTP String with a information message for the recipient
	 */
	public String unsubscribeMail(String address){
		try{
			MailDBConnector.unregisterMailaddress(address);
			return "<h2> Address " + address + " unsubscribed</h2>";
			
		}catch(NotFoundException e){
			return "<h2> Address " + address + " no found</h2>";
		}catch (AddressException e) {
			return "<h2>400 Bad Request </br></br> Address " + address + " in wrong format</h2>";
		}catch(Exception e){
			LogEntry.c(e);
			return "<h2>500 Internal Server Error</h2><br><br> Message: " + e.getMessage();
		}
	}
}