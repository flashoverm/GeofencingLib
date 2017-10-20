package de.geofencing.client;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import de.geofencing.client.beaconScanner.BeaconChangeHandler;
import de.geofencing.client.beaconScanner.BeaconScanner;
import de.geofencing.client.handler.InfoHandler;
import de.geofencing.client.handler.LogHandler;
import de.geofencing.client.networkTask.NetworkTask;
import de.geofencing.client.restClient.RestClient;
import de.geofencing.log.LogEntry;
import de.geofencing.system.beacon.SystemBeacons;
import de.geofencing.system.exceptions.ConfigurationException;

/** GeofencingClient provides functions to implement 
 * a client for the geofencing system.
 * 
 * @author Markus Thral
 *
 */
public abstract class GeofencingClient {
	
	public static final String SERVICEURL_NOT_SET = "";

	protected Activity activity;
	
	protected ClientConfiguration configuration;
	protected RestClient restClient;
	protected BeaconScanner beaconScanner;
	
	protected BeaconChangeHandler bch;
	protected LogHandler logHandler;
	protected InfoHandler infoHandler;
		
	protected String restServiceURL;
	protected boolean allowAllCertificates;
	
	/** Creates new GeofencingClient
	 * 
	 * @param activity Activity where GeofencingClient is used
	 */
	public GeofencingClient(Activity activity){
		this.activity = activity;
		this.configuration = new ClientConfiguration(activity.getApplicationContext());

		this.restServiceURL = SERVICEURL_NOT_SET;
        
		this.beaconScanner = new BeaconScanner(activity, logHandler);
		this.setBeaconChangeListener();
	}
	
	/** Sets LogHandler for processing LogEntries and errors,
	 *  for example writing to a log-file
	 * 
	 * @param logHandler LogHandler with the implemented instructions
	 */
	public void setLogHandler(LogHandler logHandler){
		this.logHandler = logHandler;
	}

	/** Sets InfoHandler for displaying information to the user
	 * 
	 * @param infoHandler InfoHandler with the implemented instructions
	 */
	public void setInfoHandler(InfoHandler infoHandler){
		this.infoHandler = infoHandler;
	}
	
