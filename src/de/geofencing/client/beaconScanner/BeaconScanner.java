package de.geofencing.client.beaconScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.jaalee.sdk.BLEDevice;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.DeviceDiscoverListener;
import com.jaalee.sdk.ErrorListener;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.RemoteException;
import de.geofencing.client.handler.LogHandler;
import de.geofencing.log.LogEntry;
import de.geofencing.util.Debugger;
import de.geofencing.util.Util;

/** Scans for beacons in range of the Device
 *  and provides every necessary functions to scan.
 *  If the beacons in range change the BeaconChangeListener
 *  is called.
 *  If an error occurs the LogHandler is called. 
 *  
 * Uses Jaalee API (Needs jaalee-beacon-android-sdk-master.jar)
 * 
 * @author Markus Thral
 *
 */
public class BeaconScanner {
	
	private Activity activity;
	
	private LogHandler logHandler;

	private BeaconManager beaconManager;
	private BeaconChangeHandler beaconChangeListener;
	
	private Region region;
	
	private List<Beacon> inRange;
	private Comparator<Beacon> beaconComparator;
	
	private boolean scanServiceReady;
	private boolean startScanAttempt;
	private boolean scanRunning;

	/** Sets up a new BeaconScanner

	 * @param activity Environment of the scanner
	 * @param logHandler LogHandler for handling error messages
	 */
	public BeaconScanner(Activity activity, LogHandler logHandler){
		this.activity = activity;
		this.logHandler = logHandler;
		
		region = new Region("", null, null, null);
		inRange = new ArrayList<>();
		
		this.beaconManager = new BeaconManager(activity.getBaseContext());
		this.beaconManager.setErrorListener(new ErrorListener() {
			
			@Override
			public void onError(Integer arg0) {
				if(logHandler != null){
					logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Error Scanning for beacons"));
				}
			}
		});	
        
		this.beaconComparator = new Comparator<Beacon>() {
			@Override
			public int compare(Beacon o1, Beacon o2) {
            	int compare = o1.getProximityUUID().compareTo(o2.getProximityUUID());
            	if(compare == 0){
            		compare = Integer.compare(o1.getMajor(), o2.getMajor());
            		if(compare == 0){
            			return Integer.compare(o1.getMinor(), o2.getMinor());
            		}
            	}
            	return compare;
			}
		};
		
        this.setRangingListener();
	}
	
	/** Returns status of the scanner
	 * 
	 * @return true if scan is running, false if not
	 */
	public boolean isScanRunning(){
		return scanRunning;
	}
		
	/** Sets scanPeriod and wait-time between to scans
	 * 
	 * @param scanPeriod Scan-time in milliseconds
	 * @param scanWaitTime Time between to scans in milliseconds
	 */
	public void setScanPeriods(int scanPeriod, int scanWaitTime){
		this.beaconManager.setBackgroundScanPeriod(scanPeriod, scanWaitTime);
		this.beaconManager.setForegroundScanPeriod(scanPeriod, scanWaitTime);
	}
	
	/** Sets the BeaconChangeListener which is called
	 *  when the beacons change
	 * 
	 * @param bcl BeaconChangeListener which should be called
	 */
	public void setBeaconChangeListener(BeaconChangeHandler bcl){
		this.beaconChangeListener = bcl;
	}
	
	public void setBeaconsInRange(List<Beacon> inRange){
		this.inRange = inRange;
	}
	
	/** Checks if device has bluetooth and asks for activating
	 *  if bluetooth is disabled. If bluetooth is activated, 
	 *  depends on user decision!
	 *  
	 *  @return true if bluetooth is activated, false if not
	 */
    private boolean verifyBluetooth(){
        if (!beaconManager.hasBluetooth()) {
			if(logHandler != null){
				logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, "Bluetooth not found"));
			}
            return false;
        }

        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, 802);
        } 
    	return true;
    }
    
    /** Deactivates bluetooth
     * 
     * @return true if bluetooth is deactivated, false if not
     */
    public boolean deactivateBluetooth(){
        if (beaconManager.isBluetoothEnabled()) {
        	return BluetoothAdapter.getDefaultAdapter().disable();
        }   
        return true;
    }

    /** Starts the scan service 
     * 
     */
    private void connectToService(){
    	if(!scanServiceReady){
            beaconManager.connect(new ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                	scanServiceReady = true;
                	if(startScanAttempt){
                		startRanging();
                    	startScanAttempt = false;
                	}
                }
            });
    	}
    }
    
    /** Stops the scan service
     * 
     */
    private void disconnectFromService(){
    	beaconManager.disconnect();
    	scanServiceReady = false;
    }
    
	/** Starts scanning for beacons, asks for activating bluetooth and starts service
	 * 
	 * @return true if starting succeeded, false if not
	 */
	public boolean startScan(){
		if(!this.verifyBluetooth()){
			return false;
		}
        this.connectToService();
        return this.startRanging();

	}
	
	/** Starts ranging and discovering BLE devices.
	 * 
	 * @return true if starting succeeded, false if not
	 */
	private boolean startRanging(){
		if(!scanServiceReady){
			startScanAttempt = true;
			scanRunning = true;
			return scanRunning;
		}
        try {
            beaconManager.startRangingAndDiscoverDevice(region);
			scanRunning = true;
			Debugger.print("Start ranging for beacons");
            return scanRunning;
        } catch (RemoteException e) {
			if(logHandler != null){
				logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, e));
			}        
		}
        return false;
	}
	
	/** Stops scanning for beacons and deactivates service 
	 * 
	 * @return true if stopping succeeded, false if not
	 */
	public boolean stopScan(){
		try{
	        beaconManager.stopRanging(region);
	        
	        this.disconnectFromService();

	        scanRunning = false;
	        return !scanRunning;
		}
		catch(RemoteException e){
			if(logHandler != null){
				logHandler.onLogEntry(new LogEntry(LogEntry.clientLogTag, e));
			}
		}
		return false;
	}

    /** Sets the listener which are called when beacons
     *  or BLEDevices are found
     */
    private void setRangingListener(){
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region , final List<Beacon> beacons) {
    			Debugger.print("FoundBeacons 1");

        		for(Beacon beacon : beacons){
        			Debugger.print("Beacon found: " + beacon.getProximityUUID());
        		}

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateBeaconsInRange(beacons);
                    }
                });
            }
        });
        beaconManager.setDeviceDiscoverListener(new DeviceDiscoverListener() {
            @Override
            public void onBLEDeviceDiscovered(BLEDevice device) {
            }
        });
    }
    
    /** Compares and updates beacons in range. If a change is detected
     *  the beaconChangeListener will be called.
     * 
     * @param beacons List of beacons
     */
    private void updateBeaconsInRange(final List<Beacon> beacons){
		Debugger.print("FoundBeacon 2");

    	List<Beacon> update = new ArrayList<Beacon>(beacons);
    	if(beacons.size() >0){
    		Debugger.print("FoundBeacon 3");

    		for(Beacon beacon : update){
    			Debugger.print("Beacon found: " + beacon.getProximityUUID());
    		}
    		Collections.sort(update, beaconComparator);
    		//List.sort doesn't work below API level 24 
    		//update.sort(c);
    	}
    	
    	if(inRange != null){
        	if(inRange.size() == update.size() && inRange.containsAll(update)){
            	return;
        	}
    	}
    	inRange = update;
    	if(beaconChangeListener != null){
        	beaconChangeListener.onBeaconsChangeDetected(Util.beaconListToSystemBeacons(update));
    	}
    	
    }
}
