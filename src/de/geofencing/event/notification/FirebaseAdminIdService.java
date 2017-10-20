package de.geofencing.event.notification;

import com.google.firebase.iid.FirebaseInstanceIdService;

/** Implementation of FirebaseInstanceIdService for GeofencingAdmin.
 * Subscribes as administrator on token refresh.
 * 
 * @author Markus Thral
 *
 */
public class FirebaseAdminIdService extends FirebaseInstanceIdService {

	@Override
	public void onTokenRefresh() {
		FirebaseClient.subscribeAsAdministrator();
	}
}
