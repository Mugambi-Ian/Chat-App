package com.nenecorp.chatapp.userinterface;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nenecorp.chatapp.R;
import com.nenecorp.chatapp.models.DbHandler;
import com.nenecorp.chatapp.models.DbSocket;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {
    private View lytLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        DbHandler dbHandler = DbSocket.dbHandler;
        lytLogo = findViewById(R.id.ASS_lytLogo);
        YoYo.with(Techniques.FadeIn)
                .repeat(0)
                .duration(200)
                .playOn(lytLogo);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shakeItUp();
                    }
                });
            }
        }, 250);
    }

    private void shakeItUp() {
        YoYo.with(Techniques.RubberBand)
                .duration(350)
                .repeat(0)
                .playOn(lytLogo);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startApplication();
                    }
                });
            }
        }, 350);
    }

    private void startApplication() {
        if (!DbHandler.userExists) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }
}
