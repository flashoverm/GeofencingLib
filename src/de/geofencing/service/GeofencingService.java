package de.geofencing.service;

import javax.ws.rs.core.Response;

import de.geofencing.event.Event;
import de.geofencing.log.LogEntry;
import de.geofencing.system.GeofencingSystem;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.exceptions.AlreadyExistingException;
import de.geofencing.system.exceptions.NotFoundException;
import de.geofencing.system.exceptions.UnauthorizedExcpetion;

/** Provides methods to implement the web service to access and modify the GeofencingSystem. 
 * Return values are wrapped in Response object. 
 * For access of secured data, the authentication header is evaluated.
 * Errors are handled with the HTTP status codes (Unauthorized, Not Found, ...)
 * 
 * Uses Jersey for the Response (Needs jaxrs-ri 2.23.2 or newer)
 * Uses Jackson for serializing (Needs jackson 2.3.2 or newer)
 * 
 * @author Markus Thral
 *
 */
public class GeofencingService {

	/** Header with administrator password or verification address of the client
	 * sent by the client for authorization
	 */
	public static final String AUTHORIZATIONHEADER = "Authorization";
	
	protected GeofencingSystem system;
	
	/** Creates a new Instance of the service extension
	 * 
	 * @param system GeofencingSystem of this server
	 */
	public GeofencingService(GeofencingSystem system){
		this.system = system;
	}

	/** Returns always true, to check if the service is running/reachable 
	 * 
	 * @return true
	 */
	public Response getServiceState(){
		return Response.status(Response.Status.OK).entity(Boolean.TRUE).build();
	}

