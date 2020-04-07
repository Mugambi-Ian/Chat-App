package com.nenecorp.chatapp.models.user;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nenecorp.chatapp.assets.interfaces.OnLoadComplete;
import com.nenecorp.chatapp.assets.resources.Constants;
import com.nenecorp.chatapp.models.chats.ChatRoom;

import java.util.ArrayList;

public class User {
    public String userId;
    public String userName;
    public String userDp;
    public String userStatus;
    public String userLang;
    public ArrayList<ChatRoom> chatRooms;
    public ArrayList<Contact> userContacts;

    public User(String userId, String userName, String userDp) {
        this.userId = userId;
        this.userName = userName;
        this.userDp = userDp;
    }

    public User(DataSnapshot dataSnapshot) {
        userId = dataSnapshot.child("userId").getValue(String.class);
        userName = dataSnapshot.child("userName").getValue(String.class);
        userDp = dataSnapshot.child("userDp").getValue(String.class);
        userStatus = dataSnapshot.child("userStatus").getValue(String.class);
        if (userStatus == null || userStatus.equals("")) {
            userStatus = Constants.Offline;
        }
        userLang = dataSnapshot.child("userLang").getValue(String.class);
        getUserContacts(new OnLoadComplete() {
            @Override
            public void loadResult(Object o) {
                userContacts = (ArrayList<Contact>) o;
            }
        }, dataSnapshot.child("userContacts"));
        getUserChats(new OnLoadComplete() {
            @Override
            public void loadResult(Object o) {
                chatRooms = (ArrayList<ChatRoom>) o;
            }
        }, dataSnapshot.child("userChatRooms"));
    }

    public User(String userId) {
        this.userId = userId;
    }

    private void getUserChats(final OnLoadComplete onLoadComplete, DataSnapshot userChatRooms) {
        final ArrayList<ChatRoom> cRooms = new ArrayList<>();
        final int i = (int) userChatRooms.getChildrenCount();
        final int[] j = {0};
        for (final DataSnapshot x : userChatRooms.getChildren()) {
            String roomId = x.getValue(String.class);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.ChatRooms).child(roomId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ChatRoom chatRoom = new ChatRoom(dataSnapshot);
                    cRooms.add(chatRoom);
                    if (i == j[0]) {
                        onLoadComplete.loadResult(cRooms);
                    }
                    j[0]++;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            reference.keepSynced(true);
        }
    }

    private void getUserContacts(final OnLoadComplete onLoadComplete, DataSnapshot userContacts) {
        final ArrayList<Contact> contacts = new ArrayList<>();
        final int i = (int) userContacts.getChildrenCount();
        final int[] j = {0};
        for (final DataSnapshot x : userContacts.getChildren()) {
            String userId = x.getValue(String.class);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Contact contact = new Contact(dataSnapshot);
                    contacts.add(contact);
                    if (i == j[0]) {
                        onLoadComplete.loadResult(contacts);
                    }
                    j[0]++;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            reference.keepSynced(true);
        }
    }


    public void saveDetails(DatabaseReference ref) {
        ref.child("userId").setValue(userId);
        ref.child("userName").setValue(userName);
        ref.child("userDp").setValue(userDp);
        ref.child("userLang").setValue(userLang);
        ref.child("userStatus").setValue(Constants.Online);
    }
}
