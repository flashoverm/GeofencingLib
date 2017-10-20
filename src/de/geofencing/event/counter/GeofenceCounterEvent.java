package de.geofencing.event.counter;

import de.geofencing.event.Trigger;
import de.geofencing.log.LogEntry;
import de.geofencing.system.exceptions.NotFoundException;

/** Event which modifies a counter which shows the amount of devices in the Geofence.
 * 
 * @author Markus Thral
 *
 */
public class GeofenceCounterEvent extends ModifyCounterEvent {

	private static final long serialVersionUID = 1L;
	
	private Trigger.Direction movement;

	/** Constructor for serializing.
	 * 
	 */
	public GeofenceCounterEvent(){
		super();
	}
	
	/** Creates Event which counts the Devices inside the Geofence.
	 * 
	 * The Counter is generated when the Event is added to the Geofence
	 * 
	 * @param description Description of the Event
	 * @param minor Minor of the Geofence
	 */
	public GeofenceCounterEvent(String description, int minor){
		super(description, minor, null, null);
		this.counterID = -1; 
	}
	
	@Override
	public boolean onAddToGeofence() {
		this.counterID = CounterDBConnector.insertCounter();
		return true;
	};
	
	@Override
	public void checkTrigger(Trigger.Direction direction, int deviceID) {
		try {
			movement = direction;
			this.trigger(deviceID);
		} catch (NotFoundException e) {
			LogEntry.c(e);
		}
	}
	
	@Override
	protected void trigger(int deviceID) throws NotFoundException{
		if(movement.equals(Trigger.Direction.Enter)){
			CounterDBConnector.incrementCounter(counterID);
			return;
		}
		if(movement.equals(Trigger.Direction.Leave)){
			CounterDBConnector.decrementCounter(counterID);
			return;
		}
	}
	
}
