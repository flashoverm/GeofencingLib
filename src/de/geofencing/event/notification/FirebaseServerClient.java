package de.geofencing.event.notification;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.geofencing.log.LogEntry;
import de.geofencing.system.SystemConfiguration;
import de.geofencing.system.exceptions.ConfigurationException;

/** Provides methods to send an notification via Firebase Messaging Service.
 * Therefore an HTTP post is send to the Firebase service.
 * 
 * @author Markus Thral
 *
 */
public class FirebaseServerClient {

	/** Firebase topic for administrator notification
	 */
	public static final String adminTopic = "geofenceAdministrators";
	/** Configuration value for administration application server key
	 */
	public static final String configAdminKey = "adminServerKey";
	/** Configuration value for device application server key
	 */
	public static final String configDeviceKey = "deviceServerKey";
	/** Firebase messaging URL
	 */
	public static final String propFirebaseURL = "firebaseUrl";

	/** Generates all fields necessary for the Firebase configuration
	 * 
	 */
	protected static void generateFirebaseConfig() {
		SystemConfiguration.setValue(propFirebaseURL, "https://fcm.googleapis.com/fcm/send");
		SystemConfiguration.setValue(configDeviceKey, "");
		SystemConfiguration.setValue(configAdminKey, "");
		
		LogEntry.c("Added firebase configuration");
	}

	/** Sets up a connection to the Firebase server with the values defined in the Configuration
	 * 
	 * @param targetURL URL of the Firebase service
	 * @param serverKey Serverkey of the Firebase project
	 * @return HTTPURLConnection to the Firebase server
	 * @throws MalformedURLException if the URL is not in correct form
	 * @throws IOException if an I/O exception occurs
	 */
	protected static HttpURLConnection setupConnection(String targetURL, String serverKey)
			throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(targetURL).openConnection();
		conn.setRequestProperty("User-Agent", "GeofencingServer");
		conn.setRequestProperty("Authorization", "key=" + serverKey);
		conn.setRequestProperty("Content-type", "application/json");
		conn.setDoInput(true);
		conn.setRequestProperty("Accept", "application/json, application/x-www-form-urlencoded; charset=UTF-8");
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		return conn;
	}

	/** Sends notification to an Device
	 * 
	 * @param title Title of the notification
	 * @param message Message of the notification
	 * @param deviceToken Firebase token of the Device
	 * @return Response of the Firebase Server
	 */
	public static String sendToDevice(String title, String message, String deviceToken) {
		return send(title, message, deviceToken);
	}

	/** Sends notification to the administrator applications
	 * 
	 * @param title Title of the notification
	 * @param message Message of the notification
	 * @return Response of the Firebase Server
	 */
	public static String sendToAdmins(String title, String message) {
		return send(title, message, null);
	}
	
	/** Sends notification to the destination or to the administrator application if the destination is null
	 * 
	 * @param title Title of the notification
	 * @param message Message of the notification
	 * @param destination Firebase token of the Device or null
	 * @return Response of the Firebase Server
	 */
	protected static String send(String title, String message, String destination) {
		try {
			String to;
			String serverKeyProperty;
			if (destination != null) {
				to = destination;
				serverKeyProperty = configDeviceKey;
			} else {
				to = "/topics/"+adminTopic;
				serverKeyProperty = configAdminKey;
			}

			HttpURLConnection conn = setupConnection(SystemConfiguration.getValue(propFirebaseURL),
					SystemConfiguration.getValue(serverKeyProperty));
			OutputStream out = conn.getOutputStream();

			out.write(builtBody(title, message, to).getBytes());

			if (conn.getResponseCode() == 200) {
				Scanner stream = new Scanner(conn.getInputStream(), "UTF-8");
				String input = stream.useDelimiter("\\A").next();
				stream.close();
				return input;
			}
			LogEntry.c("Error sending to Firebase Server - ErrorCode: " + conn.getResponseCode());

		} catch (IOException e) {
			LogEntry.c(e);
		} catch (ConfigurationException e) {
			if (e.getError() == ConfigurationException.Error.FileNotExisiting
					|| e.getError() == ConfigurationException.Error.ValueNotFound) {
				generateFirebaseConfig();
			} else{
				LogEntry.c(e);
			}
		}
		return null;
	}

	/** Builts body of the HTTP request in JSON format
	 * 
	 * @param title Title of the notification
	 * @param message Message of the notification
	 * @param to Firebase token of the destination
	 * @return Request body in JSON format
	 */
	protected static String builtBody(String title, String message, String to) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode body = mapper.createObjectNode();
		ObjectNode notification = mapper.createObjectNode();
		notification.put("title", title);
		notification.put("body", message);
		body.put("notification", notification);
		body.put("to", to);

		return body.toString();
	}
}
