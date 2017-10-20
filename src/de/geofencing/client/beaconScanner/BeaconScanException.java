package de.geofencing.client.beaconScanner;

/** Error while scanning for beacons
 * 
 * @author Markus Thral
 *
 */
public class BeaconScanException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public BeaconScanException() {
		super();
	}
	
	public BeaconScanException(String message) {
		super(message);
	}
}
