package de.geofencing.event.mail;

import static com.mongodb.client.model.Filters.eq;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;

import de.geofencing.database.DocumentConverter;
import de.geofencing.database.MongoConnection;
import de.geofencing.system.SystemConfiguration;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.system.exceptions.NotFoundException;

/** Provides methods to execute CRUD-operations for mail addresses on the MongoDB
 * 
 * @author Markus Thral
 *
 */
public class MailDBConnector {

	protected static final String mailConfirmedCollection = "confirmedMail";
	protected static final String waitingConfirationCollection = "mailOnHold";

	// Address Management

	/** Checks if mail address is already registered or on hold with a valid link. 
	 * If not the address is added to onHold.
	 * 
	 * @param recipient Mail address of the recipient
	 * @return true if mail is added to on hold, false if there's a valid entry for the address
	 * @throws ConfigurationException if mail configuration missing or not complete
	 * @throws AddressException if the recipient address is malformed
	 */
	public static boolean addAddressOnHold(String recipient) throws ConfigurationException, AddressException {
		InternetAddress address = new InternetAddress(recipient);
		if(!isMailAddressConfirmed(recipient)){
			try{
				AddressOnHold onHold = findMailOnHold(recipient);
				long confirmationTimeout = Integer.parseInt(SystemConfiguration.getValue("confirmationTimeout"))*1000;
				long currentTime = System.currentTimeMillis();
				long difference = currentTime - onHold.getVerificationRequestTime();
				if (difference < confirmationTimeout) {
					return false;
				}
				removeOnHoldMail(recipient);
			} catch(NotFoundException e1){
			}
			insertOnHoldMail(address);
		}
		return true;
	}

	/** Moves mail address from on hold to confirmed if the verification isn't expired.
	 * 
	 * @param recipient Mail address of the recipient
	 * @return true if address is confirmed, false if verification expired or address malformed
	 * @throws NotFoundException if mail is existing in on hold addresses
	 * @throws ConfigurationException if mail configuration missing or not complete
	 * @throws AddressException if the recipient address is malformed
	 */
	public static boolean confirmMailaddress(String recipient) throws NotFoundException, ConfigurationException, AddressException {
		if(!isMailAddressConfirmed(recipient)){
			AddressOnHold onHold = findMailOnHold(recipient);
			long currentTime = System.currentTimeMillis();
			long difference = currentTime - onHold.getVerificationRequestTime();
			long confirmationTimeout = Integer.parseInt(SystemConfiguration.getValue("confirmationTimeout"))*1000;
			if (difference < confirmationTimeout) {
				insertConfirmedMail(new InternetAddress(recipient));
				return true;
			}
			return false;
		}
		return true;
	}

	/** Removes mail from confirmed addresses
	 * 
	 * @param recipient Mail address of the recipient
	 * @return true if recipient is removes, false if not
	 * @throws NotFoundException if mail is existing in confirmed addresses
	 * @throws AddressException if the recipient address is malformed
	 */
	public static boolean unregisterMailaddress(String recipient) throws NotFoundException, AddressException {
		new InternetAddress(recipient);
		return removeConfirmedMail(recipient);
	}

	// Create

	/** Inserts mail address in on hold addresses with current time stamp
	 * Also removes expired on hold addresses.
	 * 
	 * @param address Mail address to be added
	 */
	protected static void insertOnHoldMail(InternetAddress address) {
		MongoConnection connection = new MongoConnection();
		AddressOnHold onHold = new AddressOnHold(address);
		getWaitingCollection(connection).insertOne(DocumentConverter.toDocument(onHold));
		connection.disconnect();
	}

	/** Inserts mail address in confirmed addresses
	 * 
	 * @param address Mail address to be added
	 */
	protected static void insertConfirmedMail(InternetAddress address) {
		MongoConnection connection = new MongoConnection();
		getConfirmedCollection(connection).insertOne(DocumentConverter.toDocument(address));
		connection.disconnect();
	}

	// Read

