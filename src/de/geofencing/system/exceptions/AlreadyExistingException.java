package de.geofencing.system.exceptions;

/** Object which should be created, is already existing
 * (triggers HTTP 409 Conflict)
 * 
 * @author Markus Thral
 *
 */
public class AlreadyExistingException extends Exception {

	private static final long serialVersionUID = 1L;

	public AlreadyExistingException(){
		super("Object is already existing");
	}
	
	public AlreadyExistingException(String object){
		super("Object is already existing: " + object);
	}
}
