package de.geofencing.event;

import java.io.Serializable;

/** Defines how a Event is triggered with direction and optional delay
 * 
 * @author Markus Thral
 *
 */
public class Trigger implements Serializable{

	private static final long serialVersionUID = 1L;

	/** Direction of the movement of the Device:
	 * Enter or leave the geofence
	 * 
	 */
	public enum Direction{Enter, Leave};
	
	protected final Direction direction;
	protected final int delay;			

	/** Creates Trigger with direction and delay, after which 
	 * the event is triggered if the condition is still fulfilled
	 * 
	 * @param direction Trigger.Direction enter or leave
	 * @param delay in seconds after the Event is triggered
	 */
	public Trigger(Direction direction, int delay){
		this.direction = direction;
		this.delay = delay;
	}

	/** Creates Trigger with direction. Triggers immediately
	 * 
	 * @param direction Trigger.Direction enter or leave
	 */
	public Trigger(Direction direction){
		this.direction = direction;
		this.delay = 0;
	}
	
	/** Constructor for serialization
	 * 
	 */
	public Trigger(){
		this.direction = null;
		this.delay = 0;
	}

	public Direction getDirection(){
		return this.direction;
	}
	
	public int getDelay(){
		return this.delay;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + delay;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
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
		Trigger other = (Trigger) obj;
		if (delay != other.delay)
			return false;
		if (direction != other.direction)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trigger [direction=" + direction + ", delay=" + delay + "]";
	}
	
	
}
