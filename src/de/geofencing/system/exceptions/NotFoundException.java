package de.geofencing.system.exceptions;

/** Requested object is not existing
 * (triggers HTTP 404 Not Found)
 * 
 * @author Markus Thral
 *
 */
public class NotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotFoundException(){
		super("Requested object not found");
	}
	
	public NotFoundException(String object){
		super("Requested object not found: " + object);
	}
	
}
