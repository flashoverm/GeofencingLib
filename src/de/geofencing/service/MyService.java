package de.geofencing.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.geofencing.event.counter.CounterServiceExtension;
import de.geofencing.event.mail.MailServiceExtension;
import de.geofencing.system.GeofencingSystem;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;

//TODO Entfernen
@Path("/")
public class MyService {
	
	GeofencingSystem system = new GeofencingSystem();
	
	//Geofencing
	
	GeofencingService service = new GeofencingService(system);
	
	@GET
	@Path("/serviceState")
	@Produces(MediaType.APPLICATION_JSON)	public Response getServiceState(){
		return service.getServiceState();
	}
	
	@PUT
	@Path("/checkPassword")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkPassword(String password){
		return service.checkPassword(password);
	}

	@GET
	@Path("/devices")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDevices(
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.getDevices(authHeader);
	}
	
	@GET
	@Path("/devices/{deviceID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDevice(@PathParam("deviceID") int deviceID,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.getDevice(deviceID, authHeader);
	}
	
	@GET
	@Path("/geofences")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGeofenceList(
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.getGeofenceList(authHeader);
	}
	
	@GET
	@Path("/geofences/{minor}/events")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getEvents(@PathParam("minor") int minor, @PathParam("eventID") int eventID,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.getEventList(minor, authHeader);
	}
	
	@GET
	@Path("/geofences/{minor}/beacons")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBeacons(@PathParam("minor") int minor,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.getBeacons(minor, authHeader);
	}
	
	@GET
	@Path("/geofences/{minor}/beacons/{major}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBeacon(@PathParam("minor") int minor, @PathParam("major") int major,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.getBeacon(minor, major, authHeader);
	}

	@PUT
	@Path("/foundBeacons")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBeaconData(SystemBeacons beacons,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.getBeaconData(beacons, authHeader);
	}	
	
	@POST
	@Path("/geofence")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addGeofence(String description,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.addGeofence(description, authHeader);
	}
	
	@POST
	@Path("/event")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addEvent(String eventJSON,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.addEvent(eventJSON, authHeader);
	}
	
	@POST
	@Path("/device")
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerDevice(@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.registerDevice(authHeader);
	}
	
	@GET
	@Path("/geofences/{minor}/generateBeacon")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateBeacon(@PathParam("minor") int minor, 
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.generateBeacon(minor, authHeader);
	}
	
	@POST
	@Path("/beacon")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addBeacon(@PathParam("minor") int minor, SystemBeacon beacon,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.addBeacon(beacon, authHeader);
	}

	@PUT
	@Path("/devices/{deviceID}/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDevice(@PathParam("deviceID") int deviceID, SystemBeacons beaconList,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.updateDevice(deviceID, beaconList, authHeader);
	}
	
	@PUT
	@Path("/devices/{deviceID}/update/token")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDevice(@PathParam("deviceID") int deviceID, String token,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.updateDeviceToken(deviceID, token, authHeader);
	}
	
	@DELETE 
	@Path("/devices/{deviceID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeDevice(@PathParam("deviceID") int deviceID,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.removeDevice(deviceID, authHeader);
	}
	
	@DELETE
	@Path("/geofences/{minor}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeGeofence(@PathParam("minor") int minor,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.removeGeofence(minor, authHeader);
	}
	
	@DELETE
	@Path("/geofences/{minor}/beacons/{major}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeBeacon(@PathParam("minor") int minor, @PathParam("major") int major,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.removeBeacon(minor, major, authHeader);
	}
	
	@DELETE
	@Path("/geofences/{minor}/events/{eventID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeEvent(@PathParam("minor") int minor, @PathParam("eventID") int eventID,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return service.removeEvent(minor, eventID, authHeader);
	}
	
	//Counter
	
	CounterServiceExtension counterService = new CounterServiceExtension(system);

	@GET
	@Path("/counter")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCounterList(
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return counterService.getCounterList(authHeader);
	}
	
	@GET
	@Path("/counter/{counterID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCounterValue(@PathParam("counterID") int counterID,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return counterService.getCounterValue(counterID, authHeader);
	}
	
	@GET
	@Path("/geofences/{minor}/counter")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGeofenceCounterValue(@PathParam("minor") int minor,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return counterService.getGeofenceCounterValue(minor, authHeader);
	}
	
	@DELETE 
	@Path("/counter/{counterID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeCounter(@PathParam("counterID") int counterID,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return counterService.removeCounter(counterID, authHeader);
	}
	
	//Mail
	
	MailServiceExtension mailService = new MailServiceExtension(system); 
	
	@GET
	@Path("/verification/{mailAddress}")
	@Produces(MediaType.TEXT_HTML)
	public String verifyMail(@PathParam("mailAddress") String mailAddress,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return mailService.verifyMail(mailAddress);
	}
	
	@GET
	@Path("/unsubscribe/{mailAddress}")
	@Produces(MediaType.TEXT_HTML)
	public String unscribeMail(@PathParam("mailAddress") String mailAddress,
			@HeaderParam(GeofencingService.AUTHORIZATIONHEADER) String authHeader){
		return mailService.unsubscribeMail(mailAddress);
	}

}