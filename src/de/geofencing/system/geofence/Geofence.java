package de.geofencing.system.geofence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.geofencing.event.Event;
import de.geofencing.event.EventList;
import de.geofencing.event.Trigger;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.exceptions.NotFoundException;

/** Represents a Geofence with its Events and SystemBeacons
 * The minor is the identifier and the Geofence is described by description
 * 
 * @author Markus Thral
 *
 */
public class Geofence implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int minor;
	private String description;
	
	private SystemBeacons beacons;
	private List<Event> events;
		
	/** Constructor for serializing
	 * 
	 */
	public Geofence(){
		this.minor = 0;
		this.description = null;
		beacons = null;
		events = null;
	}
	
	/** Creates Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param description Description of the Geofence
	 */
	public Geofence(int minor, String description){
		this.minor = minor;
		this.description = description;
		
		this.beacons = new SystemBeacons();
		this.events = new ArrayList<Event>();
	}
	
	public int getMinor(){
		return minor;
	}
	
	public String getDescription(){
		return description;
	}
	
	/** Generates GeofenceListing for this Geofence
	 * 
	 * @return GeofenceListing from this Geofence
	 */
	public GeofenceListing generateListing(){
		return new GeofenceListing(this.minor, this.description);
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	
	/*
	 * Generate Beacon
	 */
	
	/** Returns next available major for a new SystemBeacon
	 * 
	 * @return next available major
	 */
	private int nextMajor(){
		int nextMajor = 1;
		for(SystemBeacon beacon : beacons){
			if(beacon.getMajor() >= nextMajor){
				nextMajor = beacon.getMajor() + 1;
			}
		}
		return nextMajor;
	}
	
	/** Generates new SystemBeacon for the Geofence 
	 * but does not add it
	 * 
	 * @param uuid UUID of the system
	 * @return generated SystemBeacon
	 */
	public SystemBeacon generateBeacon(UUID uuid){
		return new SystemBeacon(uuid, this.nextMajor(), minor);
	}
	
	
	/*
	 *  Manage Beacons
	 */
	
	/** Returns the beacons of the Geofence
	 * 
	 * @return list of beacons as SystemBeacons object
	 */
	public SystemBeacons getBeacons(){
		return beacons;
	}
	
	/** Returns beacon with given major
	 * 
	 * @param major Major of the beacon
	 * @return SystemBeacon Beacon with the given major 
	 * @throws NotFoundException if major not existing
	 */
	public SystemBeacon getBeacon(int major) throws NotFoundException{
		return beacons.getBeacon(major, minor);
	}
	
	/** Adds given SystemBeacon to the system
	 *  (UUID and minor has to be checked!)
	 * 
	 * @param beacon SystemBeacon to be added
	 * @return true if adding succeeded, false if not
	 */
	public boolean addBeacon(SystemBeacon beacon){
		return beacons.addBeacon(beacon);
	}
	
	/*
	 * Manage Events
	 */
	
	/** Gets Event with the given eventID
	 * 
	 * @param eventID EventID of the event
	 * @return Event object with the given eventID 
	 * @throws NotFoundException if eventID not existing
	 */
	public Event getEvent(int eventID) throws NotFoundException{
		for(Event event : events){
			if(event.getEventID() == eventID){
				return event;
			}
		}
		throw new NotFoundException("Event " + eventID);
	}
	
	/** Gets list of events
	 * 
	 * @return List of events
	 */
	public List<Event> getEvents() {
		return events;
	}
	
	
	/** Gets Events as EventList object
	 * 
	 * @return Events of the Geofence as EventList object
	 */
	public EventList getEventList() {
		EventList eventList = new EventList();
		for(Event event : events){
			eventList.addEventListing(event.generateListing());
		}
		return eventList;
	}
		
	/** Adds Event to the Geofence 
	 * 
	 * @param event Event object
	 * @return true if Event is added, false if not
	 */
	public boolean addEvent(Event event){
		return this.events.add(event);
	}
	
	/** Triggers every Event of the Geofence with the given direction
	 *
	 * @param direction of the Event (Enter oder Leave)
	 * @param deviceID of the triggering Device
	 * @throws NotFoundException if deviceID not existing
	 */
	public void trigger(Trigger.Direction direction, int deviceID) throws NotFoundException{
		for(Event event:events){
			event.checkTrigger(direction, deviceID);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + minor;
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
		Geofence other = (Geofence) obj;
		if (minor != other.minor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Geofence [minor=" + minor + ", description=" + description + "]";
	}
}
