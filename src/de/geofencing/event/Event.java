package de.geofencing.event;

import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.geofencing.database.GeofencingDBConnector;
import de.geofencing.log.LogEntry;
import de.geofencing.system.exceptions.NotFoundException;

/** The Event class can be derived with instruction which should be executed, 
 * if the users behavior matches with the defined Trigger. 
 * Therefore the instructions has to be implemented in the trigger method. 
 * The Method onAddToGeofence can be overwritten with instructions, 
 * which should be executed, when the event is added to the Geofence.  
 * 
 * The Event is identified by the eventID, described by the description 
 * and is assigned to an Geofence. 
 * ClassType shows the class of the derivation for serializing.
 * 
 * @author Markus Thral
 *
 */
public abstract class Event implements Serializable {

	private static final long serialVersionUID = 1L;

	protected int eventID;
	protected final int minor;
	protected final String description;
	protected final String classType;
	protected final Trigger trigger;

	/** Constructor for serializing.
	 * 
	 */
	public Event() {
		this.minor = -1;
		this.description = null;
		this.classType = null;
		this.trigger = null;
		this.eventID = -1;

	}

	/** Creates new Event an set class as classType
	 * 
	 * @param description Description of the Event
	 * @param minor Minor of the Geofence
	 * @param trigger Trigger object which describes the behavior of the device to trigger the Event
	 */
	public Event(String description, int minor, Trigger trigger) {
		this.description = description;
		this.minor = minor;
		this.classType = getClass().getName();
		this.trigger = trigger;
		this.eventID = -1;
	}

	public int getEventID() {
		return eventID;
	}

	public int getMinor() {
		return minor;
	}

	public String getDescription() {
		return description;
	}

	public Trigger getTrigger() {
		return trigger;
	}
	
	/** Generates EventListing from this event
	 * 
	 * @return EventListing object
	 */
	public EventListing generateListing(){
		return new EventListing(this.eventID, 
				this.minor, this.description, 
				this.classType, this.trigger);
	}

	public void setEventID(int eventID) {
		this.eventID = eventID;
	}

	/** Returns Class of the Event for identifying the Event derivation
	 * 
	 * @return Derivation of the Event as Class
	 * @throws ClassNotFoundException if the class is not exisiting
	 */
	public Class<?> getClassType() throws ClassNotFoundException {
		return Class.forName(classType);
	}

	/** Checks if the conditions of the Trigger is fulfilled and triggers the Event
	 * 
	 * @param direction Direction of the movement of the Device
	 * @param deviceID DeviceID of the updating Device
	 */
	public void checkTrigger(Trigger.Direction direction, int deviceID) {
		if (trigger.getDirection().equals(direction)) {
			if (trigger.getDelay() == 0) {
				try {
					this.trigger(deviceID);
				} catch (NotFoundException e) {
					LogEntry.c(e);
				}
			} else {
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						try {
							if (conditionStillFullfilled(deviceID)) {
								Event.this.trigger(deviceID);
							}
						} catch (NotFoundException e) {
							LogEntry.c(e);
						}
					}
				}, trigger.getDelay() * 1000);
			}
		}
	}

	/** Checks if the triggering condition is still fulfilled
	 * 
	 * @param deviceID DeviceID of the updating Device
	 * @return true if condition is still fulfilled, false if not
	 * @throws NotFoundException if minor or deviceID not existing
	 */
	protected boolean conditionStillFullfilled(int deviceID) throws NotFoundException {
		if (trigger.getDirection().equals(Trigger.Direction.Enter)
				&& GeofencingDBConnector.isDeviceInGeofence(deviceID, minor)) {
			return true;
		} else if (trigger.getDirection().equals(Trigger.Direction.Leave)
				&& !GeofencingDBConnector.isDeviceInGeofence(deviceID, minor)) {
			return true;
		}
		return false;
	}

	/** Must be overwritten with instructions which should be executed if the Event is triggered
	 * 
	 * @param deviceID DeviceID of the triggering Device
	 * @throws NotFoundException if a necessary object is not existing, see exception message for further information
	 */
	protected abstract void trigger(int deviceID) throws NotFoundException;

	/**
	 * Can be overwritten with instructions which should be executed when the
	 * Event is added to the Geofence. If the method returns false, the event 
	 * couldn't be added to the Geofence
	 * 
	 * @return true if adding succeeded, false if not
	 */
	public boolean onAddToGeofence() {
		return true;
	}
	
	/** Deserializes Event or a derivation from a JSON String
	 * 
	 * @param json JSON String containing a serialized Event or a derivation
	 * @return Deserialized Event object or a derivation (has to be casted)
	 * @throws IOException if an I/O exception occurs
	 * @throws ClassNotFoundException if the class type of the JSON String is not existing
	 */
	public static Event jsonToEvent(String json) throws IOException, ClassNotFoundException{
		ObjectMapper mapper = new ObjectMapper();
		String classType = mapper.readTree(json).findValue("classType").asText();
		return (Event)mapper.readValue(json, Class.forName(classType));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classType == null) ? 0 : classType.hashCode());
		result = prime * result + eventID;
		result = prime * result + minor;
		result = prime * result + ((trigger == null) ? 0 : trigger.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (classType == null) {
			if (other.classType != null)
				return false;
		} else if (!classType.equals(other.classType))
			return false;
		if (eventID != other.eventID)
			return false;
		if (minor != other.minor)
			return false;
		if (trigger == null) {
			if (other.trigger != null)
				return false;
		} else if (!trigger.equals(other.trigger))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Event [eventID=" + eventID + ", minor=" + minor + ", description=" + description + ", classType="
				+ classType + ", trigger=" + trigger + "]";
	}
	
	
}
