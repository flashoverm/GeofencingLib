package de.geofencing.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import de.geofencing.event.Event;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.device.Device;
import de.geofencing.system.device.Devices;
import de.geofencing.system.exceptions.AlreadyExistingException;
import de.geofencing.system.exceptions.NotFoundException;
import de.geofencing.system.geofence.Geofence;

/** Provides methods to execute CRUD-operations on the MongoDB
 * 
 * Uses Mongo Java Driver (Needs mongo-java-driver-3.4.2.jar or newer)
 * 
 * @author Markus Thral
 *
 */
public class GeofencingDBConnector {
	
	//Create

	/** Inserts Device in database without checking deviceID or address
	 * 
	 * @param device Device to be added
	 */
	public static void insertDevice(Device device){
		MongoConnection connection = new MongoConnection();
		connection.getDeviceCollection().insertOne(DocumentConverter.toDocument(device));
		connection.disconnect();
	}
	
	/** Inserts Geofence in database without checking minor
	 * 
	 * @param geofence Geofence to be added
	 */
	public static void insertGeofence(Geofence geofence){
		MongoConnection connection = new MongoConnection();
		connection.getGeofenceCollection().insertOne(DocumentConverter.toDocument(geofence));
		connection.disconnect();
	}
	
	/** Inserts beacons in database without checking UUID, major or minor
	 * 
	 * @param beacon SystemBeacon to be added
	 */
	public static void insertBeacon(SystemBeacon beacon){
		MongoConnection connection = new MongoConnection();
		connection.getBeaconCollection().insertOne(DocumentConverter.toDocument(beacon));
		connection.disconnect();
	}
	
	/** Inserts Event or derived object in database without checking eventID or minor
	 * 
	 * @param event Event object to be added
	 */
	public static void insertEvent(Event event){
		MongoConnection connection = new MongoConnection();
		connection.getEventCollection().insertOne(DocumentConverter.toDocument(event));
		connection.disconnect();
	}
	
	//Read
	
	/** Checks if Geofence with the given minor exists
	 * 
	 * @param minor Minor to be checked
	 * @return true if minor exists, false if not
	 */
	public static boolean isGeofenceExisting(int minor){
		MongoConnection connection = new MongoConnection();
		Document document = connection.getGeofenceCollection().find(eq("minor", minor)).first();
		connection.disconnect();
		if(document != null){
			return true;
		}
		return false;
	}
	
