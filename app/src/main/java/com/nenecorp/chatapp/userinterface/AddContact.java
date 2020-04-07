package com.nenecorp.chatapp.userinterface;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nenecorp.chatapp.R;
import com.nenecorp.chatapp.assets.resources.Constants;
import com.nenecorp.chatapp.models.DbSocket;
import com.nenecorp.chatapp.models.user.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddContact extends AppCompatActivity {
    private EditText userName;
    private String userId;
    private ListView listView;
    private View lytAddContact;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ContactsActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        userName = findViewById(R.id.AC_edUname);
        userId = DbSocket.dbHandler.appUser.userId;
        listView = findViewById(R.id.AC_lstReq);
        lytAddContact = findViewById(R.id.AC_lytAc);
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child(Constants.Requests).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    Toast.makeText(AddContact.this, "No requests", Toast.LENGTH_SHORT).show();
                    RequestAdapter adapter = new RequestAdapter(AddContact.this, R.layout.activity_add_contact, new ArrayList<User>());
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(AddContact.this, "New Requests", Toast.LENGTH_SHORT).show();
                    ArrayList<String> userIds = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        userIds.add(child.getKey());
                    }
                    cacheUsers(userIds);
                }
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
                RequestAdapter adapter = new RequestAdapter(AddContact.this, R.layout.activity_add_contact, userReq);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void sendRequest(View view) {
        if (userName.getText().length() == 0) {
            userName.setError("You missed");
        } else {
            final String uId = userName.getText().toString();
            findViewById(R.id.AC_btnTxt).setVisibility(View.INVISIBLE);
            findViewById(R.id.AC_pBar).setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("userId")) {
                        dataSnapshot.child(Constants.Requests).child(userId).getRef().setValue(userId);
                        Toast.makeText(AddContact.this, "Request sent successfully", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.AC_btnTxt).setVisibility(View.VISIBLE);
                        findViewById(R.id.AC_pBar).setVisibility(View.INVISIBLE);
                    } else {
                        Toast.makeText(AddContact.this, "Confirm the username and try again", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.AC_btnTxt).setVisibility(View.VISIBLE);
                        findViewById(R.id.AC_pBar).setVisibility(View.INVISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void cancelReg(View view) {
        onBackPressed();
    }

    public void addContact(View view) {
        if (lytAddContact.getVisibility() != View.VISIBLE) {
            TextView x = (TextView) view;
            x.setTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.AC_btnReq)).setTextColor(Color.GRAY);
            listView.setVisibility(View.GONE);
            YoYo.with(Techniques.SlideInLeft)
                    .duration(300)
                    .repeat(0)
                    .onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            lytAddContact.setVisibility(View.VISIBLE);
                        }
                    }).playOn(lytAddContact);
        }
    }

    public void showRequest(View view) {
        if (listView.getVisibility() != View.VISIBLE) {
            TextView x = (TextView) view;
            x.setTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.AC_btnContacts)).setTextColor(Color.GRAY);
            lytAddContact.setVisibility(View.GONE);
            YoYo.with(Techniques.SlideInLeft)
                    .duration(300)
                    .repeat(0)
                    .onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            listView.setVisibility(View.VISIBLE);
                        }
                    }).playOn(listView);
        }
    }

    private class RequestAdapter extends ArrayAdapter<User> {

        RequestAdapter(@NonNull Context context, int resource, @NonNull ArrayList<User> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View parentView = convertView;
            if (parentView == null) {
                parentView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_request, parent, false);
            }
            final User x = getItem(position);
            final CircleImageView imageView = parentView.findViewById(R.id.LIR_dpImage);
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
            ((TextView) parentView.findViewById(R.id.LIR_userName)).setText(x.userId);
            String name = x.userName;
            name = name.replace("-", " ");
            ((TextView) parentView.findViewById(R.id.LIR_fullName)).setText(name);
            parentView.findViewById(R.id.LIR_btnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmRequest(x);
                }
            });
            parentView.findViewById(R.id.LIR_btnReject).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rejectRequest(x);
                }
            });
            return parentView;
        }
    }

    private void rejectRequest(User x) {
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child(Constants.Requests).child(x.userId).setValue(null);
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(x.userId).keepSynced(false);
    }

    private void confirmRequest(User x) {
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child(Constants.Requests).child(x.userId).setValue(null);
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId).child(Constants.Contacts).child(x.userId).setValue(x.userId);
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(x.userId).child(Constants.Contacts).child(userId).setValue(userId);
    }
}
