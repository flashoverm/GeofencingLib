package de.geofencing.event;

import java.io.Serializable;

/** Object for describing and transmitting derivations of the Event class 
 * 
 * @author Markus Thral
 *
 */
public class EventListing implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected final int eventID;
	protected final int minor;
	protected final String description;
	protected final String classType;
	protected final Trigger trigger;

	/** Constructor for serialization
	 * 
	 */
	public EventListing(){
		this.eventID = -1;
		this.minor = -1;
		this.description = null;
		this.classType = null;
		this.trigger = null;
	}

	/**	Constructor for creating an EventListing
	 * 
	 * @param eventID EventID of the Event
	 * @param minor Minor of the Geofence
	 * @param description Description of the Event
	 * @param classType Class of the Event derived from Event
	 * @param trigger Trigger of the Event
	 */
	public EventListing(int eventID, int minor, String description, String classType, Trigger trigger) {
		super();
		this.eventID = eventID;
		this.minor = minor;
		this.description = description;
		this.classType = classType;
		this.trigger = trigger;
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

	public String getClassType() {
		return classType;
	}

	public Trigger getTrigger() {
		return trigger;
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
		EventListing other = (EventListing) obj;
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
		return "EventListing [eventID=" + eventID + ", minor=" + minor + ", description=" + description + ", classType="
				+ classType + ", trigger=" + trigger + "]";
	}
	
	
}
