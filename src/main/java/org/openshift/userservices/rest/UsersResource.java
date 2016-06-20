package org.openshift.userservices.rest;

import com.mongodb.BasicDBObject;
import org.openshift.userservices.domain.User;
import org.openshift.userservices.mongo.DBConnection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RequestScoped
@Path("/parks")
public class UsersResource {

    private final DBConnection dbConnection;

    @Inject
    public UsersResource(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @GET()
    @Produces("application/json")
    public List<User> getAllUsers() {
        return dbConnection.findAllUsers();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("findmail")
    public User findByEmail(@QueryParam("email") String email) {
        // make the query object
        BasicDBObject query = new BasicDBObject("email", email);
        System.out.println("query by email: " + query.toString());
        return dbConnection.findUser(query);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("findname")
    public User findByNameAndSurname(@QueryParam("name") String name, @QueryParam("surname") String surname) {
        // make the query object
        BasicDBObject query = new BasicDBObject("name", name).append("surname", surname);
        System.out.println("query by name: " + query.toString());
        return dbConnection.findUser(query);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("insertUser")
    public void insertUser(User toUser) {
        BasicDBObject document = new BasicDBObject("name", toUser.getName()).append("surname", toUser.getSurname())
                .append("email", toUser.getEmail()).append("confirmed", toUser.getConfirmed());
        dbConnection.insertUser(document);
    }
}
