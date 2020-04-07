package com.nenecorp.chatapp.userinterface;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.nenecorp.chatapp.R;

public class LoginActivity extends AppCompatActivity {
    private EditText uName, uPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        uName = findViewById(R.id.AL_userName);
        uPassword = findViewById(R.id.AL_password);
    }

    public void signUp(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }

    public void login(final View view) {
        if (uName.getText().length() == 0) {
            uName.setError("You missed!");
        } else if (uPassword.getText().length() == 0) {
            uPassword.setError("You missed!");
        } else {
            String email = uName.getText().toString() + "@capp.com";
            email = email.toLowerCase();
            String pass = uPassword.getText().toString();
            pass = pass + pass;
            findViewById(R.id.AL_pBar).setVisibility(View.VISIBLE);
            view.setVisibility(View.INVISIBLE);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    }
                    view.setVisibility(View.VISIBLE);
                    findViewById(R.id.AL_pBar).setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
