package de.geofencing.database;

import java.io.IOException;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.mongodb.util.JSON;

import de.geofencing.event.Event;
import de.geofencing.log.LogEntry;

/** Provides methods to de-/serialize documents from the MongoDB
 * Uses Mongo Java Driver (Needs mongo-java-driver-3.4.2.jar or newer)
 * Uses Jackson for serializing (Needs jackson 2.3.2 or newer)
 * 
 * @author Markus Thral
 *
 */
public class DocumentConverter {

	/** Converts the given Document to an Object with the given class
	 * 
	 * @param classType Class saved in the document
	 * @param document Document to be converted
	 * @return Object with the given class
	 */
	public static Object toObject(Class<?> classType, Document document){
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY));
		try {
			document.remove("_id");
			String json = JSON.serialize(document);
			if(classType.equals(Event.class)){
				return Event.jsonToEvent(json);
			}
			return mapper.readValue(json, classType);
		} catch (IOException | ClassNotFoundException e) {
			LogEntry.c(e);
			return null;
		}
	}
	
	/** Converts given Object to document for saving in database
	 * 
	 * @param object Object to be converted
	 * @return Document of the object
	 */
	public static Document toDocument(Object object){
		ObjectMapper mapper = new ObjectMapper();
		try{
			return Document.parse(mapper.writeValueAsString(object));
		} catch (IOException e) {
			LogEntry.c(e);
			return null;
		}
	}
}
