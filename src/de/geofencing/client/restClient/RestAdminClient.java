package de.geofencing.client.restClient;

import com.google.gson.Gson;

import de.geofencing.client.httpClient.HttpClientException;
import de.geofencing.event.Event;
import de.geofencing.event.EventList;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.device.Devices;
import de.geofencing.system.geofence.GeofenceList;

/** Provides a client for the restful web service with all functions the the GeofenceAdmin.
 * 
 * @author Markus Thral
 *
 */
public class RestAdminClient extends RestClient {

	public RestAdminClient(String restServiceURL) {
		super(restServiceURL);
	}
	
	/** Checks administrator password
	 * 
	 * @param password Administrator password
	 * @return true if password is correct, false if not
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean checkPassword(String password) throws HttpClientException{
		return client.target(restServiceURL+"checkPassword")
				.put(password, String.class, Boolean.class);
	}

	/** Gets the registered devices from the system
	 * 
	 * @return Registered devices as Devices object
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public Devices getDevices() throws HttpClientException{
		return client.target(restServiceURL+"devices")
				.get(Devices.class);
	}
	
	/** Get geofences of the system as GeofenceList
	 * 
	 * @return Geofences as GeofenceList
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public GeofenceList getGeofenceList() throws HttpClientException{
		return client.target(restServiceURL+"geofences")
				.get(GeofenceList.class);
	}
	
	/** Gets events of the Geofence as EventList
	 * 
	 * @param minor Minor of the Geofence
	 * @return Events as EventList
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public EventList getEvents(int minor) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor+"/events")
				.get(EventList.class);
	}
	
	/** Gets Event of the Geofence with the given eventID
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Event
	 * @return Event (or derivation) with given eventID
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public Event getEvent(int minor, int eventID) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor+"/events/"+eventID)
				.get(Event.class);
	}
	
	/** Gets beacon of the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @return SystemBeacons object with the beacons of the system
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public SystemBeacons getBeacons(int minor) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor+"/beacons")
				.get(SystemBeacons.class);
	}
		
	/** Gets beacon of the Geofence with given major
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the beacon
	 * @return Beacon as SystemBeacon with given major
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public SystemBeacon getBeacon(int minor, int major) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor+"/beacons/"+major)
				.get(SystemBeacon.class);
	}
		
	/** Creates Geofence with given description
	 * 
	 * @param description Description of the Geofence
	 * @return Minor of the generated Geofence
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public int addGeofence(String description) throws HttpClientException{
		return client.target(restServiceURL+"geofence")
				.post(description, String.class, Integer.class);
	}
	
	/** Adds Event to Geofence matching the minor of the Event
	 * 
	 * @param event Event (or derivation) to be added
	 * @return true if Event added, false if addToGeofence aborts
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean addEvent(Event event) throws HttpClientException{
		String json = new Gson().toJson(event);
		return client.target(restServiceURL+"event")
				.post(json, String.class, Boolean.class);
	}

	/** Generates beacon for the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @return Generated beacon as SystemBeacon object
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public SystemBeacon generateBeacon(int minor) throws HttpClientException{
			return client.target(restServiceURL+"geofences/"+minor+"/generateBeacon")
					.get(SystemBeacon.class);
	}
	
	/** Adds beacon to the Geofence matching the minor of the beacon
	 * 
	 * @param beacon SystemBeacon which should be added
	 * @return true if beacon added, false if UUID is not matching with the system
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean addBeacon(SystemBeacon beacon) throws HttpClientException{
		return client.target(restServiceURL+"beacon")
				.post(beacon, SystemBeacon.class, Boolean.class);
	}
	
	/** Deletes beacon from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the beacon
	 * @return true if beacon removed
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean deleteBeacon(int minor, int major) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor+"/beacons/"+major)
				.delete(Boolean.class);
	}
	
	/** Deletes Event from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Geofence
	 * @return true if Event removed
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean deleteEvent(int minor, int eventID) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor+"/events/"+eventID)
				.delete(Boolean.class);
	}
	
	/** Deletes Geofence from the system
	 * 
	 * @param minor Minor of the Geofence
	 * @return true if Geofence removed, false if there are beacons or events left
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean deleteGeofence(int minor) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor)
				.delete(Boolean.class);
	}
}
