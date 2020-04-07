package com.nenecorp.chatapp.userinterface;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nenecorp.chatapp.R;
import com.nenecorp.chatapp.assets.dialogs.SelectImageDialog;
import com.nenecorp.chatapp.assets.resources.Constants;
import com.nenecorp.chatapp.models.user.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private static final int CAMERA = 101, GALLERY = 202;
    private String userDp, userName, userId, userMail;
    private EditText fName, lName, uName, cPin, ePin;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        fName = findViewById(R.id.ANR_edFname);
        lName = findViewById(R.id.ANR_edLname);
        uName = findViewById(R.id.ANR_edUname);
        cPin = findViewById(R.id.ANR_edCpin);
        ePin = findViewById(R.id.ANR_edEpin);
        requestMultiplePermissions();
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            //   Toast.makeText(ActivityChildInfo.this, "Enable app permissions on the settings menu", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Log.i(error.name(), error.toString());
                    }
                })
                .onSameThread()
                .check();
    }

    public void selectPictureDialog(View view) {
        new SelectImageDialog(this, R.style.DialogBox, new SelectImageDialog.ImageInterface() {
            @Override
            public void result(boolean c) {
                if (c) {
                    takePhotoFromCamera();
                } else {
                    choosePhotoFromGallery();
                }
            }
        });
    }

    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentURI);
                    bitmap.setHasAlpha(true);
                    userDp = String.valueOf(getImageUri(bitmap));
                    CircleImageView dp = findViewById(R.id.ANR_imgDp);
                    findViewById(R.id.ANR_btnAddPhoto).setVisibility(View.GONE);
                    Glide.with(dp).load(userDp).into(dp);
                    dp.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            bitmap.setHasAlpha(true);
            userDp = String.valueOf(getImageUri(bitmap));
            CircleImageView dp = findViewById(R.id.ANR_imgDp);
            findViewById(R.id.ANR_btnAddPhoto).setVisibility(View.GONE);
            Glide.with(dp).load(userDp).into(dp);
            dp.setVisibility(View.VISIBLE);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Uri getImageUri(Bitmap inImage) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(options, inImage.getWidth(), inImage.getHeight());
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(inImage, inImage.getWidth(), inImage.getHeight(), true);
            File file = new File(getFilesDir(), "My Kids" + new Random().nextInt() + ".png");
            FileOutputStream out = openFileOutput(file.getName(), Context.MODE_PRIVATE);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return uri;

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            boolean c = ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth);
            while (c) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void createAccount(View view) {
        if (fName.getText().length() == 0) {
            fName.setError("You missed!");
        } else if (lName.getText().length() == 0) {
            lName.setError("You missed!");
        } else if (uName.getText().length() == 0) {
            uName.setError("You missed!");
        } else if (ePin.getText().length() == 0) {
            ePin.setError("You missed!");
        } else if (cPin.getText().length() == 0) {
            cPin.setError("You missed!");
        } else if (!ePin.getText().toString().equals(cPin.getText().toString())) {
            cPin.setError("Pin doesn't match!");
        } else if (!isValid(uName.getText().toString())) {
            uName.setError("Try something else");
        } else {
            userMail = uName.getText().toString() + "@capp.com";
            userName = fName.getText().toString() + "-" + lName.getText().toString();
            userId = uName.getText().toString();
            findViewById(R.id.ANR_btnTxt).setVisibility(View.INVISIBLE);
            findViewById(R.id.ANR_pBar).setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child(Constants.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(userId)) {
                        uName.setError("Try something else!");
                        findViewById(R.id.ANR_btnTxt).setVisibility(View.VISIBLE);
                        findViewById(R.id.ANR_pBar).setVisibility(View.INVISIBLE);
                    } else {
                        createUser();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void createUser() {
        if (userDp != null) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            final StorageReference image = storageReference.child(Constants.USERS).child(userId);
            final UploadTask uploadTask = image.putFile(Uri.parse(userDp));
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return image.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        saveUserD(task.getResult().toString());
                    }
                }
            });
        } else {
            saveUserD("");
        }
    }

    private void saveUserD(String dp) {
        User u = new User(userId);
        u.userName = userName.toLowerCase();
        u.userDp = dp;
        u.userLang = "en";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(userId);
        u.saveDetails(ref);
        String password = cPin.getText().toString();
        password = password + "" + password;
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(userMail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    findViewById(R.id.ANR_btnTxt).setVisibility(View.VISIBLE);
                    findViewById(R.id.ANR_pBar).setVisibility(View.INVISIBLE);
                    startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Log.i("SignUp", task.getException().getMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                findViewById(R.id.ANR_btnTxt).setVisibility(View.VISIBLE);
                findViewById(R.id.ANR_pBar).setVisibility(View.INVISIBLE);
                Toast.makeText(SignUpActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isValid(String email) {
        email = email + "@capp.com";
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}
