package com.example.ahsanz.instagramfirebase;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

import static java.security.AccessController.getContext;

public class ShowProFilePic extends AppCompatActivity {


    ImageView imageView;
    Uri uri;
    DatabaseReference userProfilePhotoDatabaseReference;
    ValueEventListener listenerForProfilePhoto;
    String profileImageUrl;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pro_file_pic);

        imageView = (ImageView) findViewById(R.id.showProfilePicImageView);
        firebaseAuth = FirebaseAuth.getInstance();

        userProfilePhotoDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").
                child(firebaseAuth.getCurrentUser().getUid());

        listener();

        userProfilePhotoDatabaseReference.addListenerForSingleValueEvent(listenerForProfilePhoto);

    }

    private void listener(){
        listenerForProfilePhoto = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    HashMap hashMap = (HashMap) dataSnapshot.getValue();

                    profileImageUrl = (String) hashMap.get("imageurl");

                    Picasso.with(ShowProFilePic.this).load(profileImageUrl).into(imageView);


                } catch (Exception e) {
                    Toast.makeText(ShowProFilePic.this, "Couldn't Fetch Profile Pic", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
