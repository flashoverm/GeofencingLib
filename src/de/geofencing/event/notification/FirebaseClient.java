package de.geofencing.event.notification;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import de.geofencing.client.ClientConfiguration;
import de.geofencing.client.networkTask.NetworkTask;
import de.geofencing.client.restClient.RestDeviceClient;
import de.geofencing.system.exceptions.ConfigurationException;

/**
 * Connects with geofencing web service and transmits the Firebase token for
 * Device- and subscribes on the topic for AdminNotification.
 * 
 * @author Markus Thral
 *
 */
public class FirebaseClient {

	private Context context;
	private ClientConfiguration configuration;
	private RestDeviceClient client;
	private int deviceID;

	/** Creates FirebaseClient to communicate with the geofencing web service
	 * 
	 * @param context Application context
	 */
	public FirebaseClient(Context context) {
		this.context = context;
		this.configuration = new ClientConfiguration(context);
	}

	/** Subscribes FirebaseClient for administrator notifications
	 * 
	 */
	public static void subscribeAsAdministrator() {
		FirebaseMessaging.getInstance().subscribeToTopic(FirebaseServerClient.adminTopic);
	}

	/** Gets Firebase token on token refresh
	 * 
	 * @return Firebase token as String
	 */
	public static String getToken() {
		return FirebaseInstanceId.getInstance().getToken();
	}

	/** Sends Firebase Token to the server and updates Device.
	 * If device not registered, the token is stored in the configuration
	 * 
	 */
	public void sendToken() {
		String serviceURL;
		try{
			deviceID = configuration.getDeviceID();
			serviceURL = configuration.getServiceURL();
		} catch(ConfigurationException e){
			configuration.setFirebaseToken(getToken());
			return;
		}
		Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
		client = new RestDeviceClient(serviceURL);
		client.setAuthorization(accounts[0].name);
		
		new UpdateTokenTask().runTask(null, getToken());
	}

	/** Network Task which sends Firebase token to the server
	 * 
	 */
	private class UpdateTokenTask extends NetworkTask<String, Void, Boolean> {

		@Override
		protected Boolean runInBackground(String... params) throws Exception {
			return client.updateDeviceToken(deviceID, params[0]);
		}
	}
}
