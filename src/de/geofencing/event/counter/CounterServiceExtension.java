package de.geofencing.event.counter;

import javax.ws.rs.core.Response;

import de.geofencing.database.GeofencingDBConnector;
import de.geofencing.event.Event;
import de.geofencing.log.LogEntry;
import de.geofencing.service.GeofencingService;
import de.geofencing.system.GeofencingSystem;
import de.geofencing.system.exceptions.NotFoundException;
import de.geofencing.system.exceptions.UnauthorizedExcpetion;

/** Provides methods to implement the web service to access and modify Counter. 
 * Return values are wrapped in Response object. 
 * For access of secured data, the authentication header is evaluated.
 * Errors are handled with the HTTP status codes (Unauthorized, Not Found, ...)
 * 
 * @author Markus Thral
 *
 */
public class CounterServiceExtension extends GeofencingService{

	/** Creates a new Instance of the service extension
	 * 
	 * @param system GeofencingSystem of this server
	 */
	public CounterServiceExtension(GeofencingSystem system){
		super(system);
	}

	/** Gets the value of the Counter with the given counterID.
	 * 
	 * @param counterID CounterID of the Counter
	 * @param authHeader Administrator password from the authentication header
	 * @return Value of the Counter as Integer wrapped in Response object
	 */
	public Response getCounterValue(int counterID, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK).entity(CounterDBConnector.findCounter(counterID).getValue()).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Gets the value of the Counter from the Geofence.
	 * Only usable if only one Counter is assigned to the Geofence.
	 * I.e. with GeofenceCounterEvent.
	 * 
	 * @param minor Minor of the Geofence
	 * @param authHeader Administrator password from the authentication header
	 * @return Value of the Counter as Integer wrapped in Response object
	 */
	public Response getGeofenceCounterValue(int minor, String authHeader){
		try{
			system.checkPassword(authHeader);
			for(Event event : GeofencingDBConnector.findGeofenceEvents(minor)){
				if (event instanceof GeofenceCounterEvent){
					Counter counter = CounterDBConnector.findCounter(((GeofenceCounterEvent)event).getCounterID());
					return Response.status(Response.Status.OK).entity(counter.getValue()).build();
				}	
			}
			throw new NotFoundException("ModifyCounterEvent in Geofence " + minor);
		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/** Gets a list of all Counter
	 * 
	 * @param authHeader Administrator password from the authentication header
	 * @return All Counter as List wrapped in Response object
	 */
	public Response getCounterList(String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK).entity(CounterDBConnector.findCounterList()).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
		
	/** Removes Counter with the given counterID.
	 * 
	 * @param counterID CounterID of the Counter
	 * @param authHeader Authentication header to access secured data
	 * @return True if Counter removed, false if not, wrapped in Response object
	 */
	public Response removeCounter(int counterID, String authHeader){
		try{
			system.checkPassword(authHeader);
			return Response.status(Response.Status.OK).entity(CounterDBConnector.removeCounter(counterID)).build();

		}catch(UnauthorizedExcpetion e){
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}catch(NotFoundException e){
			return Response.status(Response.Status.NOT_FOUND).build();
		}catch(Exception e){
			LogEntry.c(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
}
