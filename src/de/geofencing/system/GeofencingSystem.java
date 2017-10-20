package de.geofencing.system;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import de.geofencing.database.GeofencingDBConnector;
import de.geofencing.event.BeaconChange;
import de.geofencing.event.Event;
import de.geofencing.event.EventList;
import de.geofencing.event.Trigger;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.device.Device;
import de.geofencing.system.device.Devices;
import de.geofencing.system.exceptions.AlreadyExistingException;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.system.exceptions.NotFoundException;
import de.geofencing.system.exceptions.UnauthorizedExcpetion;
import de.geofencing.system.geofence.Geofence;
import de.geofencing.system.geofence.GeofenceList;

/** Represents a GeofencingSystem with an UUID containing 
 * registered devices, geofences, their beacons and events.
 * Configuration parameters has to be set in the Configuration
 * 
 * @author Markus Thral
 *
 */
public class GeofencingSystem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** Value which is set, if location description of a beacon is not set
	 */
	public static final String LOCATION_NOT_SET = "-not set-";

	private final UUID uuid;

	/** Creates GeofencingSystem and creates UUID if not found in configuration
	 * 
	 */
	public GeofencingSystem() {
		this.uuid = SystemConfiguration.getUUID();
	}

	public UUID getUUID() {
		return uuid;
	}

	/** Checks if administrator password is correct
	 * 
	 * @param password Administrator password
	 * @return true if password id correct
	 * @throws UnauthorizedExcpetion if password is incorrect
	 * @throws ConfigurationException if configuration not found or password not set
	 */
	public boolean checkPassword(String password) throws UnauthorizedExcpetion, ConfigurationException {
		return SystemConfiguration.checkPassword(password);
	}

	/*
	 * Geofences
	 */

	/** Gets Geofence with given minor
	 * 
	 * @param minor Minor of the Geofence
	 * @return Geofence with given minor
	 * @throws NotFoundException if Geofence is not exisiting
	 */
	public Geofence getGeofence(int minor) throws NotFoundException {
		return GeofencingDBConnector.findGeofence(minor);
	}

	/** Gets Geofences of the system as GeofenceList
	 * 
	 * @return Geofences of the system as GeofenceList
	 */
	public GeofenceList getGeofenceList(){
		GeofenceList list = new GeofenceList();
		for (Geofence geofence : GeofencingDBConnector.findGeofences()) {
			list.addGeofenceListing(geofence.generateListing());
		}
		return list;
	}

	/** Gets next available minor
	 * 
	 * @return next available minor
	 */
	protected int getNextMinor() {
		int nextMinor = 1;
		List<Geofence> list = GeofencingDBConnector.findGeofences();
		for (Geofence geofence : list) {
			if (geofence.getMinor() >= nextMinor) {
				nextMinor = geofence.getMinor() + 1;
			}
		}
		return nextMinor;
	}

	/** Adds Geofence to system
	 * 
	 * @param description Description of the Geofence
	 * @return generated minor of the Geofence
	 */
	public int addGeofence(String description) {
		int minor = this.getNextMinor();
		GeofencingDBConnector.insertGeofence(new Geofence(minor, description));
		return minor;
	}

	/** Removes Geofence form the system
	 * 
	 * @param minor Minor of the Geofence
	 * @return true if Geofence is removed, false if there are beacons or events left
	 * @throws NotFoundException if the Geofence is not found
	 */
	public boolean removeGeofence(int minor) throws NotFoundException {
		Geofence geofence = GeofencingDBConnector.findGeofence(minor);
		if (geofence.getBeacons().beaconCount() > 0) {
			return false;
		}
		if(geofence.getEvents().size() > 0) {
			return false;
		}
		return GeofencingDBConnector.removeGeofence(minor);
	}

	/*
	 * Events
	 */
	
	/** Get Events of the Geofence as EventList
	 * 
	 * @param minor Minor of the Geofence
	 * @return Events of the Geofence as EventList
	 * @throws NotFoundException if the Geofence is not exisiting
	 */
	public EventList getEventList(int minor) throws NotFoundException{
		return GeofencingDBConnector.findGeofence(minor).getEventList();
	}
	
	/** Gets next available eventID of the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @return next available eventID of the Geofence 
	 * @throws NotFoundException if Geofence is not exisiting
	 */
	protected int getNextEventID(int minor) throws NotFoundException {
		int nextEventID = 1;
		for (Event event : GeofencingDBConnector.findGeofenceEvents(minor)) {
			if (event.getEventID() >= nextEventID) {
				nextEventID = event.getEventID() + 1;
			}
		}
		return nextEventID;
	}

	/** Adds Event to Gefence
	 * 
	 * @param event Event or derivation
	 * @return true if Event is added, false if not
	 * @throws NotFoundException if Geofence is not existing
	 */
	public boolean addEventToGeofence(Event event) throws NotFoundException {
		if(event.onAddToGeofence()){
			event.setEventID(this.getNextEventID(event.getMinor()));
			GeofencingDBConnector.insertEvent(event);
			return true;
		}
		return false;
	}

	/** Removes Event from Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Event
	 * @return true if Event removed
	 * @throws NotFoundException if Geofence or Event is not existing
	 */
	public boolean removeEventFromGeofence(int minor, int eventID) throws NotFoundException {
		GeofencingDBConnector.findGeofence(minor);
		return GeofencingDBConnector.removeEvent(minor, eventID);
	}

	/*
	 * Beacons
	 */

	/** Gets descriptions to the given beacons
	 * 
	 * @param beacons SystemBeacons object with beacons
	 * @return SystemBeacons object with beacons and description
	 */
	public SystemBeacons getBeaconData(SystemBeacons beacons) {
		SystemBeacons withData = new SystemBeacons();

		for (SystemBeacon beacon : beacons) {
			try {
				withData.addBeacon(GeofencingDBConnector.findBeacon(beacon.getMinor(), beacon.getMajor()));
			} catch (NotFoundException e) {
				withData.addBeacon(beacon);
			}
		}
		return withData;
	}

	/** Filters beacons if registered on the system
	 * 
	 * @param beacons List of beacon
	 * @return SystemBeacons object containing only registered beacons
	 */
	public SystemBeacons filterBeacons(SystemBeacons beacons) {
		SystemBeacons filtered = new SystemBeacons();
		for (SystemBeacon beacon : beacons) {
			try {
				filtered.addBeacon(GeofencingDBConnector.findBeacon(beacon.getMinor(), beacon.getMajor()));
			} catch (NotFoundException e) {

			}
		}
		return filtered;
	}

	/** Gets beacon with description from the system
	 * 
	 * @param beacon SystemBeacon to look for description
	 * @return SystemBeacon with description
	 * @throws NotFoundException if SystemBeacon is not existing
	 */
	protected SystemBeacon getBeacon(SystemBeacon beacon) throws NotFoundException {
		if (beacon.getUUID().equals(this.uuid)) {
			return GeofencingDBConnector.findBeacon(beacon.getMinor(), beacon.getMajor());
		}
		return null;
	}

	/** Generates next available SystemBeacon of the Geofence, but is not added to the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @return Generated SystemBeacon
	 * @throws NotFoundException if Geofence is not existing
	 */
	public SystemBeacon generateBeacon(int minor) throws NotFoundException {
		return GeofencingDBConnector.findGeofence(minor).generateBeacon(uuid);
	}

	/** Adds beacon to the Geofence
	 * 
	 * @param beacon SystemBeacon to be added
	 * @return true if beacon added, false if UUID is not matching with the system
	 * @throws AlreadyExistingException if beacon is already existing
	 * @throws NotFoundException if Geofence is not existing
	 */
	public boolean addBeacon(SystemBeacon beacon) throws AlreadyExistingException, NotFoundException {
		if (beacon.getUUID().equals(this.uuid)) {
			if (!GeofencingDBConnector.isGeofenceExisting(beacon.getMinor())) {
				throw new NotFoundException("Geofence: " + beacon.getMinor());
			}
			if (GeofencingDBConnector.isBeaconExisiting(beacon.getMinor(), beacon.getMajor())) {
				throw new AlreadyExistingException(
						"Beacon: " + beacon.getUUID() + ":" + beacon.getMajor() + ":" + beacon.getMinor());
			}
			if (beacon.getLocation() == "") {
				beacon.setLocation(LOCATION_NOT_SET);
			}
			GeofencingDBConnector.insertBeacon(beacon);
			return true;
		}
		return false;
	}

	/** Removes beacon from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the beacon
	 * @return true if beacons removed
	 * @throws NotFoundException if SystemBeacon is not existing
	 */
	public boolean removeBeacon(int minor, int major) throws NotFoundException {
		return GeofencingDBConnector.removeBeacon(minor, major);
	}

	/*
	 * Devices
	 */

	/** Gets Device with given deviceID
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return Device with given deviceID
	 * @throws NotFoundException if Device is not existing
	 */
	public Device getDevice(int deviceID) throws NotFoundException {
		return GeofencingDBConnector.findDevice(deviceID);
	}

	/** Gets Device with given mail address
	 * 
	 * @param mailAddress Mail address of the Device
	 * @return Device with given mail address
	 * @throws NotFoundException if Device is not existing
	 */
	public Device getDevice(String mailAddress) throws NotFoundException {
		return GeofencingDBConnector.findDevice(mailAddress);
	}

	/** Gets registered Devices
	 * 
	 * @return Devices object with registered Devices
	 */
	public Devices getDevices() {
		return GeofencingDBConnector.findDevices();
	}

	/** Adds Device to the system
	 * 
	 * @param mailAddress Mail address of the Device
	 * @return generated deviceID if Device is added
	 * @throws AlreadyExistingException if the mail address is already exisiting
	 */
	public int addDevice(String mailAddress) throws AlreadyExistingException {
		if (!GeofencingDBConnector.mailAddressInUse(mailAddress)) {
			Random rand = new Random();
			int randomDid;
			do {
				randomDid = rand.nextInt(Integer.MAX_VALUE);
			} while (GeofencingDBConnector.isDeviceExisting(randomDid));
			Device device = new Device(randomDid, mailAddress);
			GeofencingDBConnector.insertDevice(device);
			return randomDid;
		}
		throw new AlreadyExistingException(mailAddress);
	}

	/** Removes Device from the system
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return true if device is removed
	 * @throws NotFoundException if Device not existing
	 */
	public boolean removeDevice(int deviceID) throws NotFoundException {
		return GeofencingDBConnector.removeDevice(deviceID);
	}

	/** Updates beacons in range of the Device
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param beacons Beacons in range of the device
	 * @return true if updated
	 * @throws NotFoundException if Device not existing
	 */
	public boolean updateDeviceBeacons(int deviceID, SystemBeacons beacons) throws NotFoundException {
		SystemBeacons filtered = filterBeacons(beacons);
		BeaconChange beaconChange = GeofencingDBConnector.findDevice(deviceID).updateBeacons(filtered);
		if (beaconChange != null) {
			GeofencingDBConnector.updateDevice(deviceID, filtered);
			Geofence geofence;
			for (SystemBeacon beacon : beaconChange.getEnteredBeacons()) {
				try {
					geofence = GeofencingDBConnector.findGeofence(beacon.getMinor());
					geofence.trigger(Trigger.Direction.Enter, deviceID);
				} catch (NotFoundException e) {
				}
			}
			for (SystemBeacon beacon : beaconChange.getLeftBeacons()) {
				try {
					geofence = GeofencingDBConnector.findGeofence(beacon.getMinor());
					geofence.trigger(Trigger.Direction.Leave, deviceID);
				} catch (NotFoundException e) {
				}
			}
		}
		return true;
	}
	
	/** Updated Firebase token of the device
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param firebaseToken Firebase Token of the Device
	 * @return true if token is updated, false if not
	 * @throws NotFoundException if Device not existing
	 */
	public boolean updateDeviceToken(int deviceID, String firebaseToken) throws NotFoundException{
		return GeofencingDBConnector.updateDevice(deviceID, firebaseToken);
	}
}
