package org.openshift.userservices.rest;

import com.mongodb.*;
import org.openshift.userservices.domain.User;
import org.openshift.userservices.mongo.DBConnection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/parks")
public class UsersResource {

	@Inject
	private DBConnection dbConnection;

	private DBCollection getUsersCollection() {
		DB db = dbConnection.getDB();
		DBCollection parkListCollection = db.getCollection("users");

		return parkListCollection;
	}

	private User populateParkInformation(DBObject dataValue) {

		User theUser = new User();
		theUser.setName((String)dataValue.get("name"));
		theUser.setSurname((String)dataValue.get("surname"));
		theUser.setEmail((String)dataValue.get("email"));
		theUser.setConfirmed((Boolean)dataValue.get("confirmed"));
		return theUser;
	}

	// get all the mlb parks
	@GET()
	@Produces("application/json")
	public List<User> getAllUsers() {
		ArrayList<User> allUserList = new ArrayList<User>();

		DBCollection mlbParks = this.getUsersCollection();
		DBCursor cursor = mlbParks.find();
		try {
			while (cursor.hasNext()) {
				allUserList.add(this.populateParkInformation(cursor.next()));
			}
		} finally {
			cursor.close();
		}

		return allUserList;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("findmail")
	public User findByEmail(@QueryParam("email") String email) {

		User foundUser = new User();
		DBCollection users = this.getUsersCollection();

		// make the query object
		BasicDBObject query = new BasicDBObject("email",email);

		System.out.println("query by email: " + query.toString());



		DBCursor cursor = users.find(query);
		try {
			while (cursor.hasNext()) {
				this.populateParkInformation(cursor.next());
			}
		} finally {
			cursor.close();
		}

		return foundUser;
	}


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("findname")
	public User findByEmail(@QueryParam("name") String name, @QueryParam("surname") String surname) {

		User foundUser = new User();
		DBCollection users = this.getUsersCollection();

		// make the query object
		BasicDBObject query = new BasicDBObject("name",name)
				.append("surname",surname);

		System.out.println("query by name: " + query.toString());



		DBCursor cursor = users.find(query);
		try {
			while (cursor.hasNext()) {
				this.populateParkInformation(cursor.next());
			}
		} finally {
			cursor.close();
		}

		return foundUser;
	}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("insertUser")
    public void insertUser(User toUser) {

        DBCollection users = this.getUsersCollection();
        BasicDBObject document = new BasicDBObject("name",toUser.getName()).append("surname",toUser.getSurname())
                .append("email",toUser.getEmail()).append("confirmed",toUser.getConfirmed());

        users.insert(document, WriteConcern.SAFE);

    }
}
