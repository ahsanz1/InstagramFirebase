package com.example.ahsanz.instagramfirebase;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {

    EditText editUsername;
    EditText editEmail;
    EditText editPhone;
    EditText editBio;
    EditText editFullName;

    ImageView saveChangesImageView;

    CircularImageView editProfileImage;

    DatabaseReference currentUserDBref;
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        String title = "Edit Profile";

        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        editProfileImage = (CircularImageView) findViewById(R.id.editProfileImageCircular);

        editUsername = (EditText) findViewById(R.id.editUsername);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPhone = (EditText) findViewById(R.id.editPhone);
        editBio = (EditText) findViewById(R.id.editBio);
        editFullName = (EditText) findViewById(R.id.editFullName);

        saveChangesImageView = (ImageView) findViewById(R.id.saveChanges);
        saveChangesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInfo();
            }
        });

        try {

            firebaseAuth = FirebaseAuth.getInstance();
            currentUserDBref = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(firebaseAuth.getCurrentUser().getUid());

            Picasso.with(this).load(getIntent().getStringExtra("profileimage")).into(editProfileImage);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void updateUserInfo(){

        final String userName = editUsername.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String bio = editBio.getText().toString().trim();
        final String phone = editPhone.getText().toString().trim();
        final String fullName = editFullName.getText().toString().trim();

        //could have made a class of USER

        if (!userName.isEmpty())
            currentUserDBref.child("userName").setValue(userName);

        if (!email.isEmpty())
            currentUserDBref.child("emailAddress").setValue(email);

        if (!bio.isEmpty())
            currentUserDBref.child("bio").setValue(bio);

        if (!phone.isEmpty())
            currentUserDBref.child("phoneNo").setValue(phone);

        if (!phone.isEmpty())
            currentUserDBref.child("fullName").setValue(fullName);
    }
}
