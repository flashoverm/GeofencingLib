package de.geofencing.client.handler;

/** In the InfoHandler should be defined
 *  how informations for the user should be displayed
 * 
 * @author Markus Thral
 *
 */
public abstract interface InfoHandler {

	/** Is called when information should be shown to the user,
	 *  should contain a way to display information to the user 
	 *   
	 * @param message Message which should be shown
	 */
	public abstract void onInfo(String message);
}
