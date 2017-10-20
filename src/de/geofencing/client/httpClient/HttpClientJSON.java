package de.geofencing.client.httpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.google.gson.Gson;

import de.geofencing.client.httpClient.HttpClientException.ErrorCode;
import de.geofencing.util.Debugger;

/** HTTPClient for JSON based serializing of transmitted objects
 * Uses gson for serializing (Needs gson-2.3.1.jar or newer)
 * 
 * @author Markus Thral
 *
 */
public class HttpClientJSON extends HttpClient{
	
	/** Creates new HttpClient
	 * 
	 */
    public HttpClientJSON(){
        super();
    }

    @Override
	protected void objectToStream(Object object, OutputStream stream) throws HttpClientException {
		try {       
			Gson serializer = new Gson();
			Debugger.print("JSON-Stream out: " + serializer.toJson(object));
			stream.write(serializer.toJson(object).getBytes());
		} catch (IOException e) {
			throw new HttpClientException(ErrorCode.SerializationError, e);
		}
    }
    
    @Override
	protected Object streamToObject(Class<?> classType, String input) throws HttpClientException {
    	Object object;
    	try{
            Gson deserializer = new Gson();
    		object = deserializer.fromJson(input, classType);

            return object;
    	} catch(Exception e){
			throw new HttpClientException(ErrorCode.SerializationError, e);
    	}
    }

	@Override
	protected Object streamToObject(Type classType, String input) throws HttpClientException {
    	Object object;
    	try{
            Gson deserializer = new Gson();
    		object = deserializer.fromJson(input, classType);

            return object;
    	} catch(Exception e){
			throw new HttpClientException(ErrorCode.SerializationError, e);
    	}
	}
}