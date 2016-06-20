package org.openshift.userservices.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.openshift.userservices.domain.User;

import java.util.List;

public interface DBConnection {

    void insertUser(DBObject user);

    List<User> findAllUsers();

    User findUser(DBObject query);
}
