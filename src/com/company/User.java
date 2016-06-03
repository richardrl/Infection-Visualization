package com.company;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by richard on 6/1/16.
 */
public class User {
    public float version;
    public UUID userID;

    public User(float version, UUID userID) {
        this.version = version;
        this.userID = userID;
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }
}
