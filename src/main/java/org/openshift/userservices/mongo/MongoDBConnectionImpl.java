package org.openshift.userservices.mongo;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.openshift.userservices.domain.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Named
@ApplicationScoped
public class MongoDBConnectionImpl implements DBConnection {

    private static final String USERS_COLLECTION_NAME = "users";

    private final DB mongoDB;

    public MongoDBConnectionImpl() {
        String mongoHost = (System.getenv("MONGODB_SERVICE_HOST") == null) ? "127.0.0.1" : System.getenv("MONGODB_SERVICE_HOST");
        String mongoPort = (System.getenv("MONGODB_SERVICE_PORT") == null) ? "27017" : System.getenv("MONGODB_SERVICE_PORT");
        String mongoUser = (System.getenv("MONGODB_USER") == null) ? "userservices" : System.getenv("MONGODB_USER");
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

        if (!mongoDB.authenticate(mongoUser, mongoPassword.toCharArray())) {
            System.out.println("Failed to authenticate DB ");
        }

        this.initDatabase(mongoDB);
    }

    @Override
    public void insertUser(DBObject user) {
        DBCollection users = getDB().getCollection(USERS_COLLECTION_NAME);
        users.insert(user, WriteConcern.SAFE);
    }

    @Override
    public List<User> findAllUsers() {
        DBCollection users = getDB().getCollection(USERS_COLLECTION_NAME);
        return this.findUser(users, null);
    }

    @Override
    public User findUser(DBObject query) {
        DBCollection collection = getDB().getCollection(USERS_COLLECTION_NAME);
        List<User> users = this.findUser(collection, query);
        return users.isEmpty() ? null : users.get(0);
    }

    private DB getDB() {
        return mongoDB;
    }

    private void initDatabase(DB mongoDB) {
        DBCollection parkListCollection = mongoDB.getCollection("users");
        int teamsImported = 0;
        if (parkListCollection.count() < 1) {
            System.out.println("The database is empty.  We need to populate it");
            try {
                String currentLine = new String();
                URL jsonFile = new URL("https://raw.githubusercontent.com/sdellang/Ose3UserService/master/users.json");
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

    private List<User> findUser(DBCollection collection, DBObject query) {
        DBCursor cursor;
        List<User> allUserList = new ArrayList<>();

        if (query == null) {
            cursor = collection.find();
        } else {
            cursor = collection.find(query);
        }

        if (cursor != null) {
            try {
                while (cursor.hasNext()) {
                    allUserList.add(this.populateParkInformation(cursor.next()));
                }
            } finally {
                cursor.close();
            }
        }

        return allUserList;
    }

    private User populateParkInformation(DBObject dataValue) {
        User theUser = new User();
        theUser.setName((String) dataValue.get("name"));
        theUser.setSurname((String) dataValue.get("surname"));
        theUser.setEmail((String) dataValue.get("email"));
        theUser.setConfirmed(Boolean.valueOf((String) dataValue.get("confirmed")));
        return theUser;
    }

}
