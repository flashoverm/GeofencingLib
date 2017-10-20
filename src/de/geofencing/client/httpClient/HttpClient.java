package de.geofencing.client.httpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

import de.geofencing.client.httpClient.HttpClientException.ErrorCode;
import de.geofencing.system.exceptions.AlreadyExistingException;
import de.geofencing.system.exceptions.NotFoundException;
import de.geofencing.system.exceptions.UnauthorizedExcpetion;

/** HttpClient provides all necessary HTTP-requests (GET, POST, PUT, DELETE),
 *  functions for preparing data to be sent and processing received data. 
 *  
 *  Functions for converting objects from or to streams (i.e. JSON)
 *  have to be implemented in the derived class. 
 *  
 *  Is an authorization necessary, authorization has to be set.
 *  
 *  For self signed certificates, the allowAllCertificates has to be set true
 *  via the method setAllowAllCertificates of the ClientConfiguration.
 *  Not recommended for the live system, because of security issues.
 *   
 * @author Markus Thral
 *
 */
public abstract class HttpClient {
	
	/** Header with administrator password or verification address of the client
	 * sent by the client for authorization
	 */
	public static final String AUTHORIZATIONHEADER = "Authorization";
	public static final int CONNECTION_TIMEOUT = 2500;
	
	protected String authorization;
	protected boolean allowAllCertificates;
    protected HttpURLConnection conn;
    
    /** Creates new HttpClient
     * 
     */
    public HttpClient(){
    	this.allowAllCertificates = false;
    }
    
    /** Allows to accept all certificates
     * 
     */
    public void allowAllCertificates(){
    	this.allowAllCertificates = true;
    }
    
	/** Sets the targetURL of the server
     * 
     * @param targetURL URL of the web service
	 * @return HttpClient with target for calling the HTTP-request
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
	public HttpClient target(String targetURL) throws HttpClientException{
		try{
			if(allowAllCertificates){
		        conn = (HttpURLConnection) UnsafeConnection.connectAllowingAllCertificates(targetURL);
			} else {
				conn = (HttpURLConnection) new URL(targetURL).openConnection();
			}
	        conn.setConnectTimeout(CONNECTION_TIMEOUT);
	        conn.setRequestProperty("User-Agent", "GeofencingClient");
	        if(authorization != null){
		        conn.setRequestProperty(AUTHORIZATIONHEADER, authorization);
	        }
	        conn.setDoInput(true);
			return this;
		}catch(IOException e){
			throw new HttpClientException(ErrorCode.UndefinedError, e);
		}
	}
	
	/** Sets authorization information (i.e. administrator password) which is
	 * transmitted via authorization header
	 * 
	 * @param authorization Authorization information
	 */
	public void setAuthorization(String authorization){
		this.authorization = authorization;
	}

	
	/** Sends HTTP-GET request to server expecting resource with defined type T
	 * 
	 * @param <T> Return Type
	 * @param returnType Expected type T of the requested resource
	 * @return received object of type T
     * @throws HttpClientException with ErrorCode if a problem occurs
	 */
    public <T>T get(Class<T> returnType) throws HttpClientException{
        try {
        	conn.setRequestMethod("GET");
        	return returnType.cast(recieveData(returnType));
        } catch (ProtocolException e) {
        	throw new HttpClientException(ErrorCode.ProtocolError, e);
        }
    }
    
	/** Sends HTTP-GET request to server expecting list of type returnType
	 * 
	 * @param <T> Return Type
	 * @param returnType Expected Type of the requested resource
	 * @return requested list of type returnType
     * @throws HttpClientException with ErrorCode if a problem occurs
	 */
    @SuppressWarnings("unchecked")
    public <T>T get(Type returnType) throws HttpClientException{
        try {
        	conn.setRequestMethod("GET");
        	return (T)recieveData(returnType);
        } catch (ProtocolException e) {
        	throw new HttpClientException(ErrorCode.ProtocolError, e);
        }
    }
    
