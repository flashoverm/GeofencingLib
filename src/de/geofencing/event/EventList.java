package de.geofencing.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** List of EventListings for transmitting to administration application
 * 
 * @author Markus Thral
 *
 */
public class EventList implements Iterable<EventListing>, Serializable{

	private static final long serialVersionUID = 1L;
	
	private List<EventListing> eventList;
	
	public EventList(){
		eventList = new ArrayList<>();
	}

	public List<EventListing> getEventList(){
		return eventList;
	}
	
	public int eventCount(){
		return eventList.size();
	}
	
	public boolean addEventListing(EventListing event){
		return eventList.add(event);
	}

	@Override
	public Iterator<EventListing> iterator() {
		return null;
	}

}
