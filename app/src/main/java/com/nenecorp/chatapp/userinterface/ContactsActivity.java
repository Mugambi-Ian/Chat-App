package com.nenecorp.chatapp.userinterface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.nenecorp.chatapp.models.DbSocket;
import com.nenecorp.chatapp.models.chats.ChatRoom;
import com.nenecorp.chatapp.models.user.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsActivity extends AppCompatActivity {
    private ListView contactsListView;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        String userId = DbSocket.dbHandler.appUser.userId;
        contactsListView = findViewById(R.id.AMC_lstContacts);
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.USERS)
                .child(userId)
                .child(Constants.Contacts).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> userIds = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userIds.add(child.getKey());
                }
                cacheUsers(userIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cacheUsers(final ArrayList<String> userIds) {
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> userReq = new ArrayList<>();
                for (String user : userIds) {
                    dataSnapshot.child(user).getRef().keepSynced(true);
                    userReq.add(new User(dataSnapshot.child(user)));
                }
                ContactsAdapter adapter = new ContactsAdapter(ContactsActivity.this, R.layout.activity_contacts, userReq);
                contactsListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void addContact(View view) {
        startActivity(new Intent(this, AddContact.class));
        finish();
    }

    public void backPressed(View view) {
        onBackPressed();
    }

    private class ContactsAdapter extends ArrayAdapter<User> {

        public ContactsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<User> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View parentView = convertView;
            if (parentView == null) {
                parentView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_contact, parent, false);
            }
            final User x = getItem(position);
            final CircleImageView imageView = parentView.findViewById(R.id.LIC_dpUser);
            if (!x.userDp.equals("")) {
                Picasso.get().load(x.userDp).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.get().load(x.userDp).into(imageView);
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(x.userDp).into(imageView);
                    }
                });
            }
            ((TextView) parentView.findViewById(R.id.LIC_userName)).setText(x.userId);
            String name = x.userName;
            name =name.replace("-", " ");
            ((TextView) parentView.findViewById(R.id.LIC_fullName)).setText(name);
            parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openChatGroup(x);
                }
            });
            return parentView;
        }
    }

    private void openChatGroup(User x) {
        User user = DbSocket.dbHandler.appUser;
        Constants.chatRoom = new ChatRoom();
        Constants.chatRoom.setChatId(Constants.getInboxId(x, user));
        startActivity(new Intent(this, ChatScreen.class).putExtra("launcher", "contacts"));
        finish();
    }
}