	/** Gets Geofence with the given minor from the database
	 * 
	 * @param minor Minor of the Geofence
	 * @return Geofence object if minor existing
	 * @throws NotFoundException if minor not existing
	 */
	public static Geofence findGeofence(int minor) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		Document document = connection.getGeofenceCollection().find(eq("minor", minor)).first();
		if(document != null){
			Geofence geofence = (Geofence)DocumentConverter.toObject(Geofence.class, document);
			for(SystemBeacon beacon:findGeofenceBeacons(minor)){
				geofence.addBeacon(beacon);
			}
			for(Event event:findGeofenceEvents(minor)){
				geofence.addEvent(event);
			}
			connection.disconnect();
			return geofence;
		}
		connection.disconnect();
		throw new NotFoundException("Geofence " + minor);
	}
	
	/** Gets all Geofences from the database
	 * 
	 * @return List of Geofences in the database
	 */
	public static List<Geofence> findGeofences(){
		MongoConnection connection = new MongoConnection();
		List<Geofence> geofences = new ArrayList<>();
		for(Document document : connection.getGeofenceCollection().find()){
			geofences.add((Geofence)DocumentConverter.toObject(Geofence.class, document));
		}
		connection.disconnect();
		return geofences;
	}

	/** Gets beacons from the Geofence with the given minor from the database
	 * 
	 * @param minor Minor of the Geofence
	 * @return SystemBeacons object with the beacons
	 * @throws NotFoundException if the given minor is not existing
	 */
	public static SystemBeacons findGeofenceBeacons(int minor) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		isGeofenceExisting(minor);
		SystemBeacons beacons = new SystemBeacons();
		for(Document document : connection.getBeaconCollection().find(eq("minor", minor))){
			beacons.addBeacon((SystemBeacon)DocumentConverter.toObject(SystemBeacon.class, document));
		}
		connection.disconnect();
		return beacons;
	}
	
	/** Gets Event of a Geofence from the database
	 * 
	 * @param minor Minor of the Geofence
	 * @return List of Event objects
	 * @throws NotFoundException if the given minor is not existing
	 */
	public static List<Event> findGeofenceEvents(int minor) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		isGeofenceExisting(minor);
		List<Event> events = new ArrayList<>();
		for(Document document:connection.getEventCollection().find(eq("minor", minor))){
			events.add((Event)DocumentConverter.toObject(Event.class, document));
		}
		connection.disconnect();
		return events;
	}

	/** Gets Event from the database with the given eventID from the Geofence with the given minor 
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Event
	 * @return Event object with the given eventID from the Geofence
	 * @throws NotFoundException if the given minor or eventID is not existing
	 */
	public static Event findGeofenceEvent(int minor, int eventID) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		Document document = connection.getEventCollection().find(
				and(eq("minor", minor), eq("eventID", eventID)))
				.first();
		connection.disconnect();
		if(document != null){
			return (Event)DocumentConverter.toObject(Event.class, document);
		}
		throw new NotFoundException("Event minor/eventID " + minor + "/" + eventID);		
	}

	/** Checks if beacon is existing
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the SystemBeacon
	 * @return true if SystemBeacon exisiting, false if not
	 */
	public static boolean isBeaconExisiting(int minor, int major){
		try{
			findBeacon(minor, major);
			return true;
		} catch(NotFoundException e){
			return false;
		}
	}
	
	/** Gets beacon with given minor and major from the database
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the SystemBeacon
	 * @return SystemBeacon object with the given minor and major
	 * @throws NotFoundException if minor or major not existing
	 */
	public static SystemBeacon findBeacon(int minor, int major) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		Document document = connection.getBeaconCollection()
				.find(and(eq("minor", minor), eq("major", major)))
				.first();
		connection.disconnect();
		if(document != null){
			return (SystemBeacon)DocumentConverter.toObject(SystemBeacon.class, document);
		}
		throw new NotFoundException("Beacon minor/major" + minor + "/" + major);
	}
	
	/** Gets all beacons from the database
	 * 
	 * @return SystemBeacons object with all beacons
	 */
	public static SystemBeacons findBeacons(){
		MongoConnection connection = new MongoConnection();
		SystemBeacons beacons = new SystemBeacons();
		for(Document document : connection.getBeaconCollection().find()){
			beacons.addBeacon((SystemBeacon)DocumentConverter.toObject(SystemBeacon.class, document));
		}
		connection.disconnect();
		return beacons;
	}

	/** Checks if given deviceID is already existing
	 * 
	 * @param deviceID DeviceID to be checked
	 * @return true if deviceID existing, false if not
	 */
	public static boolean isDeviceExisting(int deviceID){
		try{
			findDevice(deviceID);
			return true;
		}catch(NotFoundException e){
			return false;
		}
	}
	
	/** Checks if given mail address is already in use
	 * 
	 * @param mailAddress mail address to be checked
	 * @return true if address is in use, false if not
	 */
	public static boolean mailAddressInUse(String mailAddress){
		try{
			findDevice(mailAddress);
			return true;
		}catch(NotFoundException e){
			return false;
		}
	}
	
	/** Gets Device from the database
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return Device object with the given deviceID
	 * @throws NotFoundException if deviceID is not existing
	 */
	public static Device findDevice(int deviceID) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		Document document = connection.getDeviceCollection().find(eq("deviceID", deviceID)).first();
		connection.disconnect();
		if(document != null){
			return (Device)DocumentConverter.toObject(Device.class, document);
		}
		throw new NotFoundException("Device " + deviceID);
	}
	
	/** Gets Device with the given mail address
	 * 
	 * @param mailAddress Mail address of the device
	 * @return Device object with the given mail address
	 * @throws NotFoundException if mail address not existing
	 */
	public static Device findDevice(String mailAddress) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		Document document = connection.getDeviceCollection().find(eq("address", mailAddress)).first();
		connection.disconnect();
		if(document != null){
			return (Device)DocumentConverter.toObject(Device.class, document);
		}
		throw new NotFoundException("Device " + mailAddress);
	}
	
	/** Gets all Devices from the database
	 * 
	 * @return Devices object with all devices
	 */
	public static Devices findDevices(){
		MongoConnection connection = new MongoConnection();
		Devices devices = new Devices();
		for(Document document : connection.getDeviceCollection().find()){
			try {
				devices.addDevice((Device)DocumentConverter.toObject(Device.class, document));
			} catch (AlreadyExistingException e) {
			}
		}
		connection.disconnect();
		return devices;
	}
	
	/** Checks if Device is in range of the Geofence
	 * 
	 * @param deviceID DeviceID of the device
	 * @param minor Minor of the Geofence
	 * @return true if Device is in range, false if not
	 * @throws NotFoundException if minor or deviceID not existing
	 */
	public static boolean isDeviceInGeofence(int deviceID, int minor) throws NotFoundException{
		SystemBeacons beacons = findDeviceBeacons(deviceID);
		return beacons.containsBeaconWithMinor(minor);
	}
	
	/** Gets beacons in range of the Device
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return SystemBeacons object with the beacons in range
	 * @throws NotFoundException if deviceID not existing
	 */
	public static SystemBeacons findDeviceBeacons(int deviceID) throws NotFoundException{
		return findDevice(deviceID).getBeacons();
	}
	
	//Update
	
	/** Updates beacons in range of the Device
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param beacons SystemBeacons object with beacons to be updated
	 * @return true if updated succeeded, false if an error occured
	 * @throws NotFoundException if deviceID not existing
	 */
	public static boolean updateDevice(int deviceID, SystemBeacons beacons) throws NotFoundException{
		findDevice(deviceID);
		MongoConnection connection = new MongoConnection();
		Document beaconsDoc = DocumentConverter.toDocument(beacons);	
		UpdateResult result = connection.getDeviceCollection().updateOne(
				eq("deviceID", deviceID), 
				new Document("$set", new Document("beacons", beaconsDoc)));
		if(result.getModifiedCount() == 1){
			result = connection.getDeviceCollection().updateOne(
					eq("deviceID", deviceID), 
					new Document("$set", new Document("lastTimeUpdated", System.currentTimeMillis())));
			if(result.getModifiedCount() == 1){
				connection.disconnect();
				return true;
			}
		}
		connection.disconnect();
		return false;
	}
	
	/** Updates Firebase token of the Device
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param firebaseToken Firebase token to be updated
	 * @return true if update succeeded, false if an error occured
	 * @throws NotFoundException if deviceID not existing
	 */
	public static boolean updateDevice(int deviceID, String firebaseToken) throws NotFoundException{
		findDevice(deviceID);
		MongoConnection connection = new MongoConnection();
		UpdateResult result = connection.getDeviceCollection().updateOne(
				eq("deviceID", deviceID), 
				new Document("$set", new Document("firebaseToken", firebaseToken)));
		connection.disconnect();
		if(result.getModifiedCount() == 1){
			return true;
		}
		return false;
	}
	
	//Delete
	
	/** Removes Device from the database
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return true if Device removed
	 * @throws NotFoundException if deviceID not existing
	 */
	public static boolean removeDevice(int deviceID) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		DeleteResult result = connection.getDeviceCollection().deleteOne(eq("deviceID", deviceID));
		connection.disconnect();
		if(result.getDeletedCount() == 1){
			return true;
		}
		throw new NotFoundException("Device " + deviceID);
	}
	
	/** Removes Geofence from the database
	 * 
	 * @param minor Minor of the Geofence
	 * @return true if Geofence removed
	 * @throws NotFoundException if minor not existing
	 */
	public static boolean removeGeofence(int minor) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		DeleteResult result = connection.getGeofenceCollection().deleteOne(eq("minor", minor));
		connection.disconnect();
		if(result.getDeletedCount() == 1){
			return true;
		}
		throw new NotFoundException("Geofence " + minor);
	}
	
	/** Removes Event from database
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Event
	 * @return true of Event is removed
	 * @throws NotFoundException if minor or eventID not existing
	 */
	public static boolean removeEvent(int minor, int eventID) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		DeleteResult result = connection.getEventCollection().deleteOne(and(eq("minor", minor), eq("eventID", eventID)));
		connection.disconnect();
		if(result.getDeletedCount() == 1){
			return true;
		}
		throw new NotFoundException("Event minor/eventID " + minor + "/" + eventID);		
	}
	
	/** Removes beacon from the database
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the SystemBeacon
	 * @return true of SystemBeacon is removed
	 * @throws NotFoundException if minor or major not existing
	 */
	public static boolean removeBeacon(int minor, int major) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		DeleteResult result = connection.getBeaconCollection().deleteOne(and(eq("minor", minor), eq("major", major)));
		connection.disconnect();
		if(result.getDeletedCount() == 1){
			return true;
		}
		throw new NotFoundException("Beacon minor/major" + minor + "/" + major);	
	}
}
