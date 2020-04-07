package com.nenecorp.chatapp.models;

public class DbSocket {
    public static DbHandler dbHandler;

    public DbSocket() {
        dbHandler = new DbHandler();
    }

    public static boolean isLoaded() {
        return dbHandler != null;
    }
}
