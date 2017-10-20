package de.geofencing.event.counter;

import de.geofencing.event.Event;
import de.geofencing.event.Trigger;
import de.geofencing.system.exceptions.NotFoundException;

/** Event which changes the value of an Counter if triggered.
 * The changing mode can be defined as in-/decrement and reset.
 * 
 * The Counter is generated when the Event is added to the Geofence
 * 
 * @author Markus Thral
 *
 */
public class ModifyCounterEvent extends Event{
	
	/** Describes the modification of the counter:
	 * Increment, decrement or reset to zero 
	 */
	public enum Mode {increment, decrement, reset}

	private static final long serialVersionUID = 1L;

	protected int counterID;
	protected final Mode mode;
	
	/** Constructor for serializing.
	 * 
	 */
	public ModifyCounterEvent(){
		super();
		this.counterID = -1; 
		this.mode = null;
	}
	
	/** Creates Event which modifies the value of a Counter. 
	 * The Counter is generated when the Event is added to the Geofence
	 * 
	 * @param description Description of the Event
	 * @param minor Minor of the Geofence
	 * @param trigger Trigger object which describes the behavior of the device to trigger the Event
	 * @param mode Mode of the Event modifying the Counter (i.e. increment)
	 */
	public ModifyCounterEvent(String description, int minor, Trigger trigger, Mode mode){
		super(description, minor, trigger);
		this.counterID = -1; 
		this.mode = mode;
	}
	
	/** Creates Event which modifies the value of the Counter with the given counterID
	 * 
	 * @param counterID	CounterID of the couter
	 * @param description Description of the Event
	 * @param minor Minor of the Geofence
	 * @param trigger Trigger object which describes the behavior of the device to trigger the Event
	 * @param mode Mode of the Event modifying the Counter (i.e. increment)
	 */
	public ModifyCounterEvent(int counterID, String description, int minor, Trigger trigger, Mode mode){
		super(description, minor, trigger);
		this.counterID = counterID; 
		this.mode = mode;
	}
	
	public int getCounterID(){
		return counterID;
	}
	
	public Mode getMode(){
		return mode;
	}
	
	@Override
	public boolean onAddToGeofence() {
		if(this.counterID == -1){
			this.counterID = CounterDBConnector.insertCounter();
		} else{
			try{
				CounterDBConnector.findCounter(counterID);
			} catch(NotFoundException e){
				return false;
			}
		}
		return true;
	};

	@Override
	protected void trigger(int deviceID) throws NotFoundException{
		if(mode.equals(Mode.increment)){
			CounterDBConnector.incrementCounter(counterID);
			return;
		}
		if(mode.equals(Mode.decrement)){
			CounterDBConnector.decrementCounter(counterID);
			return;
		}
		if(mode.equals(Mode.reset)){
			CounterDBConnector.resetCounter(counterID);
			return;
		}
	}
}
