package de.geofencing.client;

import android.app.Activity;
import de.geofencing.client.beaconScanner.BeaconChangeHandler;
import de.geofencing.client.handler.LoginHandler;
import de.geofencing.client.networkTask.NetworkTask;
import de.geofencing.client.restClient.RestAdminClient;

/** GeofencingAdminLogin provides functions for check 
 * the service URL and administrator password
 * 
 * @author Markus Thral
 *
 */
public class GeofencingAdminLogin extends GeofencingClient {

	private LoginHandler loginHandler;
	
	/** Creates GeofencingAdminLogin and starts the GeofencingClient
	 * 
	 * @param activity Activity where GeofencingAdminLogin is used
	 */
	public GeofencingAdminLogin(Activity activity) {
		super(activity);
	}

	
	/* 
	 * Credential check
	 */
	
	@Override
	protected void setupServerConnection(String restServiceURL){
		this.restClient = new RestAdminClient(restServiceURL);
	}

	/** Checks if given administrator password is correct.
	 * Connection to server has to be set up with "setupServerConnection".
	 * If credentials correct the set LoginHandler is called, if not the InfoHandler is called.
     * Occurring errors will be handled by the LogHandler.
	 * 
	 * @param password Password of the administrator of the system
	 * @param loginHandler LoginHandler which is called if credentials are correct
	 */
	public void checkCredentials(String password, LoginHandler loginHandler){
		this.loginHandler = loginHandler;
		new CheckCredentialsTask().runTask(logHandler, password);
	}
	
    /** Task checks if given credentials are correct.
	 * Result is handled by the InfoHandler.
     * Occurring errors will be handled by the LogHandler.
     */
    private class CheckCredentialsTask extends NetworkTask<String, Void, Boolean> {
    	
		@Override
		protected Boolean runInBackground(String... params) throws Exception {
			return ((RestAdminClient)restClient).checkPassword(params[0]);
		}
		@Override
		protected void onSuccess(Boolean result) {
			if(result){
				loginHandler.onLogin();
			} else {
				onInfo("Password incorrect");
			}
		}
    }

    
    /*
     *  Unused methods
     */
    
    /** UnsupportedOperation
     * @return - 
     */
    @Override
    public boolean isBtScanActive() {
    	throw new UnsupportedOperationException();
    }
    /** UnsupportedOperation
     */
    @Override
	public void setScanPeriods(int scanPeriod, int scanWaitTime){
    	throw new UnsupportedOperationException();
    }
    /** UnsupportedOperation
     */
    @Override
	public void setBeaconChangeHandler(BeaconChangeHandler beaconChangeHandler){
    	throw new UnsupportedOperationException();
    }
    
	@Override
	protected void startInteractingWithServer() {}

	@Override
	protected void stopInteractingWithServer() {}

	@Override
	protected void restartInteractingWithServer() {}
	
	@Override
	protected void setBeaconChangeListener() {}

}
