package de.geofencing.system.exceptions;

/** Access to object is restricted or authentication is incorrect
 * (triggers HTTP 401 Unauthorized)
 * 
 * @author Markus Thral
 *
 */
public class UnauthorizedExcpetion extends Exception {
	
	private static final long serialVersionUID = 1L;

	public UnauthorizedExcpetion(){
		super("Not authenticated or wrong credentials");
	}
}
