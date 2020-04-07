package com.nenecorp.chatapp.userinterface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nenecorp.chatapp.R;
import com.nenecorp.chatapp.assets.resources.Constants;
import com.nenecorp.chatapp.models.DbHandler;
import com.nenecorp.chatapp.models.DbSocket;
import com.nenecorp.chatapp.models.chats.ChatRoom;
import com.nenecorp.chatapp.models.user.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    Hashtable<String, ChatItem> chatItems;
    private User appUser;
    private HashMap<String, User> userContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        chatItems = new Hashtable<>();
        appUser = DbSocket.dbHandler.appUser;
        final String userId = appUser.userId;
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child(Constants.Contacts).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> userIds = new ArrayList<>();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    userIds.add(s.getKey());
                    cacheUsers(userIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child("userStatus").onDisconnect().setValue(Constants.Offline);
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child("userStatus").setValue(Constants.Online);
    }

    private void cacheUsers(final ArrayList<String> userIds) {
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userContacts = new HashMap<>();
                for (String user : userIds) {
                    Log.i("TAG", "onDataChange: " + user);
                    dataSnapshot.child(user).getRef().keepSynced(true);
                    userContacts.put(user, new User(dataSnapshot.child(user)));
                }
                loadInbox(userContacts);
                DbHandler.userContacts = userContacts;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadInbox(final HashMap<String, User> userReq) {
        final int[] x = new int[1];
        for (String userId : userReq.keySet()) {
            User user = userReq.get(userId);
            String inboxId = Constants.getInboxId(appUser, user);
            FirebaseDatabase.getInstance().getReference().child(Constants.ChatRooms).child(inboxId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("chatId")) {
                        dataSnapshot.getRef().keepSynced(true);
                        dataSnapshot.getRef().addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                x[0]++;
                                ChatRoom c = new ChatRoom(dataSnapshot);
                                ChatItem ci;
                                if (chatItems.get(c.getChatId()) != null) {
                                    boolean newText = false;
                                    if (chatItems.get(c.getChatId()).chatRoom.getChatMessagesList().size() != c.getChatMessagesList().size()) {
                                        newText = true;
                                    }
                                    ci = chatItems.get(c);
                                    if (ci != null) {
                                        ci.unreadMessages = newText;
                                    }
                                } else {
                                    ci = new ChatItem(c);
                                }
                                if (c.getChatId() != null && ci != null) {
                                    chatItems.put(c.getChatId(), ci);
                                }
                                if (x[0] >= userReq.size()) {
                                    displayInbox();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayInbox() {
        ListView listView = findViewById(R.id.AH_lstMessages);
        MessageAdapter adapter = new MessageAdapter(this, R.layout.activity_home, getChatItems(chatItems));
        listView.setAdapter(adapter);
    }

    public void myProfile(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    public void myContacts(View view) {
        startActivity(new Intent(this, ContactsActivity.class));
        finish();
    }

    ArrayList<ChatItem> getChatItems(Hashtable<String, ChatItem> chatItems) {
        ArrayList<ChatItem> items = new ArrayList<>();
        for (String item : chatItems.keySet()) {
            items.add(chatItems.get(item));
        }
        return items;
    }


    public void myLanguage(View view) {
        startActivity(new Intent(this, MyLanguage.class));
        finish();
    }

    class ChatItem {
        boolean unreadMessages;
        ChatRoom chatRoom;
        User displayUser;

        ChatItem(ChatRoom chatRoom) {
            this.chatRoom = chatRoom;
            unreadMessages = Constants.chatState(chatRoom.getChatId(), HomeActivity.this);
            for (String x : chatRoom.getChatMembers()) {
                if (!appUser.userId.equals(x)) {
                    displayUser = userContacts.get(x);
                    break;
                }
            }
        }
    }

    private class MessageAdapter extends ArrayAdapter<ChatItem> {

        MessageAdapter(@NonNull Context context, int resource, @NonNull List<ChatItem> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View parentView = convertView;
            if (parentView == null) {
                parentView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_message, parent, false);
            }
            final ChatItem x = getItem(position);
            final CircleImageView imageView = parentView.findViewById(R.id.LIM_dpProfile);
            if (x != null) {
                if (!x.displayUser.userDp.equals("")) {
                    Picasso.get().load(x.displayUser.userDp).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.get().load(x.displayUser.userDp).into(imageView);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(x.displayUser.userDp).into(imageView);
                        }
                    });
                }
                ((TextView) parentView.findViewById(R.id.LIM_txtFullName)).setText(x.displayUser.userName);
                if (x.chatRoom.getChatMessagesList().size() != 0) {
                    ((TextView) parentView.findViewById(R.id.LIM_txtMsgDate)).setText(x.chatRoom.getChatMessagesList().get(x.chatRoom.getChatMessagesList().size() - 1).getMsgDate());
                    ((TextView) parentView.findViewById(R.id.LIM_txtMsgLast)).setText(x.chatRoom.getChatMessagesList().get(x.chatRoom.getChatMessagesList().size() - 1).getMsgMessage());
                } else {
                    ((TextView) parentView.findViewById(R.id.LIM_txtMsgDate)).setText("any-day-now");
                    ((TextView) parentView.findViewById(R.id.LIM_txtMsgLast)).setText("...");
                }
                String name = x.displayUser.userName;
                name =name.replace("-", " ");
                ((TextView) parentView.findViewById(R.id.LIM_txtFullName)).setText(name);
                if (x.unreadMessages) {
                    parentView.findViewById(R.id.LIM_indicator).setVisibility(View.VISIBLE);
                } else {
                    parentView.findViewById(R.id.LIM_indicator).setVisibility(View.GONE);
                }
                parentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openChat(x.chatRoom);
                    }
                });
            }
            return parentView;
        }
    }

    private void openChat(ChatRoom chatRoom) {
        Constants.chatRoom = chatRoom;
        startActivity(new Intent(this, ChatScreen.class).putExtra("launcher", "home"));
        finish();
    }


}
