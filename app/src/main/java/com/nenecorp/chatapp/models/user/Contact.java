package com.nenecorp.chatapp.models.user;

import com.google.firebase.database.DataSnapshot;

public class Contact {
    private String userName;
    private String userId;
    private String userStatus;
    private String userDp;

    public Contact(DataSnapshot contactRef) {
        userName = contactRef.child("userName").getValue(String.class);
        userId =contactRef.child("userId").getValue(String.class);
        userStatus = contactRef.child("userStatus").getValue(String.class);
        userDp = contactRef.child("userDp").getValue(String.class);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserDp() {
        return userDp;
    }

    public void setUserDp(String userDp) {
        this.userDp = userDp;
    }
}
