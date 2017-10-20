package de.geofencing.event.counter;

import java.util.List;

import com.google.gson.reflect.TypeToken;

import de.geofencing.client.httpClient.HttpClientException;
import de.geofencing.client.restClient.RestAdminClient;

public class RestCounterClient extends RestAdminClient {

	public RestCounterClient(String restServiceURL) {
		super(restServiceURL);
	}
	
	public List<Counter> getCounterList() throws HttpClientException {
		return client.target(restServiceURL+"counter")
				.get(new TypeToken<List<Counter>>(){}.getType());
	}
	
	/** Gets Value of the Counter
	 * 
	 * @param counterID CounterID of the Counter
	 * @return Value of the counter
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public int getCounterValue(int counterID) throws HttpClientException{
		return client.target(restServiceURL+"counter/"+counterID)
				.get(Integer.class);
	}
	
	/** Gets the value of the Counter from the Geofence.
	 * Only usable if only one Counter is assigned to the Geofence.
	 * I.e. with GeofenceCounterEvent.
	
	 * @return Value of the counter
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public int getGeofenceCounterValue(int minor) throws HttpClientException{
		return client.target(restServiceURL+"geofences/"+minor+"/counter")
				.get(Integer.class);
	}
	
	/** Removes Counter 
	 * 
	 * @param counterID CounterID of the Counter
	 * @return true if counter removed, false if not
	 * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	public boolean removeCounter(int counterID) throws HttpClientException{
		return client.target(restServiceURL+"counter/"+counterID)
				.delete(Boolean.class);
	}

}
