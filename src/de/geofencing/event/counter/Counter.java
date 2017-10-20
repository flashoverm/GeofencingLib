package de.geofencing.event.counter;

import java.io.Serializable;

/** Represents an Counter with counterID and value
 * 
 * @author Markus Thral
 *
 */
public class Counter implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected final int counterID;
	protected int value;
	
	/** Constructor for serialization
	 * 
	 */
	public Counter(){
		this.counterID = -1;
		this.value = 0;
	}
	
	/** Creates new counter with given counterID.
	 *  Value is set to zero
	 * 
	 * @param counterID counterID of the Counter
	 */
	public Counter(int counterID){
		this.counterID = counterID;
		this.value = 0;
	}

	public int getCounterID(){
		return this.counterID;
	}
	
	public int getValue(){
		return this.value;
	}
	
	/** Increments counter
	 * 
	 * @return new value of the counter
	 */
	public int increment(){
		this.value ++;
		return this.value;
	}
	
	/** Decrements counter
	 * 
	 * @return new value of the counter
	 */
	public int decrement(){
		if(this.value >0){
			this.value --;			
		}
		return this.value;
	}
	
	/** Sets counter value to zero
	 * 
	 */
	public void reset(){
		this.value = 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + counterID;
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
		Counter other = (Counter) obj;
		if (counterID != other.counterID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Counter [counterID=" + counterID + ", value=" + value + "]";
	}
}
