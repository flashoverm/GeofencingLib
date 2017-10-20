package de.geofencing.client.restClient;

import de.geofencing.client.httpClient.HttpClient;
import de.geofencing.client.httpClient.HttpClientException;
import de.geofencing.client.httpClient.HttpClientJSON;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.device.Device;

/** Provides a client for the restful web service. 
 * 
 * Defined methods are used by both, the RestDeviceClient and the RestAdminClient.
 * For the specific methods the RestDeviceClient or the RestAdminClient has to be used.
 * 
 * @author Markus Thral
 *
 */
public abstract class RestClient {
	
	protected HttpClient client;
	protected String restServiceURL;
	
	/** Constructs new RestClient
	 * 
	 * @param restServiceURL URL of the web service (i.e. https://127.0.0.1:8080/Geofencing)
	 */
	public RestClient(String restServiceURL) {
		client = new HttpClientJSON();
		setRestServiceURL(restServiceURL); 
	}
	
    /** Allows HttpClient to accept all certificates
     * 
     */
	public void allowAllCertificates(){
		client.allowAllCertificates();
	}
	
	/** Sets authorization information transmitted in the authorization header
	 * 
	 * @param authorization Administrator password or Device address
	 */
	public void setAuthorization(String authorization){
		client.setAuthorization(authorization);
	}

	/** Sets URL of the geofencing rest service
	 * 
	 * @param restServiceURL URL of the web service (i.e. https://127.0.0.1:8080/Geofencing)
	 */
	public void setRestServiceURL(String restServiceURL){
		if(restServiceURL.charAt(restServiceURL.length()-1) != '/'){
			this.restServiceURL = restServiceURL + "/";
		} else {
			this.restServiceURL = restServiceURL;
		}
	}
	
	/** Gets state of the web service to check service URL
	 * 
	 * @return true if service is running, false if not reachable
	 */
	public boolean isServiceRunning() {
		try{
			return client.target(restServiceURL+"serviceState")
					.get(Boolean.class);
		} catch(HttpClientException e){
			return false;
		}
	}
	
	/** Gets Device with the given deviceID
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return Device with the given deviceID
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public Device getDevice(int deviceID) throws HttpClientException{
		return client.target(restServiceURL+"devices/"+deviceID)
				.get(Device.class);
	}
		
	/** Gets additional data of the given SystemBeacons from server if available, 
	 * otherwise the beacons are returned without data.
	 *  
	 * @param beacons Beacons to get the data for
	 * @return SystemBeacons object with the beacons
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public SystemBeacons getBeaconData(SystemBeacons beacons) throws HttpClientException{
		return client.target(restServiceURL + "foundBeacons")
				.put(beacons, SystemBeacons.class, SystemBeacons.class);
	}

	/** Deletes device with the given device id from the GeoSystem with the given system id
	 * 
	 * @param deviceID DeviceID of the Device
	 * @return true if Device is deleted
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean deleteDevice(int deviceID) throws HttpClientException{
		return client.target(restServiceURL + "devices/" + deviceID)
				.delete(Boolean.class);
	}

}
