package de.geofencing.client.beaconScanner;

import de.geofencing.system.beacon.SystemBeacons;

/** The BeaconChangeHandler can be used for handling
 *  or displaying the beacons in range 
 * 
 * @author Markus Thral
 *
 */
public abstract interface BeaconChangeHandler {
	
	/** Is called when the beacons in range change
	 * 
	 * @param inRange List of beacons in range as SystemBeacons object
	 */
	public abstract void onBeaconsChangeDetected(SystemBeacons inRange);
	
}
