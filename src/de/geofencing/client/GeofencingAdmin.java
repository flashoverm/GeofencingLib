package de.geofencing.client;

import android.app.Activity;
import de.geofencing.client.beaconScanner.BeaconChangeHandler;
import de.geofencing.client.handler.GeneratedBeaconHandler;
import de.geofencing.client.handler.SystemDataHandler;
import de.geofencing.client.networkTask.NetworkTask;
import de.geofencing.client.restClient.RestAdminClient;
import de.geofencing.event.Event;
import de.geofencing.event.EventList;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.device.Device;
import de.geofencing.system.device.Devices;
import de.geofencing.system.geofence.GeofenceList;
import de.geofencing.system.geofence.GeofenceListing;

/** The GeofencingAdmin provides functions for maintaining 
 * the geofencing system and display the data. 
 * 
 *  TODO Necessary permissions
 *  	INTERNET, ACCESS_NETWORK_STATE
 *  	BLUETOOTH, BLUETOOTH_ADMIN
 *  	WRITE_EXTERNAL_STORAGE
 *  
 * @author Markus Thral
 *
 */
public class GeofencingAdmin extends GeofencingClient{
	
	private SystemDataHandler systemDataHandler;
	private GeneratedBeaconHandler generatedBeaconHandler;
	
	private String password;
		
	/** Creates new GeofencingAdminClient
	 *  Has to be started with method start() and could be stopped with method stop().
	 *   
	 * @param activity Activity where GeofenceAdmin is used
	 */
	public GeofencingAdmin(Activity activity){
		super(activity);
	}
	
	/*
	 * System functions
	 */

	/** Sets the administrator password for the system.
	 * Should be checked before using here, 
	 * for example with GeofencingAdminLogin class.
	 * 
	 * @param password Password of the administrator
	 */
	public void setPassword(String password){
		this.password = password;
		if(this.restClient != null){
			this.restClient.setAuthorization(password);
		}
	}
	
	@Override
	protected void setupServerConnection(String restServiceURL){
		this.restClient = new RestAdminClient(restServiceURL);
		if(password != null){
			restClient.setAuthorization(password);
		}
	}

	@Override
	protected void startInteractingWithServer(){ }
	
	@Override
	protected void restartInteractingWithServer() {	}
	
	@Override
	protected void stopInteractingWithServer(){
		 beaconScanner.stopScan();
	}

	
	/*
	 * Beacon scanner
	 */
	
	/** Starts scanning for beacons in range
	 * 
	 * @return true if starting succeeded, false if not
	 */
	public boolean startScanning(){
		return beaconScanner.startScan();
	}
	
	/** Stops scanning for beacons in range
	 * 
	 * @return true if stopping succeeded, false if not
	 */
	public boolean stopScanning(){
		 beaconScanner.stopScan();
		 return beaconScanner.deactivateBluetooth();
	}

	/** Sets BeaconChangeListener for BeaconScanner and user-defined 
	 * BeaconChangeListener is called.
	 */
	@Override
	protected void setBeaconChangeListener(){
		beaconScanner.setBeaconChangeListener(new BeaconChangeHandler() {
			
			@Override
			public void onBeaconsChangeDetected(SystemBeacons inRange) {
				if(restClient == null){
					new GetBeaconDataTask().runTask(logHandler, inRange);
				}
				else if(bch != null){
					bch.onBeaconsChangeDetected(inRange);
				}
			}
		});
	}
	
	
	/*
	 * Functions for data-updates from server
	 */
	
	/** Gets data of the geofencing system from the server (Devices and geofences)
	 * Received data are handler by the SystemDataHandler
	 */
	public void getSystemData(){
		new SystemDataUpdate().runTask(logHandler);
	}
	
	/** Gets data of the Geofence from the server (Events and beacons)
	 * Received data are handler by the SystemDataHandler
	 * 
	 * @param geofence GeofenceListing object to get the data for
	 */
	public void getGeofence(GeofenceListing geofence){
		new GeofenceDataUpdate().runTask(logHandler, geofence);
	}

