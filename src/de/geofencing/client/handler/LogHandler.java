package de.geofencing.client.handler;

import de.geofencing.log.LogEntry;

/** In the LogHandler should be defined
 *  how log entries should be handled
 * 
 * @author Markus Thral
 *
 */
public abstract interface LogHandler {
	
	/** Is called when information should be logged 
	 *   
	 * @param logEntry LogEntry
	 */
	public abstract void onLogEntry(LogEntry logEntry);

}
