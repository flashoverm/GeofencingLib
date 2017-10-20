package de.geofencing.event.counter;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import de.geofencing.database.DocumentConverter;
import de.geofencing.database.MongoConnection;
import de.geofencing.system.exceptions.NotFoundException;

/** Provides methods to execute CRUD-operations for Counters on the MongoDB
 * 
 * @author Markus Thral
 *
 */
public class CounterDBConnector {
	
	private static final String counterCollection = "counter";
	
	/** Sets next counterID and inserts new Counter in database
	 * 
	 * @return CounterID of the Counter
	 */
	public static int insertCounter(){

		Counter counter = new Counter(getNextCounterID());
		MongoConnection connection = new MongoConnection();
		getCounterCollection(connection).insertOne(DocumentConverter.toDocument(counter));
		connection.disconnect();
		return counter.getCounterID();
	}
	
	/** Gets Counter with given counterID from the database
	 * 
	 * @param counterID CounterID of the Counter
	 * @return Counter object with the given CounterID
	 * @throws NotFoundException if the counterID not existing
	 */
	public static Counter findCounter(int counterID) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		Document document = getCounterCollection(connection)
				.find(eq("counterID", counterID)).first();
		connection.disconnect();
		if(document != null){
			return (Counter)DocumentConverter.toObject(Counter.class, document);
		}
		throw new NotFoundException("Counter " + counterID);
	}
	
	/** Gets list of all Counters in the database
	 * 
	 * @return List of Counters
	 */
	public static List<Counter> findCounterList(){
		MongoConnection connection = new MongoConnection();
		List<Counter> counterList = new ArrayList<>();
		for(Document document : getCounterCollection(connection).find()){
			counterList.add((Counter)DocumentConverter.toObject(Counter.class, document));
		}
		return counterList;
	}
	
	/** Increments Counter with the given counterID
	 * 
	 * @param counterID CounterID of the Counter
	 * @return new value of the Counter
	 * @throws NotFoundException if the counterID not existing
	 */
	public static int incrementCounter(int counterID) throws NotFoundException{
		int value = findCounter(counterID).getValue()+1;
		MongoConnection connection = new MongoConnection();
		MongoCollection<Document> collection = getCounterCollection(connection);
		UpdateResult result = collection.updateOne(
				eq("counterID", counterID), 
				new Document("$set", new Document("value", value+"")));
		connection.disconnect();
		if(result.getModifiedCount() == 1){
			return value;
		}
		throw new NotFoundException("Counter " + counterID);	
	}
	
	/** Decrements Counter with the given counterID. Value is at least zero.
	 * 
	 * @param counterID CounterID of the Counter
	 * @return new value of the Counter
	 * @throws NotFoundException if the counterID not existing
	 */
	public static int decrementCounter(int counterID) throws NotFoundException{
		int value = Math.max(findCounter(counterID).getValue()-1, 0);
		MongoConnection connection = new MongoConnection();
		MongoCollection<Document> collection = getCounterCollection(connection);
		UpdateResult result = collection.updateOne(
				eq("counterID", counterID), 
				new Document("$set", new Document("value", value+"")));
		connection.disconnect();
		if(result.getModifiedCount() == 1){
			return value;
		}
		throw new NotFoundException("Counter " + counterID);
	}
	
	/** Resets the value of the Counter with the given counterID to zero.
	 * 
	 * @param counterID CounterID of the Counter
	 * @return true if the reset is done
	 * @throws NotFoundException if the counterID not existing
	 */
	public static boolean resetCounter(int counterID) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		MongoCollection<Document> collection = getCounterCollection(connection);
		UpdateResult result = collection.updateOne(
				eq("counterID", counterID), 
				new Document("$set", new Document("value", "0")));
		connection.disconnect();
		if(result.getModifiedCount() == 1){
			return true;
		}
		throw new NotFoundException("Counter " + counterID);	
	}
	
	/** Removes Counter with given counterID
	 * 
	 * @param counterID CounterID of the Counter
	 * @return true if counter removed
	 * @throws NotFoundException if the counterID not existing
	 */
	public static boolean removeCounter(int counterID) throws NotFoundException{
		MongoConnection connection = new MongoConnection();
		MongoCollection<Document> collection = getCounterCollection(connection);
		DeleteResult result = collection.deleteOne(eq("counterID", counterID));
		connection.disconnect();
		if(result.getDeletedCount() == 1){
			return true;
		}
		throw new NotFoundException("Counter " + counterID);	
	}
	
	/** Gets collection of all Counters. Collection is created if not existing
	 * 
	 * @param connection Established connection to the database
	 * @return MongoCollection object with all Counters
	 */
	protected static MongoCollection<Document> getCounterCollection(MongoConnection connection){
		MongoCollection<Document> collection = connection.getCollection(counterCollection);
		if(collection == null){
			connection.getDatabase().createCollection(counterCollection);
			collection = connection.getCollection(counterCollection);
		}
		return collection;
	}
	
	/** Generates the next available counterID
	 * 
	 * @return generated counterID
	 */
	protected static int getNextCounterID(){
		int counterID = 1;
		for(Counter counter : findCounterList()){
			if(counter.getCounterID() >= counterID){
				counterID = counter.getCounterID()+1;
			}
		}
		return counterID;
	}
}
