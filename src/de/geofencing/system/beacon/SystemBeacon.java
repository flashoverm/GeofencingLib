package de.geofencing.system.beacon;

import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;

/** Represents a beacon in the system with location description
 * or in range of a device without location description
 * 
 * @author Markus Thral
 *
 */
public class SystemBeacon implements Serializable, Comparator<SystemBeacon>{

	private static final long serialVersionUID = 1L;

    private final UUID uuid;
	private final int major;
	private final int minor;
	private String location;
	
	/** Constructor for serializing
	 * 
	 */
	SystemBeacon(){
		this.uuid = null;
		this.major = -1;
		this.minor = -1;
		this.location = null;
	}
	
	/** Creates new beacon with location description.
	 * 
	 * @param uuid UUID of the beacon
	 * @param major Major of the beacon
	 * @param minor Minor of the beacon
	 * @param location Location description of the beacon
	 */
	public SystemBeacon(UUID uuid, int major, int minor, String location) {
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
		this.location = location;
	}

	/** Creates new beacon without location description.
	 * 
	 * @param uuid UUID of the beacon
	 * @param major Major of the beacon
	 * @param minor Minor of the beacon
	 */
	public SystemBeacon(UUID uuid, int major, int minor) {
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
		this.location = null;
	}

	/** Gets locations description
	 * 
	 * @return description or null if not set
	 */
	public String getLocation() {
		return location;
	}

	public UUID getUUID() {
		return uuid;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + major;
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
		SystemBeacon other = (SystemBeacon) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if(location != null){
			return "Beacon [uuid=" + uuid + ", major=" + major + ", minor=" + minor + ", location=" + location + "]";
		}
		return "Beacon [uuid=" + uuid + ", major=" + major + ", minor=" + minor + "]";
	}

	@Override
	public int compare(SystemBeacon o1, SystemBeacon o2) {
		if(o1.getUUID().equals(o2.getUUID())){
			
			if(o1.getMajor() == o2.getMajor()){
				
				if(o1.getMinor() == o2.getMinor()){
					return 0;
				}
				if(o1.getMinor() < o2.getMinor()){
					return -1;
				}
				return 1;
			}
			if(o1.getMajor() < o2.getMajor()){
				return -1;
			}
			return 1;
		}
		return o1.getUUID().compareTo(o2.getUUID());
	}
	
}
