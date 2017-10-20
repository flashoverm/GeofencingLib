package de.geofencing.system.device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.geofencing.system.exceptions.AlreadyExistingException;
import de.geofencing.system.exceptions.NotFoundException;

/** Represents a list of Device objects.
 * 
 * @author Markus Thral
 *
 */
public class Devices implements Serializable, Iterable<Device> {

	private static final long serialVersionUID = 1L;
	
	private List<Device> deviceList;
	
	/** Creates a new list of devices
	 * 
	 */
	public Devices(){
		deviceList = new ArrayList<>();
	}

	/** Returns all devices as List
	 * 
	 * @return List of all devices
	 */
	public List<Device> getDeviceList() {
		return deviceList;
	}

	/** Returns amount of devices in the list
	 * 
	 * @return amount of devices in the list
	 */
	public int deviceCount() {
		return deviceList.size();
	}
	
	/** Returns Device with the given deviceID
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return Device with the given deviceID
	 * @throws NotFoundException if the Device is not found
	 */
	public Device getDevice(int deviceID) throws NotFoundException{
		for(Device device : deviceList){
			if(device.getDeviceID() == deviceID){
				return device;
			}
		}
		throw new NotFoundException("Device " + deviceID); 
	}
	
	/** Returns Device with the given address
	 * 
	 * @param address Mail address of the Device
	 * @return Device with the given address
	 * @throws NotFoundException if the Device is not found
	 */
	public Device getDevice(String address) throws NotFoundException{
		for(Device device : deviceList){
			if(device.getAddress().equals(address)){
				return device;
			}
		}
		throw new NotFoundException("Device " + address);
	}
	
	/** Adds device to the list
	 * 
	 * @param device Device which should be added
	 * @return true if adding succeeded
	 * @throws AlreadyExistingException if deviceID already exisiting 
	 */
	public boolean addDevice(Device device) throws AlreadyExistingException{
		if(!deviceList.contains(device)){
			return deviceList.add(device);
		}
		throw new AlreadyExistingException("Device " + device.getDeviceID()
			+ ":" + device.getAddress());
	}
	
	/** Iterator for iterating through the list
	 * 
	 */
    @Override
    public Iterator<Device> iterator() {
        return deviceList.iterator();
    }
}
