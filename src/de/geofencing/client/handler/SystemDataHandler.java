package de.geofencing.client.handler;

import de.geofencing.event.EventList;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.device.Devices;
import de.geofencing.system.geofence.GeofenceList;
import de.geofencing.system.geofence.GeofenceListing;

/** The SystemDataHandler provides methods which are called,
 * when the requested data about the geofencing system are received.
 * (List of Geofences, Devices, Geofence with beacons and events)
 * 
 * @author Markus Thral
 *
 */
public abstract interface SystemDataHandler {
	
	/** Is called when the geofence list is updated
	 * 
	 * @param geofences Geofences of the system as GeofenceList
	 */
	public abstract void onUpdateGeofences(GeofenceList geofences);
	
	/** Is called when the list of devices is updated
	 * 
	 * @param devices Devies of the system as Devices
	 */
	public abstract void onUpdateDevices(Devices devices);
	
	/** Is called when a specific Geofence is updated
	 * 
	 * @param geofence Information about the Geofence as GeofenceListing
	 * @param events List of events as EventList
	 * @param beacons List of beacon as SystemBeacons object
	 */
	public abstract void onUpdateGeofence(
			GeofenceListing geofence, 
			EventList events, 
			SystemBeacons beacons);

}
