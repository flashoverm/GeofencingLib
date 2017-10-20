package de.geofencing.util;

public class Debugger {
	
	public static boolean debug = false;
	
	/** Prints the stack trace of the given exception.
	 * 
	 * @param exception Exception to be displayed
	 */
	public static void print(Exception exception){
		if(debug){
			Debugger.print("Debugger - Exception triggered:");
			exception.printStackTrace();
		}
	}
	
	/** Prints the given message
	 * 
	 * @param message Message to be displayed
	 */
	public static void print(String message){
		if(debug){
			System.out.println("Debugger: " + message);
		}
	}

}
