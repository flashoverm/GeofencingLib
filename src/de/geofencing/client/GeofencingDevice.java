package de.geofencing.client;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import de.geofencing.client.beaconScanner.BeaconChangeHandler;
import de.geofencing.client.handler.LogHandler;
import de.geofencing.client.httpClient.HttpClientException;
import de.geofencing.client.httpClient.HttpClientException.ErrorCode;
import de.geofencing.client.networkTask.NetworkTask;
import de.geofencing.client.restClient.RestDeviceClient;
import de.geofencing.log.LogEntry;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.device.Device;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.util.Util;

/** The GeofencingDevice is used to scan for beacons and send them to the server.
 * Therefore the Device registers on the server. If the Device is already registered
 * the corresponding data is loaded.  
 * 
 *  TODO Necessary permissions
 *  	INTERNET, ACCESS_NETWORK_STATE
 *  	BLUETOOTH, BLUETOOTH_ADMIN
 *  	WRITE_EXTERNAL_STORAGE
 *  	GET_ACCOUNTS
 * 
 * @author Markus Thral
 *
 */
public class GeofencingDevice extends GeofencingClient{
	
	private boolean registered;
	private Device clientDevice;
	
	private boolean beaconDataRequest;
		
	/** Creates a new GeofencingDevice. Overwrites saved restServiceURL
	 *  Has to be started with start() and could be stopped with stop().
	 * 
	 * @param activity Activity where GeofencingAdmin is used
	 * @param restServiceURL URL of the web service as String 
	 * (i.e. https://127.0.0.1:8080/Geofencing)
	 * @param logHandler LogHandler for handling log entries
	 */
	public GeofencingDevice(Activity activity, String restServiceURL, LogHandler logHandler){
		super(activity);
		this.beaconDataRequest = false;
		this.logHandler = logHandler;
		configuration.setServiceURL(restServiceURL);
   	}
	
	/** Creates a new GeofencingDevice and loads properties from the file.
	 *  Has to be started with start() and could be stopped with stop().
	 * 
	 * @param activity Activity where GeofencingAdmin is used
	 */
	public GeofencingDevice(Activity activity){
		super(activity);
		this.beaconDataRequest = false;
	}

	/** Gets if Device is registered
	 * 
	 * @return true if registered, false if not
	 */
	public boolean isRegistered(){
		return registered;
	}
	
	/** Gets with the data of this device
	 * 
	 * @return Device object, null if not set
	 */
	public Device getClientDevice(){
		return clientDevice;
	}

	
	/*
	 * System functions
	 */
	
	/** Return mail address which is related to Google Play
	 * 
	 * @return Mail address related to Google Play
	 */
	protected String getLocalAccount(){
        Account[] accounts = AccountManager.get(activity.getApplicationContext()).getAccountsByType("com.google");
        return accounts[0].name;
	}
	
	@Override
	protected void setupServerConnection(String restServiceURL){
		this.restClient = new RestDeviceClient(restServiceURL);
		this.restClient.setAuthorization(this.getLocalAccount());
	}
		
	@Override
	protected void startInteractingWithServer(){
		new LoadDeviceDataTask().runTask(logHandler);
	}
	
	@Override
	protected void stopInteractingWithServer(){
		beaconScanner.stopScan();
	}
	
	@Override
	protected void restartInteractingWithServer() {
		beaconScanner.startScan();
	}
 
	
	/*
	 * Beacon scanner
	 */
	
	/** If set true, additional data for beacons in range are received from the system
	 * 
	 * @param status True if additional data should be received, false if not
	 */
	public void setBeaconDataRequest(boolean status){
		this.beaconDataRequest = status;
	}

	/** Sets BeaconChangeListener for BeaconScanner. Updated beacons are sent the the server 
	 *  and defined BeaconChangeListener is called.
     * Occurring errors will be handled by the LogHandler.
	 */
	protected void setBeaconChangeListener(){
		beaconScanner.setBeaconChangeListener(new BeaconChangeHandler() {
			
			@Override
			public void onBeaconsChangeDetected(SystemBeacons inRange) {
				if(beaconDataRequest){
					new GetBeaconDataTask().runTask(logHandler, inRange);
				} else {
					bch.onBeaconsChangeDetected(inRange);
				}
				new UpdateTask().runTask(logHandler, inRange);
			}
		});
	}

	
	/*
	 * Device - Server communication
	 */
	
	/** Task which gets Device information and beacons in range from the system.
	 *  If Task succeeded the Device information will be stored in memory.
	 *  If device is not found on server, the device will be registered, 
     * Occurring errors will be handled by the LogHandler.
	 */
	private class LoadDeviceDataTask extends NetworkTask<Void, Void, Boolean> {
		@Override
		protected Boolean runInBackground(Void... params) throws Exception{
			try{
				clientDevice = ((RestDeviceClient)restClient).getDevice(configuration.getDeviceID());
		      	return true;
			} catch(ConfigurationException e){
				return false;
			}
		}
		@Override
		protected void onSuccess(Boolean result) {
			if(result){
				registered = true;
		           if(logHandler != null){
						logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Device information loaded"));
		           }
				beaconScanner.setBeaconsInRange(
						Util.SystemBeaconsToBeaconList(clientDevice.getBeacons()));
				beaconScanner.startScan();
				return;
				}
			this.afterFailure();
		}
		
		protected void afterFailure() {
           if(logHandler != null){
				logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "DeviceID not found - Try to register"));
           }
			new RegisterTask().runTask(logHandler);
		}		
	}
		
	/** Task which registers Device on the system. 
	 * If the Firebase token is set in Configuration, it will be sent to the server.
	 *  If Task succeeded the device-data will be stored in memory. 
	 *  Occurring errors will be handled by the LogHandler.
	 */
    private class RegisterTask extends NetworkTask<Void, Void, Integer> {
    	
		@Override
		protected Integer runInBackground(Void... params) throws Exception{
			int deviceID = ((RestDeviceClient)restClient).registerDevice();
			try{
				String token = configuration.getFirebaseToken();
				((RestDeviceClient)restClient).updateDeviceToken(deviceID, token);
			} catch (ConfigurationException e){	
				
			} catch (HttpClientException e){
				if(e.getErrorCode().equals(ErrorCode.ObjectAlreadyExisting)){
					return null;
				}
				throw e;
			}
			return deviceID; 
		}
		@Override
		protected void onSuccess(Integer result) {
			if(result != null){
				clientDevice = new Device(result, GeofencingDevice.this.getLocalAccount());
				registered = true;
		        if(logHandler != null){
		        	logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Device registered"));
		        }
	        	configuration.setDeviceID(result);
				beaconScanner.startScan();
				return;
			} else {
		           if(logHandler != null){
						logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Device-Address already registered"));
		           }
			}
		}
    }

    /** Task which updates beacons in range if the Device.
     * Only registered beacons are stored in the Device object.
     * Occurring errors will be handled by the LogHandler.
     */
    private class UpdateTask extends NetworkTask<SystemBeacons, Void, Boolean> {
    	
		@Override
		protected Boolean runInBackground(SystemBeacons... params) throws Exception{
			return ((RestDeviceClient)restClient).updateDevice(clientDevice.getDeviceID(), params[0]);
		}
    }
  
    /** Stops interacting with server and scanning for beacons
     * and removes own device from server.
     * 
     * After restarting the client the Device is registered again.
     */
    public void unregister(){
    	this.stop();
    	new RemoveDeviceTask().runTask(logHandler, clientDevice.getDeviceID());
    }	
}
