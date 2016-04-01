package org.openshift.mlbparks.userservices.mongo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

@Named
@ApplicationScoped
public class DBConnection {

	private DB mongoDB;

	public DBConnection() {
		super();
	}

	@PostConstruct
	public void afterCreate() {
		String mongoHost = (System.getenv("MONGODB_SERVICE_HOST") == null) ? "127.0.0.1" : System.getenv("MONGODB_SERVICE_HOST");
		String mongoPort = (System.getenv("MONGODB_SERVICE_PORT") == null) ? "27017" : System.getenv("MONGODB_SERVICE_PORT"); 
		String mongoUser = (System.getenv("MONGODB_USER")== null) ? "userservices" : System.getenv("MONGODB_USER");
		String mongoPassword = (System.getenv("MONGODB_PASSWORD") == null) ? "userservices" : System.getenv("MONGODB_PASSWORD");
		String mongoDBName = (System.getenv("MONGODB_DATABASE") == null) ? "userservices" : System.getenv("MONGODB_DATABASE");
		// Check if we are using a mongoDB template or mongodb RHEL 7 image
		if (mongoHost == null) {
			mongoHost = System.getenv("MONGODB_24_RHEL7_SERVICE_HOST");
		} 
		if (mongoPort == null) {
			mongoPort = System.getenv("MONGODB_24_RHEL7_SERVICE_PORT");
		}
		
		int port = Integer.decode(mongoPort);
		
		Mongo mongo = null;
		try {
			mongo = new Mongo(mongoHost, port);
			System.out.println("Connected to database");
		} catch (UnknownHostException e) {
			System.out.println("Couldn't connect to MongoDB: " + e.getMessage() + " :: " + e.getClass());
		}

		mongoDB = mongo.getDB(mongoDBName);

		if (mongoDB.authenticate(mongoUser, mongoPassword.toCharArray()) == false) {
			System.out.println("Failed to authenticate DB ");
		}

		this.initDatabase(mongoDB);

	}

	public DB getDB() {
		return mongoDB;
	}

	private void initDatabase(DB mongoDB) {
		DBCollection parkListCollection = mongoDB.getCollection("users");
		int teamsImported = 0;
		if (parkListCollection.count() < 1) {
			System.out.println("The database is empty.  We need to populate it");
			try {
				String currentLine = new String();
				URL jsonFile = new URL(
						"http://163.162.61.163/cloud-repo/openshift/userservices.json");
				BufferedReader in = new BufferedReader(new InputStreamReader(jsonFile.openStream()));
				while ((currentLine = in.readLine()) != null) {
					parkListCollection.insert((DBObject) JSON.parse(currentLine.toString()));
					teamsImported++;
				}
				System.out.println("Successfully imported " + teamsImported + " teams.");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}