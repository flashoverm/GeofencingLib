package de.geofencing.client.handler;

/** In the LoginHandler should be defined what happens
 * if the set credentials are correct.
 * For example: Switch from login window to main activity.
 * 
 * @author Markus Thral
 *
 */
public abstract interface LoginHandler {

	/** Is called when the set credentials are checked 
	 * by the server successfully.
	 */
	public abstract void onLogin();
}
