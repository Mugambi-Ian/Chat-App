package com.nenecorp.chatapp.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nenecorp.chatapp.assets.resources.Constants;
import com.nenecorp.chatapp.models.chats.ChatRoom;
import com.nenecorp.chatapp.models.user.User;

import java.util.ArrayList;
import java.util.HashMap;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DbHandler {
    public static boolean userExists = false;
    public User appUser;
    public static  HashMap<String, User> userContacts;

    DbHandler() {
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                userExists = firebaseAuth.getCurrentUser() != null;
                if (userExists) {
                    loadUserData();
                }
            }
        });


    }

    private void loadUserData() {
        String userId = getUserId();
        Log.i(TAG, "loadUserData: " + userId);
        appUser = new User(userId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                appUser = new User(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userRef.keepSynced(true);
    }

    private String getUserId() {
        String[] x = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@");
        return x[0];
    }


}
