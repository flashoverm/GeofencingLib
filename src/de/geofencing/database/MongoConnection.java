package de.geofencing.database;

import java.util.Arrays;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.geofencing.log.LogEntry;
import de.geofencing.system.SystemConfiguration;
import de.geofencing.system.exceptions.ConfigurationException;

/** Establishes connection to MongoDB
 * 
 * @author Markus Thral
 *
 */
public class MongoConnection{
	
	private static final String geofenceCollection = "geofences";
	private static final String beaconCollection = "beacons";
	private static final String eventCollection = "events";
	private static final String deviceCollection = "devices";
	
	private MongoClient client;
	private MongoDatabase database;
	
	/** Sets up connection to database defined in the Configuration. 
	 * Should be disconnected after use
	 * 
	 */
	public MongoConnection(){
		try{
			MongoCredential mongoCredentials = MongoCredential.createCredential(
					SystemConfiguration.getValue(SystemConfiguration.dbUser), 
					SystemConfiguration.getValue(SystemConfiguration.dbName),
					SystemConfiguration.getValue(SystemConfiguration.dbPassword).toCharArray());
			
	        client = new MongoClient(
	        		new ServerAddress(
	        				SystemConfiguration.getValue(SystemConfiguration.dbAddress), 
	        				Integer.valueOf(SystemConfiguration.getValue(SystemConfiguration.dbPort))),	
	        		Arrays.asList(mongoCredentials));

	        database = client.getDatabase(SystemConfiguration.getValue(SystemConfiguration.dbName));    
		}catch(ConfigurationException e){
			LogEntry.c(e);
		}
	}
	
	/** Gets database defined in the Configuration
	 * 
	 * @return MongoDatabase object
	 */
	public MongoDatabase getDatabase(){
		return database;
	}
	
	/** Gets collection with the given name from the database 
	 * 
	 * @param collection Name of the Collection
	 * @return MongoCollection of all documents of the collection
	 */
	public MongoCollection<Document> getCollection(String collection){
		try{
			return database.getCollection(collection);
		}catch(IllegalArgumentException e){
			return null;
		}
	}
	
	/** Gets collection of all Geofences. Collection is created if not existing
	 * 
	 * @return MongoCollection object with all Geofences
	 */
	public MongoCollection<Document> getGeofenceCollection(){
		MongoCollection<Document> collection = getCollection(geofenceCollection);
		if(collection == null){
			getDatabase().createCollection(geofenceCollection);
			collection = getCollection(geofenceCollection);
		}
		return collection;
	}
	
	/** Gets collection of all beacons. Collection is created if not existing
	 * 
	 * @return MongoCollection object with all beacons
	 */
	public MongoCollection<Document> getBeaconCollection(){
		MongoCollection<Document> collection = getCollection(beaconCollection);
		if(collection == null){
			getDatabase().createCollection(beaconCollection);
			collection = getCollection(beaconCollection);
		}
		return collection;
	}

	/** Gets collection of all Events. Collection is created if not existing
	 * 
	 * @return MongoCollection object with all Events
	 */
	public MongoCollection<Document> getEventCollection(){		
		MongoCollection<Document> collection = getCollection(eventCollection);
		if(collection == null){
			getDatabase().createCollection(eventCollection);
			collection = getCollection(eventCollection);
		}
		return collection;
	}
	
	/** Gets collection of all Devices. Collection is created if not existing
	 * 
	 * @return MongoCollection object with all Devices
	 */
	public MongoCollection<Document> getDeviceCollection(){
		MongoCollection<Document> collection = getCollection(deviceCollection);
		if(collection == null){
			getDatabase().createCollection(deviceCollection);
			collection = getCollection(deviceCollection);
		}
		return collection;	
	}
	
	/** Disconnects from the database
	 * 
	 */
	public void disconnect(){
		client.close();
	}
}
