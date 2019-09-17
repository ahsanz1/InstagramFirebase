package com.example.ahsanz.instagramfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    TextView userNameText;
    TextView passwordText;
    TextView confirmPasswordText;
    TextView Bio;
    TextView emailText;
    TextView phonNumber;
    TextView FullName;

    Button signUpButton;

    String userID;
    String userProfilePicUrl;

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    StorageReference storageReference;
    StorageReference mStorageRef;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    RelativeLayout SignUpLayout;
    CircularImageView profilePicture;

    ProgressBar progressBar;
    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;
    Uri imageHoldUri = null;

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.signupButton) {
            SignUpLayout.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            SignUpUser();

        } else if (view.getId() == R.id.profilePic) {
            imagePicker();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        userNameText = (TextView) findViewById(R.id.username);
        passwordText = (TextView) findViewById(R.id.password);
        confirmPasswordText = (TextView) findViewById(R.id.confirmpassword);
        emailText = (TextView) findViewById(R.id.email);
        Bio = (TextView) findViewById(R.id.bio);
        phonNumber = (TextView) findViewById(R.id.phonenumber);
        FullName = (TextView) findViewById(R.id.fullname);


        signUpButton = (Button) findViewById(R.id.signupButton);
        signUpButton.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        SignUpLayout = (RelativeLayout) findViewById(R.id.signuplayout);
        profilePicture = (CircularImageView) findViewById(R.id.profilePic);
        profilePicture.setImageResource(R.drawable.facebook_avatar);

        profilePicture.setOnClickListener(this);

        if (SignUpLayout.getVisibility() == View.INVISIBLE) {
            SignUpLayout.setVisibility(View.VISIBLE);
        }


        firebaseDatabase = FirebaseDatabase.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {

                    Intent intent = new Intent(getApplicationContext(), GramHomeActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void imagePicker() {

        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setTitle("Add Photo!");

        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void cameraIntent() {

        //CHOOSE CAMERA
        Log.d("gola", "entered here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {

        //CHOOSE IMAGE FROM GALLERY
        Log.d("gola", "entered here");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //SAVE URI FROM GALLERY
        if(requestCode == SELECT_FILE && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }else if ( requestCode == REQUEST_CAMERA && resultCode == RESULT_OK ){
            //SAVE URI FROM CAMERA

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }


        //image crop library code
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageHoldUri = result.getUri();
                userProfilePicUrl = imageHoldUri.toString();
                profilePicture.setImageURI(imageHoldUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void SignUpUser() {

        final String userName = userNameText.getText().toString().trim();
        final String email = emailText.getText().toString().trim();
        final String bio = Bio.getText().toString().trim();
        final String password = passwordText.getText().toString().trim();
        final String phone = phonNumber.getText().toString().trim();
        final String fullname = FullName.getText().toString().trim();
        String confirmPassword = confirmPasswordText.getText().toString().trim();


        if (!password.isEmpty() && !userName.isEmpty() && !confirmPassword.isEmpty() && !email.isEmpty()) {

            if (password.equals(confirmPassword)) {

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            databaseReference = firebaseDatabase.getReference().child("Users")
                                    .child(firebaseAuth.getCurrentUser().getUid());

                            //could have made a class of USER

                            final User newUser = new User(userName, email, bio, password, phone, fullname, userID);

                            databaseReference.setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                                Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_LONG).show();
                                            else
                                                Toast.makeText(SignUpActivity.this, "Couldn't SignUp User", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            if (imageHoldUri != null) {
                                userID = firebaseAuth.getCurrentUser().getUid().toString();
                                mStorageRef = storageReference.child(userID).child("User_Profile_Photo");
                                String profilePicUrl = imageHoldUri.getLastPathSegment();

                                mStorageRef.putFile(imageHoldUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Uri imageUri = taskSnapshot.getDownloadUrl();
                                        databaseReference.child("imageurl").setValue(imageUri.toString());
                                        newUser.setUserProfilePicUrl(imageUri.toString());
                                    }
                                });
                            }

                            Intent intent = new Intent(getApplicationContext(), GramHomeActivity.class);
                            progressBar.setVisibility(View.INVISIBLE);
                            startActivity(intent);


                        } else {
                            Toast.makeText(SignUpActivity.this, "\t\t\t\t\t\t\tSignUp Failed!\n" +
                                    task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            SignUpLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        } else {
            Toast.makeText(this, "One of the text fields is empty!", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
            SignUpLayout.setVisibility(View.VISIBLE);
        }
    }
}