	/** Find entry in addresses on hold for the given recipient
	 * 
	 * @param recipient Mail address of the recipient
	 * @return AddressOnHold object with address and time stamp
	 * @throws NotFoundException if address not found in on hold
	 * @throws ConfigurationException if mail configuration missing or not complete
	 */
	public static AddressOnHold findMailOnHold(String recipient) throws NotFoundException, ConfigurationException {
		removeExpiredFromOnHold();
		MongoConnection connection = new MongoConnection();
		Document document = getWaitingCollection(connection).find(eq("mailAddress.address", recipient)).first();
		connection.disconnect();
		if (document != null) {
			return (AddressOnHold) DocumentConverter.toObject(AddressOnHold.class, document);
		}
		throw new NotFoundException("Address On Hold " + recipient);
	}

	/** Checks if given mail address is confirmed
	 * 
	 * @param address Mail address of the recipient
	 * @return true if address is confirmed, false if not
	 */
	public static boolean isMailAddressConfirmed(String address){
		MongoConnection connection = new MongoConnection();
		Document document = getConfirmedCollection(connection).find(eq("address", address)).first();
		connection.disconnect();
		if (document != null) {
			return true;
		}
		return false;
	}

	// Delete

	/** Removes mail address from confirmed addresses
	 * 
	 * @param recipient Mail address of the recipient
	 * @return true if address is removed
	 * @throws NotFoundException if address not found in confirmed
	 */
	protected static boolean removeConfirmedMail(String recipient) throws NotFoundException {
		MongoConnection connection = new MongoConnection();
		DeleteResult result = getConfirmedCollection(connection).deleteOne(eq("address", recipient));
		connection.disconnect();
		if (result.getDeletedCount() == 1) {
			return true;
		}
		throw new NotFoundException("Mail Address " + recipient);
	}

	/** Removes mail address from addresses on hold
	 * 
	 * @param recipient Mail address of the recipient
	 * @return true if address is removed
	 * @throws NotFoundException if address not found in on hold
	 */
	protected static boolean removeOnHoldMail(String recipient) throws NotFoundException {
		MongoConnection connection = new MongoConnection();
		DeleteResult result = getWaitingCollection(connection)
				.deleteOne(eq("mailAddress.address", recipient));
		connection.disconnect();
		if (result.getDeletedCount() == 1) {
			return true;
		}
		throw new NotFoundException("Mail Address " + recipient);
	}

	// Collections

	/** Gets collection of all confirmed addresses. Collection is created if not existing
	 * 
	 * @param connection Established connection to the database
	 * @return MongoCollection object with all confirmed addresses
	 */
	protected static MongoCollection<Document> getConfirmedCollection(MongoConnection connection) {
		MongoCollection<Document> collection = connection.getCollection(mailConfirmedCollection);
		if (collection == null) {
			connection.getDatabase().createCollection(mailConfirmedCollection);
			collection = connection.getCollection(mailConfirmedCollection);
		}
		return collection;
	}

	/** Gets collection of all addresses on hold. Collection is created if not existing
	 * 
	 * @param connection Established connection to the database
	 * @return MongoCollection object with all addresses on hold
	 */
	protected static MongoCollection<Document> getWaitingCollection(MongoConnection connection) {
		MongoCollection<Document> collection = connection.getCollection(waitingConfirationCollection);
		if (collection == null) {
			connection.getDatabase().createCollection(waitingConfirationCollection);
			collection = connection.getCollection(waitingConfirationCollection);
		}
		return collection;
	}

	// Utilities

	/** Removes all addresses, where the delete time is expired
	 * 
	 * @throws ConfigurationException if mail configuration missing or not complete
	 */
	protected static void removeExpiredFromOnHold() throws ConfigurationException {
		long currentTime = System.currentTimeMillis();
		long difference;
		long deleteTimeout = Integer.parseInt(SystemConfiguration.getValue("deleteTimeout"))*1000;

		MongoConnection connection = new MongoConnection();
		for (Document document : getWaitingCollection(connection).find()) {
			AddressOnHold onHold = (AddressOnHold) DocumentConverter.toObject(AddressOnHold.class, document);
			difference = currentTime - onHold.getVerificationRequestTime();
			if (difference > deleteTimeout) {
				try {
					removeOnHoldMail(onHold.getMailAddress().getAddress());
				} catch (NotFoundException e) {
				}
			}
		}
		connection.disconnect();
	}
}