	/** Gets all Devices registered on the system
	 * 
	 * @param authHeader Administrator password from the authentication header
	 * @return Deivces of the system as Devices object wrapped in Response object
	 */
	public Response getDevices(String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK).entity(system.getDevices()).build();
			
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Gets Device with the given DeviceID
	 * 
	 * @param deviceID DeviceID if the Device
	 * @param authHeader must contain administrator password or the authentication address of the Device
	 * @return Device object, wrapped in Response object
	 */
	public Response getDevice(int deviceID, String authHeader){
		try{
			try{
				this.authenticateDevice(deviceID, authHeader);
			}catch(UnauthorizedExcpetion e){
				system.checkPassword(authHeader);
			}
			system.getDevice(deviceID);
			return Response.status(Response.Status.OK).entity(system.getDevice(deviceID)).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Gets Geofences of the system as GeofenceList object
	 * 
	 * @param authHeader Administrator password from the authentication header
	 * @return Geofences as GeofenceList object, wrapped in Response object
	 */
	public Response getGeofenceList(String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK).entity(system.getGeofenceList()).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/** Gets Events from the Geofence as EventList object
	 * 
	 * @param minor Minor of the Geofence
	 * @param authHeader Administrator password from the authentication header
	 * @return Events of the Geofence as EventList object, wrapped in Response object
	 */
	public Response getEventList(int minor, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK).entity(system.getEventList(minor)).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Gets Event from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Event
	 * @param authHeader Administrator password from the authentication header
	 * @return Event object, wrapped in Response object
	 */
	public Response getEvent(int minor, int eventID, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK).entity(
					system.getGeofence(minor).getEvent(eventID)).build();
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
		
	/** Gets beacons of the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param authHeader Administrator password from the authentication header
	 * @return beacons of the Geofence as SystemBeacons object, wrapped in Response object
	 */
	public Response getBeacons(int minor, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.getGeofence(minor).getBeacons()).build();
			
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Gets beacon with given major from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the beacon
	 * @param authHeader Administrator password from the authentication header
	 * @return beacon as SystemBeacon object, wrapped in Response object
	 */
	public Response getBeacon(int minor, int major, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.getGeofence(minor).getBeacon(major)).build();
			
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Gets description of the given beacons an returns them
	 * 
	 * @param beacons SystemBeacons object with beacons to get description for
	 * @param authHeader must contain administrator password or an authentication address of an Device
	 * @return beacons with description as SystemBeacons object, wrapped in Response object
	 */
	public Response getBeaconData(SystemBeacons beacons, String authHeader){
		try{
			try{
				this.authenticateDevice(system.getDevice(authHeader).getDeviceID(), authHeader);
			}catch(UnauthorizedExcpetion | NotFoundException e){
				system.checkPassword(authHeader);
			}
			return Response.status(Response.Status.OK)
					.entity(system.getBeaconData(beacons)).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/** Generates minor and adds Geofence to the system
	 * 
	 * @param description Description of the Geofence
	 * @param authHeader Administrator password from the authentication header
	 * @return generated minor of the Geofence, wrapped in Response object
	 */
	public Response addGeofence(String description, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.addGeofence(description)).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Registers Device with the given address on the system and generates deviceID
	 * 
	 * @param address Authentication mail address of the device
	 * @return generated deviceID as Integer, wrapped in Response object
	 */
	public Response registerDevice(String address){
		try {
			return Response.status(Response.Status.OK)
					.entity(system.addDevice(address)).build();
		} catch (AlreadyExistingException e) {
			return Response.status(Response.Status.CONFLICT).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Generates next beacon of the Geofence. Beacon is not added
	 * 
	 * @param minor Minor of the Geofence
	 * @param authHeader Administrator password from the authentication header
	 * @return generated beacon as SystemBeacon object, wrapped in Response object
	 */
	public Response generateBeacon(int minor, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.generateBeacon(minor)).build();
			
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Adds beacon to the Geofence
	 * 
	 * @param beacon SystemBeacon to be added
	 * @param authHeader Administrator password from the authentication header
	 * @return true if beacon added, false if not, wrapped in Response object
	 */
	public Response addBeacon(SystemBeacon beacon, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.addBeacon(beacon)).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Adds Event to the Geofence
	 * 
	 * @param eventJSON JSON String containing a serialized Event or a derivation
	 * @param authHeader Administrator password from the authentication header
	 * @return true if Event is added, false if an problem occurs, wrapped in Response objectw
	 */
	public Response addEvent(String eventJSON, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.addEventToGeofence(Event.jsonToEvent(eventJSON))).build();
			
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			e.printStackTrace();
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/** Updates beacons in range of the device
	 * 
	 * @param deviceID DeviceID if the Device
	 * @param beaconList Updated beacons in range of the Device
	 * @param authHeader must contain the authentication address of the Device
	 * @return true if beacons are updated, false if not, wrapped in Response object
	 */
	public Response updateDevice(int deviceID, SystemBeacons beaconList, String authHeader){
		try{
			this.authenticateDevice(deviceID, authHeader);
			if(system.updateDeviceBeacons(deviceID, beaconList)){
				return Response.status(Response.Status.OK).entity(Boolean.TRUE).build();
			}

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			LogEntry.c(e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		LogEntry.c("Could not update Device");
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/** Updates Firebase token of the Device
	 * 
	 * @param deviceID DeviceID if the Device
	 * @param token Firebase token of the Device
	 * @param authHeader must contain the authentication address of the Device
	 * @return true if token is updated wrapped in Response object
	 */
	public Response updateDeviceToken(int deviceID, String token, String authHeader){
		try{
			this.authenticateDevice(deviceID, authHeader);
			if(system.updateDeviceToken(deviceID, token)){
				return Response.status(Response.Status.OK).entity(Boolean.TRUE).build();
			}

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			LogEntry.c(e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		LogEntry.c("Could not update Device");
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/** Removes Device from system
	 * 
	 * @param deviceID DeviceID if the Device
	 * @param authHeader Administrator password from the authentication header
	 * @return true if Device removed, false if not, wrapped in Response object
	 */
	public Response removeDevice(int deviceID, String authHeader){
		try{
			try{
				this.authenticateDevice(deviceID, authHeader);
			}catch(UnauthorizedExcpetion e){
				system.checkPassword(authHeader);
			}
			return Response.status(Response.Status.OK)
					.entity(system.removeDevice(deviceID)).build();
			
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Removes Geofence from system
	 * 
	 * @param minor Minor of the Geofence
	 * @param authHeader Administrator password from the authentication header
	 * @return True if Geofence removed, false if there are beacons or events left, wrapped in Response object
	 */
	public Response removeGeofence(int minor, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.removeGeofence(minor)).build();
	
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Removes beacon from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param major Major of the beacon
	 * @param authHeader Administrator password from the authentication header
	 * @return True if beacon removed, false if not, wrapped in Response object
	 */
	public Response removeBeacon(int minor, int major, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.removeBeacon(minor, major)).build();
	
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Removes Event from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Event
	 * @param authHeader Administrator password from the authentication header
	 * @return True if Event removed, false if not, wrapped in Response object
	 */
	public Response removeEvent(int minor, int eventID, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK)
					.entity(system.removeEventFromGeofence(minor, eventID)).build();
	
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Checks if given administrator password is correct
	 * 
	 * @param password Administrator password from the authentication header
	 * @return true if correct, false if not
	 */
	public Response checkPassword(String password){
		try{
			system.checkPassword(password);
			return Response.status(Response.Status.OK).entity(Boolean.TRUE).build();
	
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.OK).entity(Boolean.FALSE).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Checks if the given authentication header contains the address which matches with the deviceID
	 * 
	 * @param deviceID DeviceID if the Device
	 * @param authHeader must contain the authentication address of the Device
	 * @return true if address and deviceID match, false if not
	 * @throws UnauthorizedExcpetion if deviceID not found or address incorrect
	 * @throws NotFoundException if Device with given deviceID not existing
	 */
	public boolean authenticateDevice(int deviceID, String authHeader) throws UnauthorizedExcpetion, NotFoundException{
		try{
			if(system.getDevice(authHeader).getDeviceID() == deviceID){
				return true;
			}
		} catch(NotFoundException e){}
		throw new UnauthorizedExcpetion();
	}
}
