package com.nenecorp.chatapp;

import android.app.Application;
import android.os.AsyncTask;

import com.google.firebase.database.FirebaseDatabase;
import com.medavox.library.mutime.MuTime;
import com.nenecorp.chatapp.models.DbSocket;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ChatApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
        new DbSocket();
        MuTime.enableDiskCaching(this);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    MuTime.requestTimeFromServer("time.google.com");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