	/** Shows message to the user via infoHandler and makes entry in Log via logHandler
	 * 
	 * @param message Message to be shown and logged
	 */
	public void onInfo(String message){
        if(logHandler != null){
				logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, message));
        }
		if(infoHandler != null){
			infoHandler.onInfo(message);
		}
	}
	
	
	/*
	 * Functions for handling service URL and connection
	 */
	
	public String getRestServiceURL(){
		return restServiceURL;
	}
	
	/** Checks if network is available
	 * 
	 * @return true if network available, false if not
	 */
	protected boolean isNetworkAvailable() {
		activity.getApplicationContext();
		ConnectivityManager connectivityManager = 
				(ConnectivityManager) activity.getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null &&	activeNetworkInfo.isConnected();
	}

	/** Sets up connection to the server with given URL
	 * Connection could be closed by setting to SERVICEURL_NOT_SET
	 * 
	 * @param restServiceURL URL of the web service as String 
	 * (i.e. https://127.0.0.1:8080/Geofencing)
	 */
	public void setServiceURL(String restServiceURL){
		if(isNetworkAvailable()){
			if(restServiceURL != null){
				if(!this.restServiceURL.equals(restServiceURL)){
					
					//Set URL to SERVICEURL_NOT_SET
					if(restServiceURL.equals(SERVICEURL_NOT_SET) &&
							!this.restServiceURL.equals(SERVICEURL_NOT_SET)){
						this.stopInteractingWithServer();
						this.restClient = null;
					}
					
					this.restServiceURL = restServiceURL;
					configuration.setServiceURL(restServiceURL);
					if(!this.restServiceURL.equals(SERVICEURL_NOT_SET)){
						if(logHandler != null && allowAllCertificates){
							logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Attention! All SSL certificates allowed!"));
						}
						new SetupConnectionTask().runTask(logHandler);
					}
				}
				return;
			}
		}
		if(logHandler != null){
			logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "No network connection"));
		}
	}

	/** Task checks if the URL of the service is reachable and starts communication
     * Occurring errors will be handled by the LogHandler.
     */
	private class SetupConnectionTask extends NetworkTask<Void, Void, Boolean> {
	
		@Override
		protected Boolean runInBackground(Void... arg0) throws Exception {
			GeofencingClient.this.setupServerConnection(restServiceURL);
			if(allowAllCertificates){
				restClient.allowAllCertificates();
			}
			return restClient.isServiceRunning();
		}

		@Override
		protected void onSuccess(Boolean result) {
			if(result){
				if(logHandler != null){
					logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Connected to " + restServiceURL));
				}
				GeofencingClient.this.startInteractingWithServer();
				return;
			}
			afterFailure();
		}
		
		@Override
		protected void afterFailure() {
			if(logHandler != null){
				logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Service URL not reachable " + restServiceURL));
			}
		}	
	}
	
	/** Should initiate the corresponding RestClient with the
	 * given URL. Authorization information should be set.
	 * 
	 * @param restServiceURL URL of the web service as String
	 * (i.e. https://127.0.0.1:8080/Geofencing)
	 */
	protected abstract void setupServerConnection(String restServiceURL);
	
    /** Allows to accept all certificates for the connection
     * to the web service. Have to be called before start().
     */
	public void allowAllCertificates(){
		this.allowAllCertificates = true;
	}
	
	/** Checks if the given service URL is reachable. The set URL is not changed.
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
     * 
	 * @param restServiceURL URL of the web service as String 
	 * (i.e. https://127.0.0.1:8080/Geofencing)
	 */
    public void checkServiceURL(String restServiceURL) {
    	if(restServiceURL != null 
    			&& !restServiceURL.equals(SERVICEURL_NOT_SET))
    	{
            new CheckServerServiceURLTask().runTask(this.logHandler, restServiceURL);
    	}
    }
	
    /** Task checks if the given web service URL is reachable. 
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
     */
    private class CheckServerServiceURLTask extends NetworkTask<String, Void, Boolean> {

    	@Override
        protected Boolean runInBackground(String... arg0) throws Exception {
           	GeofencingClient.this.stopInteractingWithServer();
        	GeofencingClient.this.setupServerConnection(arg0[0]);
			if(allowAllCertificates){
				restClient.allowAllCertificates();
			}
            return GeofencingClient.this.restClient.isServiceRunning();
        }

        @Override
        protected void onSuccess(Boolean result) {
            if(infoHandler != null) {
            	if(result){
                    infoHandler.onInfo("Service URL is reachable");
            	} else {
            		infoHandler.onInfo("Service URL not reachable");
            	}
            }
            afterFailure();	//Restores previous settings
        }
        
        @Override
        protected void afterFailure(){
            GeofencingClient.this.setServiceURL(GeofencingClient.this.restServiceURL);
            GeofencingClient.this.restartInteractingWithServer();
        }
    }

	
	/*
	 * System functions
	 */

	/** Loads properties from memory if existing and sets up connection
	 * to the web service. If connected startInteractingWithServer 
	 * is called.
	 * If called as GeofencingAdmin, make sure that the administrator password is set
	 * 
	 * All used handler should be set before starting the client.
	 */
	public void start(){
		try{
			setServiceURL(configuration.getServiceURL());
		} catch(ConfigurationException e){
	        if(logHandler != null){
	        	logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, e));
	        }
	    }
	}
	
	/** Stops interacting with server and scanning for beacons.
	 * Also bluetooth is deactivated.
	 * 
	 */
	public void stop(){
		if(isBtScanActive()){
			beaconScanner.stopScan();
			beaconScanner.deactivateBluetooth();
		}
		this.stopInteractingWithServer();
	}

	/** Should be overwritten with instructions which are executed when 
	 * the connection to web service is established the first time. 
	 */
	protected abstract void startInteractingWithServer();
	
	/** Should be overwritten with instructions which are executed
	 * when the connection to the web service is corrupted. 
	 */
	protected abstract void stopInteractingWithServer();
	
	/** Should be overwritten with instructions which are executed when 
	 * the connection to the web service is re-established.
	 */
	protected abstract void restartInteractingWithServer();
	
	
	/*
	 * Beacon scanner 
	 */
	
	/** Returns status of the BeaconScanner
	 * 
	 * @return true if scan is active, false if not
	 */
	public boolean isBtScanActive(){
		return beaconScanner.isScanRunning();
	}
	
	/** Sets scanPeriod and wait-time between to scans
	 * 
	 * @param scanPeriod Scan-time in milliseconds
	 * @param scanWaitTime Time between to scans in milliseconds
	 */
	public void setScanPeriods(int scanPeriod, int scanWaitTime){
		beaconScanner.setScanPeriods(scanPeriod, scanWaitTime);
	}
	
	/** Sets BeaconChangeHandler for displaying or processing beacons in range
	 * 
	 * @param beaconChangeHandler BeaconChangeHandler with the implemented function
	 */
	public void setBeaconChangeHandler(BeaconChangeHandler beaconChangeHandler){
		this.bch = beaconChangeHandler;
	}

	/** Should be overwritten with instructions how 
	 * the changed beacons in range should be handled.
	 * At the end, the BeaconChangeHandler should be called, if not null. 
	 * Alternatively the GetBeaconDataTask could be run.
	 */
	protected abstract void setBeaconChangeListener();
	
    /** Task which gets additional data for sent beacons if available.
     * Occurring errors will be handled by the LogHandler.
     */
    protected class GetBeaconDataTask extends NetworkTask<SystemBeacons, Void, SystemBeacons> {
    	
		@Override
		protected SystemBeacons runInBackground(SystemBeacons... params) throws Exception{
			return restClient.getBeaconData(params[0]);
		}
		@Override
		protected void onSuccess(SystemBeacons result) {
			if(bch != null){
				bch.onBeaconsChangeDetected(result);
			}		
		}
    }

    
	/*
	 * Additional Tasks used from Device and Administrator
	 */
	
    /** Task removes own Device.
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
     */
    protected class RemoveDeviceTask extends NetworkTask<Integer, Void, Integer> {

		@Override
		protected Integer runInBackground(Integer... arg0) throws Exception {
			restClient.deleteDevice(arg0[0]);
			return arg0[0];
		}

		@Override
		protected void onSuccess(Integer result) {
			onInfo("Device " + result + " removed");
		}	
	}
}
