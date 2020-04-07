package com.nenecorp.chatapp.userinterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.nenecorp.chatapp.models.chats.Chat;
import com.nenecorp.chatapp.models.chats.ChatRoom;
import com.nenecorp.chatapp.models.user.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatScreen extends AppCompatActivity {
    private ChatRoom chatRoom;
    private User otherUser, appUser;
    private EditText msgBody;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        chatRoom = Constants.chatRoom;
        Constants.chatRoom = null;
        appUser = DbSocket.dbHandler.appUser;
        msgBody = findViewById(R.id.ACS_edInputField);
        msgBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    findViewById(R.id.ACS_btnSend).setEnabled(false);
                } else {
                    findViewById(R.id.ACS_btnSend).setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child(Constants.ChatRooms).child(chatRoom.getChatId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("chatId")) {
                    chatRoom = new ChatRoom(dataSnapshot);
                    updateScreen();
                } else {
                    ArrayList<String> members = new ArrayList<>();
                    String userId = appUser.userId;
                    String other = chatRoom.getChatId().replace(userId, "");
                    members.add(userId);
                    members.add(other);
                    chatRoom.setChatMembers(members);
                    dataSnapshot.getRef().setValue(chatRoom);
                }
                dataSnapshot.getRef().keepSynced(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateScreen() {
        for (String id : chatRoom.getChatMembers()) {
            if (!DbSocket.dbHandler.appUser.userId.equals(id)) {
                otherUser = DbHandler.userContacts.get(id);
                break;
            }
        }
        final CircleImageView imageView = findViewById(R.id.ACS_dpImage);
        if (!otherUser.userDp.equals("")) {
            Picasso.get().load(otherUser.userDp).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    Picasso.get().load(otherUser.userDp).into(imageView);
                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(otherUser.userDp).into(imageView);
                }
            });
        }
        String name = otherUser.userName;
        name = name.replace("-", " ");
        ((TextView) findViewById(R.id.ACS_txtFullName)).setText(name);
        ((TextView) findViewById(R.id.ACS_txtStatus)).setText(otherUser.userStatus);
        final ListView chatListView = findViewById(R.id.ACS_lstConvo);
        MessagesAdapter adapter = new MessagesAdapter(this, R.layout.activity_chat_screen, chatRoom.getChatMessagesList());
        chatListView.setAdapter(adapter);
        Log.i("TAG", "updateScreen: " + appUser.userLang + " " + otherUser.userLang);
        if (appUser.userLang != null) {
            if (!appUser.userLang.equals(otherUser.userLang)) {
                chatRoom.translateMessages(new ChatRoom.TranslateInterface() {
                    @Override
                    public void onComplete(ChatRoom x) {
                        Toast.makeText(ChatScreen.this, "Translation is now active", Toast.LENGTH_SHORT).show();
                        chatRoom = x;
                        MessagesAdapter adapter = new MessagesAdapter(ChatScreen.this, R.layout.activity_chat_screen, chatRoom.getChatMessagesList());
                        chatListView.setAdapter(adapter);
                    }
                }, this, appUser, otherUser);
            }
        }
    }

    public void hideKeyboard() {
        Activity activity = this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void sendMessage(View view) {
        hideKeyboard();
        chatRoom.sendMessage(msgBody.getText().toString());
        msgBody.setText("");
    }

    private class MessagesAdapter extends ArrayAdapter<Chat> {
        MessagesAdapter(@NonNull Context context, int resource, ArrayList<Chat> chats) {
            super(context, resource, chats);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View parentView = convertView;
            Chat x = getItem(position);
            if (x.getSenderId().equals(appUser.userId)) {
                if (parentView == null) {
                    parentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sent, parent, false);
                }
            } else {
                if (parentView == null) {
                    parentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recieved, parent, false);
                }
            }
            ((TextView) parentView.findViewById(R.id.LIS_txtDate)).setText(x.getMsgDate() + " " + x.getMsgTime());
            ((TextView) parentView.findViewById(R.id.LIS_txtMessage)).setText(x.getMsgMessage());
            return parentView;
        }


    }
}

