package de.geofencing.system.exceptions;

/** Error with Configuration. File not found or necessary value not set.
 * 
 * @author Markus Thral
 *
 */
public class ConfigurationException extends Exception {

	/** Types of configuration errors
	 * 
	 */
	public enum Error{ValueNotFound, ValueNotSet, FileNotExisiting, CouldntWriteFile, Other};

	private static final long serialVersionUID = 1L;
	private final Error error;
	
	public ConfigurationException(Error error){
		super("Error with Settings: " + error);
		this.error = error;
	}
	
	public ConfigurationException(Exception e, Error error){
		super("Error with Settings: " + error, e);
		this.error = error;
	}
	
	public Error getError(){
		return error;
	}
	
}