	/** Sets the handler for the received data of the system and the Geofence. 
	 * 
	 * @param systemDataHandler SystemDataHandler object
	 */
	public void setSystemDataHandler(SystemDataHandler systemDataHandler){
		this.systemDataHandler = systemDataHandler;
	}

    /** Task which gets geofences and registered devices from the server.
     * Occurring errors will be handled by the logHandler.
     */
    private class SystemDataUpdate extends NetworkTask<Void, Void, Void> {
    	
    	private GeofenceList geofences;
    	private Devices registeredDevices;
    	
		@Override
		protected Void runInBackground(Void... params) throws Exception {
			if(systemDataHandler != null){
				geofences = ((RestAdminClient)restClient).getGeofenceList();
				registeredDevices = ((RestAdminClient)restClient).getDevices();
			}
			return null;
		}
		@Override
		protected void onSuccess(Void result) {
			if(geofences != null && registeredDevices != null){
				systemDataHandler.onUpdateDevices(registeredDevices);
				systemDataHandler.onUpdateGeofences(geofences);
			}
		}
    }
    
    /** Task which gets events and registered beacons for a Geofence from the server.
     * Occurring errors will be handled by the logHandler.
     */
    private class GeofenceDataUpdate extends NetworkTask<GeofenceListing, Void, GeofenceListing> {
    	
    	private EventList events;
    	private SystemBeacons beacons;
    	
		@Override
		protected GeofenceListing runInBackground(GeofenceListing... params) throws Exception {
			if(systemDataHandler != null){
				events = ((RestAdminClient)restClient).getEvents(params[0].getMinor());
				beacons = ((RestAdminClient)restClient).getBeacons(params[0].getMinor());
			}
			return params[0];
		}
		@Override
		protected void onSuccess(GeofenceListing result) {
			if(events != null && beacons != null){
				systemDataHandler.onUpdateGeofence(result, events, beacons);
			}
		}
    }
    
    
    /*
     * Generate Beacon 
     */
       
    /** Sets handler for a generated beacon for a Geofence
     * 
     * @param generatedBeaconHandler GeneratedBeaconHandler object
     */
    public void setGeneratedBeaconHandler(GeneratedBeaconHandler generatedBeaconHandler){
    	this.generatedBeaconHandler = generatedBeaconHandler;
    }
    
    /** Generates beacon for the Geofence
     * 
     * @param minor Minor of the Geofence
     */
    public void generateBeacon(int minor){
		new GenerateBeaconTask().runTask(logHandler, minor);
    }
    
	/** Task generated beacon for the Geofence
     * Occurring errors will be handled by the LogHandler.
     */
	private class GenerateBeaconTask extends NetworkTask<Integer, Void, SystemBeacon> {

		@Override
		protected SystemBeacon runInBackground(Integer... arg0) throws Exception {
			return ((RestAdminClient)restClient).generateBeacon(arg0[0]);
		}

		@Override
		protected void onSuccess(SystemBeacon result) {
			generatedBeaconHandler.onBeaconGenerated(result);
		}
	}
	
	
	/*
	 * 	Functions for modifying the system
	 */

    /** Adds Geofence to the system
     * 
     * @param description Description of the Geofence
     */
    public void addGeofence(String description){
		new AddGeofenceTask().runTask(logHandler, description);
    }
    
    /** Adds Event or derivation to the Geofence
     * 
     * @param event Event object or derivation
     */
    public void addEvent(Event event){
		new AddEventTask().runTask(logHandler, event);
    }
	
	/** Adds a SystemBeacon with the given location description to the Geofence
	 * 
	 * @param beacon SystemBeacon object to be added
	 * @param location Location description of the beacon
	 */
	public void addBeacon(SystemBeacon beacon, String location){
		beacon.setLocation(location);
		new AddBeaconTask().runTask(logHandler, beacon);
	}
	
	/** Removes Event from the Geofence
	 * 
	 * @param minor Minor of the Geofence
	 * @param eventID EventID of the Event
	 */
	public void removeEvent(int minor, int eventID){
		new RemoveEventTask().runTask(logHandler, new Integer[]{minor,eventID});
	}
	
