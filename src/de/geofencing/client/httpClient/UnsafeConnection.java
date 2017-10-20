package de.geofencing.client.httpClient;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** HTTP connection which allow every certificate without validation.
 * Can be used for self signed certificate.
 * Not recommended for the live system, because of security issues.
 * 
 * @author Markus Thral
 *
 */
public class UnsafeConnection {
	
	/** Creates Connection to target allowing all certificates
	 * 
	 * @param target URL of the web service (i.e. https://127.0.0.1:8080/Geofencing)
	 * @return URLConnection to the given target 
	 * @throws IOException if an I/O error occurs
	 */
	public static URLConnection connectAllowingAllCertificates(String target) throws IOException{

	    TrustManager[] trustAllCerts = new TrustManager[] {
	       new X509TrustManager() {
	          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	            return null;
	          }
	          public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

	          public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
	       }
	    };
	    
	    try{
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		    
		    HostnameVerifier allHostsValid = new HostnameVerifier() {
		        public boolean verify(String hostname, SSLSession session) {
		          return true;
		        }
		    };
		    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		    
			URL url = new URL(target);
			return url.openConnection();
	    } catch (NoSuchAlgorithmException | KeyManagementException e) {
		    throw new IOException(e);
		}
	}
}
