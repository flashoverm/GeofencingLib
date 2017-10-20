package de.geofencing.client.httpClient;

/** Error while communicating with the web service.
 * Error code describes the error.
 * 
 * @author Markus Thral
 *
 */
public class HttpClientException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private ErrorCode errorCode;
	
	public HttpClientException(ErrorCode errorCode, Exception exception){
		super(exception);
		this.errorCode = errorCode;
	}
	
	public ErrorCode getErrorCode(){
		return errorCode;
	}
	
	@Override
	public String getMessage(){
		return "ErrorCode: " + errorCode.toString();
	}
	
	/** Describes an error occurring in the RestClient
	 * 
	 */
	public enum ErrorCode {
		ObjectNotFound,
		ObjectAlreadyExisting,
		SerializationError,
		InternalServerError,
		Unauthorized,

		/** Host address is not reachable
		 */
		HostNotReachable,
		/** Web service is not running
		 */
		ServiceNotRunning,
		/** Error with output stream of connection
		 */
		DataOutputStreamError,
		/** Error with input stream of connection
		 */
		DataInputStreamError,
		ProtocolError,
		/** Error doesn't match with defined errors
		 */
		UndefinedError
	}

}
