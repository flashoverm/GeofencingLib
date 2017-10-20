package de.geofencing.util;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.jaalee.sdk.Beacon;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.geofencing.system.beacon.SystemBeacon;
import de.geofencing.system.beacon.SystemBeacons;

/** Provides useful functions for the GeoencingClient
 * 
 * @author Markus Thral
 *
 */
public class Util {
	
	/** Converts a Jaalee-beacon object to a SystemBeacon object
	 * 
	 * @param beacon Jallee-beacon object
	 * @return SystemBeacon object
	 */
	public static SystemBeacon beaconToSystemBeacon(Beacon beacon){
		return new SystemBeacon(
                UUID.fromString(beacon.getProximityUUID()),
                beacon.getMajor(), 
                beacon.getMinor()
                );
	}
	
	/** Converts a List of Jaalee-beacons to a SystemBeacons-object
	 * 
	 * @param beaconList List of Jaalee-beacons
	 * @return SystemBeacons object with the given beacons
	 */
	public static SystemBeacons beaconListToSystemBeacons(List<Beacon> beaconList){
		SystemBeacons beacons = new SystemBeacons();
		for(Beacon beacon : beaconList){
			beacons.addBeacon(beaconToSystemBeacon(beacon));
		}
		return beacons;
	}
	
	/** Converts a SystemBeacons object to a list of Jaalee beacons.
	 * Use with care: Only UUID, major and minor are set correctly.
	 * 
	 * @param systemBeacons SystemBeacons object to be converted
	 * @return List of Jaalee beacons
	 */
	public static List<Beacon> SystemBeaconsToBeaconList(SystemBeacons systemBeacons){
		List<Beacon> beacons = new LinkedList<>();
		for(SystemBeacon sysBeacon : systemBeacons){
			beacons.add(new Beacon(sysBeacon.getUUID().toString(), "name", "mac", sysBeacon.getMajor(), sysBeacon.getMinor(), 0, 0, 0));
		}
		return beacons;
	}
	
	/** Sets height of a ListView-object depending on the entry-count
	 *  Source: https://gist.github.com/ajamaica/8d6ba913c94c604b6900
	 * 
	 * @param listView ListView object
	 * @param listItemPadding Padding of the items of the list
	 */
    public static void setListViewHeightBasedOnChildren(ListView listView, int listItemPadding) {
		ArrayAdapter<?> listAdapter = (ArrayAdapter<?>) listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight()+listItemPadding/2;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }	 
}
