package de.geofencing.event.notification;

import de.geofencing.event.Event;
import de.geofencing.event.Trigger;
import de.geofencing.system.exceptions.NotFoundException;

/** Event for sending String messages to a recipient
 * 
 * @author Markus Thral
 *
 */
public abstract class NotificationEvent extends Event{
	
	private static final long serialVersionUID = 1L;

	protected String title;
	protected String message;
	
	/** Constructor for serializing.
	 * 
	 */
	public NotificationEvent(){
		super();
		this.message = null;
	}

	/** Creates event with title and message. The message can be extended with further 
	 * information (i.e. data about the triggering Device) in the trigger method
	 * 
	 * @param description Description of the Event
	 * @param minor Minor of the Geofence
	 * @param trigger Trigger object which describes the behavior of the device to trigger the Event
 	 * @param title Title or subject of the message 
	 * @param message Message which is sent to the recipient
	 */
	public NotificationEvent(String description, int minor, Trigger trigger, String title, String message){
		super(description, minor, trigger);
		this.title = title;
		this.message = message;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	protected abstract void trigger(int deviceID) throws NotFoundException;

}
