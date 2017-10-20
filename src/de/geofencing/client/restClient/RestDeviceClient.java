package de.geofencing.client.restClient;

import de.geofencing.client.httpClient.HttpClientException;
import de.geofencing.system.beacon.SystemBeacons;

/** Provides a client for the restful web service with all functions the the GeofencingDevice.
 *
 * @author Markus Thral
 *
 */
public class RestDeviceClient extends RestClient {
	
	public RestDeviceClient(String restServiceURL) {
		super(restServiceURL);
	}
	
	/** Adds Device to the system. The address of the Device has to
	 *  be set as authorization before this method is called.
	 * 
	 * @return Generated deviceID
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public int registerDevice() throws HttpClientException{
		return client.target(restServiceURL + "device")
				.post(Integer.class);
	}

	/** Updates beacons in range of the Device 
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param beacons Beacons in range of the Device as SystemBeacons object
	 * @return true if Device is updated
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean updateDevice(int deviceID, SystemBeacons beacons) throws HttpClientException{
		return client.target(restServiceURL + "devices/" + deviceID + "/update")
				.put(beacons, SystemBeacons.class, Boolean.class);
	}
	
	/** Updates Firebase token of the Device
	 * 
	 * @param deviceID DeviceID of the Device
	 * @param deviceToken Firebase token of the Device
	 * @return true if token is updated
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean updateDeviceToken(int deviceID, String deviceToken) throws HttpClientException{
		return client.target(restServiceURL + "devices/" + deviceID + "/update/token")
				.put(deviceToken, String.class, Boolean.class);
	}
}