	/** Removes beacon from the Geofence
	 * 
	 * @param beacon SystemBeacon to be removed
	 */
	public void removeBeacon(SystemBeacon beacon){
		new RemoveBeaconTask().runTask(logHandler, beacon);
	}
	
	/** Removes Geofence from the system (if no more beacons and events left)
	 * 
	 * @param minor Minor of the Geofence
	 */
	public void removeGeofence(int minor){
		new RemoveGeofenceTask().runTask(logHandler, minor);
	}
	
	/** Removes Device from the system
	 * 
	 * @param device Device to be removed
	 */
	public void removeDevice(Device device){
		new RemoveDeviceTask().runTask(logHandler, device.getDeviceID());
	}
	
	/** Task adds the Geofence to the system
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
	 */
	private class AddGeofenceTask extends NetworkTask<String, Void, Integer> {

		@Override
		protected Integer runInBackground(String... arg0) throws Exception {
			return ((RestAdminClient)restClient).addGeofence(arg0[0]);
		}

		@Override
		protected void onSuccess(Integer result) {
			onInfo("Geofence " + result + " added");
		}
	}   
	
	/** Task adds the Event or derivation to the Geofence.
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
     */
	private class AddEventTask extends NetworkTask<Event, Void, Event> {

		@Override
		protected Event runInBackground(Event... arg0) throws Exception {
			if(((RestAdminClient)restClient).addEvent(arg0[0])){
				return arg0[0];
			}
			return null;
		}

		@Override
		protected void onSuccess(Event result) {
			if(result != null){
				onInfo("Event added to geofence " + result.getMinor());
			} else {
				onInfo("Error adding event, check Server logs");
			}
		}
	}
	
	/** Task adds the given SystemBeacon to the Geofence. 
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
	 */
	private class AddBeaconTask extends NetworkTask<SystemBeacon, Void, Boolean> {

		@Override
		protected Boolean runInBackground(SystemBeacon... arg0) throws Exception {
			return ((RestAdminClient)restClient).addBeacon(arg0[0]);
		}

		@Override
		protected void onSuccess(Boolean result) {
			if(result){
				onInfo("Beacon added to geofence");
			} else {
				onInfo("Beacon UUID not matching with the system");
			}
		}
	}   
	
	/** Task removes Event from the Geofence
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
	 */
	private class RemoveEventTask extends NetworkTask<Integer, Void, Integer[]> {

		@Override
		protected Integer[] runInBackground(Integer... arg0) throws Exception {
			((RestAdminClient)restClient).deleteEvent(arg0[0], arg0[1]);
			return arg0;
		}

		@Override
		protected void onSuccess(Integer[] result) {
			onInfo("Removed event " + result[1] + " from geofence " + result[0]);
		}
	}
	
	/** Task removes SystemBeacon form the Geofence. 
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
     */
	private class RemoveBeaconTask extends NetworkTask<SystemBeacon, Void, SystemBeacon> {

		@Override
		protected SystemBeacon runInBackground(SystemBeacon... arg0) throws Exception {
			((RestAdminClient)restClient).deleteBeacon(arg0[0].getMinor(), arg0[0].getMajor());
			return arg0[0];
		}

		@Override
		protected void onSuccess(SystemBeacon result) {
			onInfo("Removed beacon (Minor/Major: " 
					+result.getMinor()+"/"+result.getMajor() + ")");
		}
	}
	
	/** Task removes Geofence with the given minor from the system. 
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
	 */
	private class RemoveGeofenceTask extends NetworkTask<Integer, Void, Integer> {

		@Override
		protected Integer runInBackground(Integer... arg0) throws Exception {
			if(((RestAdminClient)restClient).deleteGeofence(arg0[0])){
				return arg0[0];
			}
			return null;
		}

		@Override
		protected void onSuccess(Integer result) {
			if(result != null){
				onInfo("Geofence " + result + " deleted");
			} else {
				onInfo("Still Beacons or Event exisiting");
			}
		}
	}
}
