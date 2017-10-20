package de.geofencing.system.device;

import java.io.Serializable;
import java.sql.Timestamp;

import de.geofencing.event.BeaconChange;
import de.geofencing.system.beacon.SystemBeacons;


/** Represents a device which is registered on the GeofencingSystem. 
 * The deviceID is the primary identifier, the mail address is 
 * for verification.
 * The time stamp lastTimeUpdated shows the last time, the device
 * updated its beacons. 
 * The optional Firebase token is used to send notifications to the Device.
 * 
 * @author Markus Thral
 *
 */
public class Device implements Serializable{

	private static final long serialVersionUID = 1L;

	private final int deviceID;
	private final String address;
	private long lastTimeUpdated;
	private String firebaseToken;
	
	private SystemBeacons beacons;
	
	/** Constructor for serializing
	 * 
	 */
	Device(){
		this.deviceID = -1;
		this.address = null;
		this.lastTimeUpdated = -1;
		this.beacons = null;
		this.firebaseToken = null;
	}

	/** Creates Device with given deviceID and address
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param address Mail address of the Device
	 */
	public Device(int deviceID, String address){
		this.deviceID = deviceID;
		this.address = address;
		this.lastTimeUpdated = System.currentTimeMillis();
		this.beacons = new SystemBeacons();
		this.firebaseToken = null;
	}

	/** Creates Device with given deviceID, address and Firebase token
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param address Mail address of the Device
	 * @param firebaseToken Firebase token of the Device
	 */
	public Device(int deviceID, String address, String firebaseToken){
		this.deviceID = deviceID;
		this.address = address;
		this.lastTimeUpdated = System.currentTimeMillis();
		this.beacons = new SystemBeacons();
		this.firebaseToken = firebaseToken;
	}
	
	public int getDeviceID() {
		return deviceID;
	}
	
	public String getAddress(){
		return address;
	}

	public Timestamp getLastTimeUpdated(){
		return new Timestamp(lastTimeUpdated);
	}
	
	public String getFireBaseToken(){
		return firebaseToken;
	}
	
	public SystemBeacons getBeacons() {
		return beacons;
	}
	
	public void setFireBaseToken(String firebaseToken){
		this.firebaseToken = firebaseToken;
	}

	public void setBeacons(SystemBeacons beacons) {
		this.beacons = beacons;
	}
	
	/** Updates beacons in range of the Device
	 * 
	 * @param updatedBeacons List of beacons in range as SystemBeacons object
	 * @return BeaconChange log with information about the change, null if equal
	 */
	public BeaconChange updateBeacons(SystemBeacons updatedBeacons){
		return this.beacons.compare(updatedBeacons);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + deviceID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Device other = (Device) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (deviceID != other.deviceID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Device [deviceID=" + deviceID + ", address=" + address + ", lastTimeUpdated: " + lastTimeUpdated + "]";
	}	
}