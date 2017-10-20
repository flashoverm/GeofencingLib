package de.geofencing.system.geofence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** List of GeofenceListings for transmitting to administration application
 * 
 * @author Markus Thral
 *
 */
public class GeofenceList implements Serializable, Iterable<GeofenceListing> {

	private static final long serialVersionUID = 1L;
	
	private List<GeofenceListing> geofenceList;
	
	public GeofenceList(){
		geofenceList = new ArrayList<>();
	}
	
	public List<GeofenceListing> getGeofenceList(){
		return geofenceList;
	}

	public boolean addGeofenceListing(GeofenceListing geofence) {
		return geofenceList.add(geofence);
	}
	
	/** Iterator for iterating through the list
	 * 
	 */
    @Override
    public Iterator<GeofenceListing> iterator() {
        return geofenceList.iterator();
    }
}