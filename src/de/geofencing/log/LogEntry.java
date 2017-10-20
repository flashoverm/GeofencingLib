package de.geofencing.log;

public class LogEntry{
	
	public static final String serverLogTag = "Geofencing Server Log";
	public static final String clientLogTag = "Geofencing Client log";
	
	private String logEntry;
	
	/** Builds Log entry
	 * 
	 * @param logTag LogTag from class Log
	 * @param message Message
	 */
	public LogEntry(String logTag, String message){
		logEntry = logTag + ": " + message;
	}
	
	/** Builds Log entry
	 * 
	 * @param logTag LogTag from class Log
	 * @param e Exception
	 */
	public LogEntry(String logTag, Exception e){
		logEntry = logTag + ": " + e.getClass() + " - " + e.getMessage() + "\n";
		for(int i=0; i<e.getStackTrace().length; i++){
			logEntry += e.getStackTrace()[i] + "\n";
		}
	}
	
	/** gets built LogEntry
	 * 
	 * @return LogEntry as String message
	 */
	public String getLogEntry(){
		return logEntry;
	}
	
	/** Prints server log on console
	 * 
	 * @param message Message
	 */
	public static void c(String message){
		System.out.println(new LogEntry(serverLogTag, message).getLogEntry());
	}
	
	/** Prints server log on console
	 * 
	 * @param e Exception
	 */
	public static void c(Exception e){
		System.out.println(new LogEntry(serverLogTag, e).getLogEntry());
	}
	
}
