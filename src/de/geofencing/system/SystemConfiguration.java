package de.geofencing.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import de.geofencing.log.LogEntry;
import de.geofencing.system.exceptions.ConfigurationException;
import de.geofencing.system.exceptions.UnauthorizedExcpetion;

/** Provides methods for reading the configuration file and setting entries as key-value pairs.
 * 
 * @author Markus Thral
 *
 */
public class SystemConfiguration{

	private static final String adminPassword = "adminPassword";
	private static final String uuid = "uuid";
	/** Configuration value: Address of the database server (i.e. 127.0.0.1)
	 */
	public static final String dbAddress = "dbAddress";
	/** Configuration value: Port of the database server
	 */
	public static final String dbPort = "dbPort";
	/** Configuration value: Name of the database
	 */
	public static final String dbName = "dbDatabase";

	/** Configuration value: User of the database
	 */
	public static final String dbUser = "dbUser";
	/** Configuration value: Password of the database user
	 */
	public static final String dbPassword = "dbPassword";

	/** Saves Configuration in the file
	 * 
	 * @param properties Properties with key-value pairs
	 * @return true if saving is done
	 * @throws ConfigurationException if the file couldn't be written
	 */
	protected static boolean save(Properties properties) throws ConfigurationException {
		try {
			FileOutputStream out = new FileOutputStream(getFile());
			properties.store(out, "");
			return true;
		} catch (IOException e) {
			throw new ConfigurationException(ConfigurationException.Error.CouldntWriteFile);
		}
	}
	
	/** Loads Configuration from the file
	 * 
	 * @return Configuration as Properties object
	 * @throws ConfigurationException if file is not found or couln't be read
	 */
	protected static Properties load() throws ConfigurationException{
		File file = getFile();
		Properties properties = new Properties();
		try{
			if(file.exists()){
				FileInputStream in = new FileInputStream(file);
				properties.load(in);
				return properties;
			}
			generateConfiguration();
			throw new ConfigurationException(ConfigurationException.Error.FileNotExisiting);
		}catch(IOException e){
			throw new ConfigurationException(ConfigurationException.Error.Other);
		}
	}
	
	/** Generates file and empty configuration parameter
	 * 
	 */
	protected static void generateConfiguration(){
		try {
			File configFile = getFile();
			FileOutputStream out = new FileOutputStream(configFile);
			Properties configuration = new Properties();
			configuration.store(out, "");
			
			setValue(adminPassword, "");
			setValue(dbAddress, "");
			setValue(dbPort, "");
			setValue(dbName, "");
			setValue(dbUser, "");
			setValue(dbPassword, "");
			
			LogEntry.c("Configuration file not found - Generated empty file in " + configFile.getAbsolutePath());
			
		} catch (IOException e) {
			LogEntry.c(e);
		}
	}

	/** Gets value from configuration
	 * 
	 * @param key Key to get the value for
	 * @return value of the key
	 * @throws ConfigurationException if an error occurs
	 */
	public static String getValue(String key) throws ConfigurationException{
		String value = load().getProperty(key);
		if(value != null){
			if(!value.equals("")){
				return value;
			}
			throw new ConfigurationException(ConfigurationException.Error.ValueNotSet);
		}
		throw new ConfigurationException(ConfigurationException.Error.ValueNotFound);
	}	
	
	/** Sets key and value or updated key if existing
	 * 
	 * @param key Key to get the value for
	 * @param value Value of the key
	 * @return true if value is set, false of an error occurs
	 */
	public static boolean setValue(String key, String value) {
		Properties configuration;
		try{
			configuration = load();
		}catch(ConfigurationException e){
			configuration = new Properties();
		}
		configuration.setProperty(key, value);
		try{
			return save(configuration);
		}catch(ConfigurationException e){
			return false;
		}
	}
	
	/** Checks if the password matches with the configuration
	 * 
	 * @param password Administrator password
	 * @return true if password matches with the configuration
	 * @throws UnauthorizedExcpetion if the password doesn't match
	 * @throws ConfigurationException if password value not set in configuration
	 */
	public static boolean checkPassword(String password) throws UnauthorizedExcpetion, ConfigurationException{
		try{
			if(!getValue(adminPassword).equals(password)){
				throw new UnauthorizedExcpetion();
			}
			return true;
		}catch(ConfigurationException e){
			LogEntry.c("Configuration Error: Password not set in confiuration");
			throw new ConfigurationException(ConfigurationException.Error.ValueNotSet);
		}
	}
	
	/** Gets UUID from the Configuration
	 * 
	 * @return UUID of the system as UUID object
	 */
	public static UUID getUUID(){
		try{
			String uuidValue = getValue(uuid);
			return UUID.fromString(uuidValue);
		}catch(ConfigurationException e){
			UUID generated = UUID.randomUUID();
			setValue(uuid, generated.toString());
			return generated;
		}
	}
	
	/** Gets configuration file
	 * 
	 * @return configuration file as File object
	 */
	public static File getFile(){
		String classesFolder = SystemConfiguration.class.getClassLoader().getResource("/").getPath();
		String directory = classesFolder.substring(0, classesFolder.length()-16);
		return new File(directory+"configuration");
	}

}
