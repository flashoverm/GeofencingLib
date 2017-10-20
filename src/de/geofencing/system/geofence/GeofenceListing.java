package de.geofencing.system.geofence;

import java.io.Serializable;

/** Object for describing and transmitting a Geofence
 * only with minor and description 
 * 
 * @author Markus Thral
 *
 */
public class GeofenceListing implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected final int minor;
	protected final String description;
	
	/** Constructor for serialization
	 * 
	 */
	public GeofenceListing(){
		this.minor = -1;
		this.description = null;
	}
	
	/** Creates new GeofenceListing
	 * 
	 * @param minor Minor of the Geofence
	 * @param description Description of the Geofence
	 */
	public GeofenceListing(int minor, String description){
		this.minor = minor;
		this.description = description;
	}

	public int getMinor() {
		return minor;
	}

	public String getDescription() {
		return description;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + minor;
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
		GeofenceListing other = (GeofenceListing) obj;
		if (minor != other.minor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GeofenceListing [minor=" + minor + ", description=" + description + "]";
	}	
}