    /** Sends object of defined type via HTTP-POST request to server 
     * expecting resource with defined type T
     * 
	 * @param <T> Return Type
     * @param sendObject Object to be sent
     * @param sendType Type of the object to be sent
     * @param returnType Expected type T of the requested resource
     * @return received object of type T
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
    public <T>T post(Object sendObject, Class<?> sendType, Class<T> returnType) throws HttpClientException{
        try {
        	conn.setRequestMethod("POST");
        	sendData(sendType, sendObject);
        	return returnType.cast(recieveData(returnType));
        } catch (ProtocolException e) {
        	throw new HttpClientException(ErrorCode.ProtocolError, e);
        }
    }
    
    /** Sends HTTP-POST request to server expecting resource with defined type T
     * 
	 * @param <T> Return Type
     * @param returnType Expected type T of the requested resource
     * @return received object of type T
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
    public <T>T post(Class<T> returnType) throws HttpClientException{
        try {
        	conn.setRequestMethod("POST");
        	return returnType.cast(recieveData(returnType));
        } catch (ProtocolException e) {
        	throw new HttpClientException(ErrorCode.ProtocolError, e);
        }
    }
    
    /** Sends object of defined type via HTTP-PUT request to server 
     *  expecting resource with defined type T
     * 
	 * @param <T> Return Type
     * @param sendObject Object to be sent
     * @param sendType Type of the object to be sent
     * @param returnType Expected type T of the requested resource
     * @return received object of type T
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
    public <T>T put(Object sendObject, Class<?> sendType, Class<T> returnType) throws HttpClientException{
        try {
        	conn.setRequestMethod("PUT");
        	sendData(sendType, sendObject);
        	return returnType.cast(recieveData(returnType));
        } catch (ProtocolException e) {
        	throw new HttpClientException(ErrorCode.ProtocolError, e);
        }
    }

    /** Sends HTTP-DELTE request to server expecting resource with defined type T
     * 
	 * @param <T> Return Type
     * @param returnType Expected type T of the requested resource
     * @return received object of type T
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
    public <T>T delete(Class<T> returnType) throws HttpClientException{
        try {
			conn.setRequestMethod("DELETE");
	        return returnType.cast(recieveData(returnType));
		} catch (ProtocolException e) {
			throw new HttpClientException(ErrorCode.ProtocolError, e);
		}
    }

    
    /** Prepares connection and data and sends it to the target
     * 
     * @param sendType Type of the object to be sent
     * @param sendObject Object to be sent
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
    private void sendData(Class<?> sendType, Object sendObject) throws HttpClientException{
    	try{
            conn.setDoOutput(true);
        	if(sendType.equals(String.class)){
                conn.setRequestProperty("Content-Type", "text/plain; charset=\"utf-8\"");
                OutputStream os = conn.getOutputStream();
        		String outputObject = (String) sendObject;
    			os.write(outputObject.getBytes());
        	}
        	else{
   	            conn.setRequestProperty("Content-Type", "application/json; charset=\"utf-8\"");
    			OutputStream os = conn.getOutputStream();
    			objectToStream(sendObject, os);
        	}
    	} catch(IOException e){
    		throw new HttpClientException(ErrorCode.DataOutputStreamError, e);
    	}
    }
     
    /** Converts given object to OutputStream for sending to the target
     *  has to be implemented for the used stream-type (i.e. JSON) 
     * 
     * @param object Object to be converted
     * @param stream OutputStream where to object should be written in
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
    protected abstract void objectToStream(Object object, OutputStream stream) throws HttpClientException;
    
    /** Receives and processes data from connection to object with the given class type
     * 
     * @param classType Expected class type of the received object
     * @return converted Object, has to be casted, null if no input found
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
	private Object recieveData(Class<?> classType) throws HttpClientException{
		evaluateHttpStatus();
		String json = convertInputToString();
		
        if(classType.equals(String.class)){
        	return json;
        }
        if(classType.equals(Integer.class)){
        	return Integer.parseInt(json);
        }
        return streamToObject(classType, json);
    }
	
    /** Receives and processes data from connection to object with the given Type
     * 
     * @param classType Expected Type of the received object
     * @return converted Object, has to be casted, null if no input found
     * @throws HttpClientException with ErrorCode if a problem occurs
     */
	private Object recieveData(Type classType) throws HttpClientException{
		evaluateHttpStatus();
		String json = convertInputToString();

		return streamToObject(classType, json);
    }
    
	/** Converts serialized String (i.e. JSON) to object of the defined class type.
	 * Handling different types of Event has to be implemented.
	 *  
	 * @param classType Expected class type of the received object
	 * @param input InputStream of the object as String
	 * @return converted Object, has to be casted
     * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	protected abstract Object streamToObject(Class<?> classType, String input) throws HttpClientException;
	
	/** Converts serialized String (i.e. JSON) to object of the defined Type.
	 * Handling different types of Event has to be implemented.
	 *  
	 * @param classType Expected Type of the received object
	 * @param input InputStream of the object as String
	 * @return converted Object, has to be casted
     * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	protected abstract Object streamToObject(Type classType, String input) throws HttpClientException;
	
	/** Evaluates HTTP status code
	 * 
	 * @throws HttpClientException if the status code is not 200
	 */
	protected void evaluateHttpStatus() throws HttpClientException {
		try{
			int responseCode = conn.getResponseCode();
			
			if(responseCode == 500){
				Scanner toString = new Scanner(conn.getErrorStream(),"UTF-8");
				String errorMessage = toString.useDelimiter("\\A").next();
				toString.close();
				throw new HttpClientException(ErrorCode.InternalServerError, new Exception("ResponseCode: 500 \n" + errorMessage));
			}
			switch(responseCode){
				case (200):		return;
				case (401):		throw new HttpClientException(ErrorCode.Unauthorized, new UnauthorizedExcpetion());
				case (404): 	throw new HttpClientException(ErrorCode.ObjectNotFound, new NotFoundException());
				case (409):		throw new HttpClientException(ErrorCode.ObjectAlreadyExisting, new AlreadyExistingException());
				default:		throw new HttpClientException(ErrorCode.UndefinedError, new Exception("ResponseCode: " + responseCode));
			}
		} catch(SocketTimeoutException e){
			throw new HttpClientException(ErrorCode.HostNotReachable, e);
		} catch(ConnectException e){
			throw new HttpClientException(ErrorCode.ServiceNotRunning, e);
		} catch(IOException e){
			throw new HttpClientException(ErrorCode.UndefinedError, e);
		}
	}

	/** Converts InputStream of the connection to an JSON String
	 * 
	 * @return JSON String as String object
     * @throws HttpClientException with ErrorCode if a problem occurs
	 */
	private String convertInputToString() throws HttpClientException{
		try{
			Scanner toString = new Scanner(conn.getInputStream(),"UTF-8");
			String json = toString.useDelimiter("\\A").next();

	        toString.close();
			return json;
		} catch(IOException e){
			throw new HttpClientException(ErrorCode.DataInputStreamError, e);
		}
	}
}