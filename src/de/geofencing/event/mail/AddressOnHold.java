package de.geofencing.event.mail;

import java.io.Serializable;

import javax.mail.internet.InternetAddress;

/** Represents an subscribed mail address which is not yet verified.
 * Contains mail address and subscription time  
 * 
 * @author Markus Thral
 *
 */
public class AddressOnHold implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final InternetAddress mailAddress;
	private long verificationRequestTime;
	
	/** Constructor for serialization
	 * 
	 */
	public AddressOnHold(){
		this.mailAddress = null;
		this.verificationRequestTime = -1;
	}
	
	/** Generates AddressOnHold and saves time, when the verification request is sent to the address
	 * 
	 * @param mailAddress Mail address where the request is sent to
	 */
	public AddressOnHold(InternetAddress mailAddress){
		this.mailAddress = mailAddress;
		this.verificationRequestTime = System.currentTimeMillis();
	}
	
	public InternetAddress getMailAddress(){
		return mailAddress;
	}
	
	public long getVerificationRequestTime(){
		return verificationRequestTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mailAddress == null) ? 0 : mailAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddressOnHold other = (AddressOnHold) obj;
		if (mailAddress == null) {
			if (other.mailAddress != null)
				return false;
		} else if (!mailAddress.equals(other.mailAddress))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AddressOnHold [mailAddress=" + mailAddress + ", verificationRequestTime=" + verificationRequestTime
				+ "]";
	}
}

