package org.openshift.userservices.tests;


import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openshift.userservices.domain.User;
import org.openshift.userservices.mongo.DBConnection;
import org.openshift.userservices.rest.UsersResource;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserTest {

    @Mock
    private DBConnection dbConnection;

    @Test
    public void testInsertUser() {
        String expected_name = "nome di prova";
        String expected_surname = "cognome di prova";
        User user = buildUser(expected_name, expected_surname);

        reset(dbConnection);

        UsersResource usersResource = new UsersResource(dbConnection);
        usersResource.insertUser(user);

        ArgumentCaptor<DBObject> dbObjectCaptor = ArgumentCaptor.forClass(DBObject.class);
        verify(dbConnection, times(1)).insertUser(dbObjectCaptor.capture());

        DBObject value = dbObjectCaptor.getValue();
        Assert.assertEquals(4, value.keySet().size());
        Assert.assertEquals(expected_name, value.get("name"));
        Assert.assertEquals(expected_surname, value.get("surname"));
        Assert.assertNull(value.get("email"));
        Assert.assertNull(value.get("confirmed"));
    }

    @Test
    public void testFindByNameAndSurname() {
        String expected_name = "nome di prova";
        String expected_surname = "cognome di prova";

        reset(dbConnection);

        UsersResource usersResource = new UsersResource(dbConnection);

        usersResource.findByNameAndSurname(expected_name, expected_surname);

        verify(dbConnection, times(1)).findUser(argThat(new DBObjectHasKeysAndValues("name", expected_name, "surname", expected_surname)));
    }

    @Test
    public void testFindUserByEmail() {
        String expected_email = "email@prova.it";

        reset(dbConnection);

        UsersResource usersResource = new UsersResource(dbConnection);

        usersResource.findByEmail(expected_email);

        verify(dbConnection, times(1)).findUser(argThat(new DBObjectHasKeysAndValues("email", expected_email)));
    }

    @Test
    public void testFindAllUsers() {
        reset(dbConnection);

        UsersResource usersResource = new UsersResource(dbConnection);

        usersResource.getAllUsers();

        verify(dbConnection, times(1)).findAllUsers();
    }

    private User buildUser(String name, String surname) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        return user;
    }

}
