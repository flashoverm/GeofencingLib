package de.geofencing.event;

import java.util.ArrayList;
import java.util.List;

import de.geofencing.system.beacon.SystemBeacon;

/** Log of changes of the beacons in range of a Device object.
 *  Contains lists of entered and left beacons
 * 
 * @author Markus Thral
 *
 */
public class BeaconChange {
	private List<SystemBeacon> enteredBeacons;
	private List<SystemBeacon> leftBeacons;
	
	/** Creates a new log
	 * 
	 */
	public BeaconChange(){
		enteredBeacons = new ArrayList<>();
		leftBeacons = new ArrayList<>();
	}

	/** Returns List of SystemBeacons which are new in range.
	 * 
	 * @return SystemBeacons as List
	 */
	public List<SystemBeacon> getEnteredBeacons() {
		return enteredBeacons;
	}

	/** Adds new beacon in range
	 * 
	 * @param newBeacon SystemBeacon new in range
	 */
	public void addEnteredBeacon(SystemBeacon newBeacon) {
		this.enteredBeacons.add(newBeacon);
	}

	/** Returns List of SystemBeacons which are no more in range.
	 * 
	 * @return SystemBeacons as List
	 */
	public List<SystemBeacon> getLeftBeacons() {
		return leftBeacons;
	}

	/** Adds beacon which is no more in range
	 * 
	 * @param removed SystemBeacon no more in range
	 */
	public void addLeftBeacon(SystemBeacon removed) {
		this.leftBeacons.add(removed);
	}
}
