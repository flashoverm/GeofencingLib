package de.geofencing.system.beacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.geofencing.event.BeaconChange;
import de.geofencing.system.exceptions.NotFoundException;

/** Represents a list of beacons and can compare 
 * two lists and generate a BeaconChange log
 *  
 * @author Markus Thral
 *
 */
public class SystemBeacons implements Serializable, Iterable<SystemBeacon>{

	private static final long serialVersionUID = 1L;

	private List<SystemBeacon> beaconList;
	
	/** Creates a new list of beacons
	 * 
	 */
	public SystemBeacons(){
		beaconList = new ArrayList<>();
	}

	/** Returns list of all beacons as List object
	 * 
	 * @return List of all beacons
	 */
	public List<SystemBeacon> getBeaconList() {
		return beaconList;
	}
	
	/** Returns amount of beacons in the list
	 * 
	 * @return amount of beacons in the list
	 */
	public int beaconCount(){
		return beaconList.size();
	}
	
	/** Checks if the list contains beacons of the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @return true if beacons of the Geofence contained, false if not
	 */
	public boolean containsBeaconWithMinor(int minor) {
		for(SystemBeacon b : beaconList){
			if(b.getMinor() == minor ){
				return true;
			}
		}
		return false;
	}
	
	/** Returns beacon from the list
	 * 
	 * @param major Major of the beacon
	 * @param minor Minor of the Geofence
	 * @return SystemBeacon with the given values
	 * @throws NotFoundException if beacon not found
	 */
	public SystemBeacon getBeacon(int major, int minor) throws NotFoundException{
		for(SystemBeacon b : beaconList){
			if(b.getMajor() == major && b.getMinor() == minor ){
				return b;
			}
		}
		throw new NotFoundException("Beacon "+major+":"+minor);
	}

	/** Sets given list of beacons as new beacon list
	 * 
	 * @param beaconList List of beacons to be set
	 */
	private void setBeaconList(List<SystemBeacon> beaconList) {
		this.beaconList = beaconList;
	}
	
	/** Adds beacon to the list
	 * 
	 * @param b Beacon which should be added
	 * @return true if beacon added, false if not
	 */
	public boolean addBeacon(SystemBeacon b){
		if(beaconList.contains(b)){
			return false;
		}
		return beaconList.add(b);
	}
	
	/** Removes beacon from the list
	 * 
	 * @param b Bacon which should be removed
	 * @return true of beacon removed
	 * @throws NotFoundException if the beacon is not found
	 */
	public boolean removeBeacon(SystemBeacon b) throws NotFoundException{
		if(beaconList.remove(b)){
			return true;
		}
		throw new NotFoundException("Beacon " + b.getMajor()+":"+b.getMinor());
	}
	
	/** Removes every beacon from the list
	 * 
	 */
    public void removeAll(){
    	this.beaconList.clear();
    }
	
	/** Iterator for iterating through the list
	 * 
	 */
    @Override
    public Iterator<SystemBeacon> iterator() {
        return beaconList.iterator();
    }
    
	/** Compares SystemBeacons object with this object and updates this object
     * 
     * @param updated  Updated list of beacons as SystemBeacons object
     * @return BeaconChangeLog object with new and missing beacons, null if lists are equals
     */
    public BeaconChange compare(SystemBeacons updated){
    	int listSize = this.beaconCount(); 
    	BeaconChange beaconChange = null;
    	for (SystemBeacon beacon : updated){
    		if(beaconList.contains(beacon)){
    			//beacon in both lists
    			listSize--;    		
    		}
    		else{
    			//beacon only in updatedList
    			if(beaconChange == null){
    				beaconChange = new BeaconChange();
    			}
    			beaconChange.addEnteredBeacon(beacon);
    		}
    	}
    	if(listSize >0){
    		//some (listSize) beacons missing
        	for (SystemBeacon beacon : beaconList){
        		if(!updated.beaconList.contains(beacon)){
        			
        			//beacon only in beaconList
        			if(beaconChange == null){
        				beaconChange = new BeaconChange();
        			}
        			beaconChange.addLeftBeacon(beacon);
        		}
        	}
    	}
		if(beaconChange != null){
	    	//Overwrite this list with updated list
	    	this.setBeaconList(updated.getBeaconList());
	    }
    	return beaconChange;
    }
}
