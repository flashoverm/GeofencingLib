package de.geofencing.client;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.system.exceptions.ConfigurationException.Error;
import de.geofencing.util.Debugger;

/** Provides methods for reading the client configuration file and setting entries as key-value pairs.
 * 
 * @author Markus Thral
 *
 */
public class ClientConfiguration{

	private static final String serverUrl_key = "ServiceURL";
	private static final String deviceId_key = "DeviceID";
	private static final String token_key = "Token";
	
	protected Context context;
	
	public ClientConfiguration(Context context){
		this.context = context;
	}
	
	/** Gets ServiceURL from the configuration
	 * 
	 * @return URL of the Webservice
	 * @throws ConfigurationException if value not found or couln't read 
	 */
	public String getServiceURL() throws ConfigurationException {
		return getValue(serverUrl_key);
	}
	
	/** Gets DeviceID from the configuration
	 * 
	 * @return DeviceID of the device
	 * @throws ConfigurationException if value not found or couln't read 
	 */
	public int getDeviceID() throws ConfigurationException{
		return Integer.parseInt(getValue(deviceId_key));
	}
	
	/** Gets Firebase token and deletes it from configuration
	 * 
	 * @return Firebase token
	 * @throws ConfigurationException if value could not be read 
	 */
	public String getFirebaseToken() throws ConfigurationException{
		String token = getValue(token_key);
		getClientConfiguration().edit().remove(token_key).commit();
		return token;
	}
	
	/** Sets ServiceURL in configuration
	 * 
	 * @param serviceURL Service URL
	 */
	public void setServiceURL(String serviceURL) {
		setValue(serverUrl_key, serviceURL);
	}
	
	/** Sets DeviceID in configuration
	 * 
	 * @param deviceID DeviceID of the Device
	 */
	public void setDeviceID(int deviceID) {
		setValue(deviceId_key, deviceID+"");
	}

	
	/** Sets Firebase token in configuration
	 * 
	 * @param token Firebase token
	 */
	public void setFirebaseToken(String token) {
		setValue(token_key, token);
	}

	
	/** Gets value from configuration
	 * 
	 * @param key Key to get the value for
	 * @return value of the key
	 * @throws ConfigurationException if key does not exist
	 */
	public String getValue(String key) throws ConfigurationException{
		String value = this.getClientConfiguration().getString(key, null);
		if(value != null){
			return value;
		}
		throw new ConfigurationException(Error.ValueNotFound);
	}
	
	/** Sets key and value or updated key if existing
	 * 
	 * @param key Key to get the value for
	 * @param value Value of the key
	 */
	public void setValue(String key, String value) {
		this.getClientConfiguration().edit().putString(key, value).commit();
		if(Debugger.debug){
			print();
		}
	}

	/** Prints configuration on console
	 * 
	 */
	protected void print(){
		Map<String, ?> config = this.getClientConfiguration().getAll();
		Properties props = new Properties();
		props.putAll(config);
		try {
			System.out.println("\nClient-Configuration: ");
			props.store(System.out, "");
			System.out.println("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected SharedPreferences getClientConfiguration(){
		return context.getSharedPreferences("GeofencingConfiguration", 0);
	}
			
}
