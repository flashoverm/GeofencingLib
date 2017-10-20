package de.geofencing.client.handler;

import de.geofencing.system.beacon.SystemBeacon;

/** The GeneratedBeaconHandler should implement a way to display 
 * the administrator the values of the generated beacon.
 * 
 * @author Markus Thral
 *
 */
public abstract interface GeneratedBeaconHandler {
	
	/** Is called when a beacon is generated
	 * 
	 * @param beacon Generated beacon as SystemBeacon
	 */
	public abstract void onBeaconGenerated(SystemBeacon beacon);

}
