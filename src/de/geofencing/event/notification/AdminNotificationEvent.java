package de.geofencing.event.notification;

import de.geofencing.event.Trigger;
import de.geofencing.log.LogEntry;
import de.geofencing.system.SystemConfiguration;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.system.exceptions.NotFoundException;

/** Event which sends a notification to administration applications via Firebase Messaging Service.
 * Therefore the administrator has to create a Firebase Project 
 * and the administration application has to subscribe FirebaseServerClient.adminTopic
 * 
 * @author Markus Thral
 *
 */
public class AdminNotificationEvent extends NotificationEvent{

	private static final long serialVersionUID = 1L;
		
	/** Constructor for serializing.
	 * 
	 */
	public AdminNotificationEvent(){
		super();
	}
	
	/** Creates Event with a message which is sent to the administration applications
	 * 
	 * @param description Description of the Event
	 * @param minor Minor of the Geofence
	 * @param trigger Trigger object which describes the behavior of the device to trigger the Event
	 * @param title Title of the Notification which is sent to the recipient
	 * @param message Message which is sent to the recipient
	 */
	public AdminNotificationEvent(String description, int minor, Trigger trigger, String title, String message){
		super(description, minor, trigger, title, message);
	}
	
	@Override
	public boolean onAddToGeofence(){
		try{
			SystemConfiguration.getValue(FirebaseServerClient.configAdminKey);
		return true;
		} catch (ConfigurationException e) {
			if (e.getError() == ConfigurationException.Error.FileNotExisiting
					|| e.getError() == ConfigurationException.Error.ValueNotFound) {
				FirebaseServerClient.generateFirebaseConfig();
			}
			LogEntry.c(e);
		}
		return false;
	}
	

	@Override
	protected void trigger(int deviceID) throws NotFoundException{
		String result = FirebaseServerClient.sendToAdmins(title, message);
		if(result != null){
			LogEntry.c("Firebase Message Return:\n" + result);
		}	
	}
}
