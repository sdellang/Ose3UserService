package org.openshift.userservices.tests;

import com.mongodb.DBObject;
import org.mockito.ArgumentMatcher;

import java.util.HashMap;
import java.util.Map;

public class DBObjectHasKeysAndValues extends ArgumentMatcher<DBObject> {

    private Map<String, Object> matches = new HashMap<>();

    public DBObjectHasKeysAndValues(String key1, Object value1) {
        matches.put(key1, value1);
    }

    public DBObjectHasKeysAndValues(String key1, Object value1, String key2, Object value2) {
        this(key1, value1);
        matches.put(key2, value2);
    }

    @Override
    public boolean matches(Object argument) {
        if (argument != null && DBObject.class.isAssignableFrom(argument.getClass())) {
            DBObject object = (DBObject) argument;
            for (Map.Entry<String, Object> match : matches.entrySet()) {
                Object value = object.get(match.getKey());
                if (value == null || !value.equals(match.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
