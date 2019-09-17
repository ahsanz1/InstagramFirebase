package com.example.ahsanz.instagramfirebase;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class UserProfileActivity extends AppCompatActivity {

    TextView numberOfPosts;
    TextView totalFollowers;
    TextView totalFollowing;
    TextView noPhotosToShow;
    TextView fullName;
    TextView bio;
    CircularImageView userProfileImage;

    Button FollowButton;
    Button EditProfileButton;

    RecyclerView userPostsView;
    RecyclerAdapterFirOtherInstagramUsersPhotos recyclerAdapter;

    FirebaseDatabase PostsDatabase;
    DatabaseReference userImagesReference;
    FirebaseAuth firebaseAuth;
    DatabaseReference userProfilePhotoDatabaseReference;
    DatabaseReference dbRefForUpdatingOtherUserFollowers;
    DatabaseReference dbRefForUpdatingCurrentUserFollowing;


    DataSnapshot datasnapshotForOtherUserFollowers;
    DataSnapshot datasnapshotForCurrentUserFollowing;

    String userID;
    String profileImageUrl;
    String currentUserID;

    ArrayList<Photo> userImages;

    ValueEventListener listenerForProfilePhoto;
    ValueEventListener valueEventListener;
    ValueEventListener listenerForGettingOtherUserFollowers;
    ValueEventListener listenerForUpdatingCurrentUserFollowing;

    long otherUserFollowers = 0, currentUserFollowing = 0;
    boolean followed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        String title = "User Profile";

        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        userProfileImage = (CircularImageView) findViewById(R.id.profileImageCircular);
        noPhotosToShow = (TextView) findViewById(R.id.noPhotos);
        noPhotosToShow.setVisibility(View.INVISIBLE);
        totalFollowers = (TextView) findViewById(R.id.totalfollowers);
        totalFollowing = (TextView) findViewById(R.id.totalfollowing);
        numberOfPosts = (TextView) findViewById(R.id.totalposts);
        fullName = (TextView) findViewById(R.id.usernameView);
        bio = (TextView) findViewById(R.id.bioView);
        EditProfileButton = (Button) findViewById(R.id.editProfileButton);
        EditProfileButton.setVisibility(View.INVISIBLE);
        FollowButton = (Button) findViewById(R.id.followButton);

        try {

            userID = getIntent().getStringExtra("userID");
            fullName.setText(getIntent().getStringExtra("fullName"));
            bio.setText(getIntent().getStringExtra("bio"));

            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!followed)
                        updateFollowers();
                    else
                        removeFollowers();

                }
            });


            firebaseAuth = FirebaseAuth.getInstance();

            userProfilePhotoDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").
                    child(userID);

            PostsDatabase = FirebaseDatabase.getInstance();
            userImagesReference = PostsDatabase.getReference().child("Users").child(userID).child("user_photos");

            dbRefForUpdatingOtherUserFollowers = PostsDatabase.getReference().child("Users").child(userID).child("followers");

            dbRefForUpdatingCurrentUserFollowing = PostsDatabase.getReference().child("Users")
                    .child(currentUserID).child("following");
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        userImages = new ArrayList<>();

        userPostsView = (RecyclerView) findViewById(R.id.recyclerInstaPost);
        userPostsView.setHasFixedSize(true);
        userPostsView.setLayoutManager(new GridLayoutManager(this, 3));

        createListeners();

        userProfilePhotoDatabaseReference.addListenerForSingleValueEvent(listenerForProfilePhoto);
        userImagesReference.addListenerForSingleValueEvent(valueEventListener);
        dbRefForUpdatingOtherUserFollowers.addValueEventListener(listenerForGettingOtherUserFollowers);
        dbRefForUpdatingCurrentUserFollowing.addValueEventListener(listenerForUpdatingCurrentUserFollowing);
    }

    private void removeFollowers() {

        try {

            for (DataSnapshot snapshot : datasnapshotForOtherUserFollowers.getChildren()) {

                if (snapshot.getValue().equals(currentUserID)) {
                    dbRefForUpdatingOtherUserFollowers.child(snapshot.getKey()).removeValue();
                }

            }

            for (DataSnapshot snapshot : datasnapshotForCurrentUserFollowing.getChildren()) {

                if (snapshot.getValue().equals(userID)) {
                    dbRefForUpdatingCurrentUserFollowing.child(snapshot.getKey()).removeValue();
                }

            }


            FollowButton.setText("Follow");
            FollowButton.setTextColor(Color.BLACK);
            FollowButton.setBackgroundResource(R.drawable.follow_button);
            followed = false;
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateFollowers() {

        try {

            dbRefForUpdatingOtherUserFollowers.push().setValue(currentUserID);

            dbRefForUpdatingCurrentUserFollowing.push().setValue(userID)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                FollowButton.setText("following");
                                FollowButton.setTextColor(Color.BLACK);
                                FollowButton.setBackgroundResource(R.drawable.following_button);
                            }
                        }
                    });

            followed = true;
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        userImagesReference.removeEventListener(valueEventListener);
        dbRefForUpdatingOtherUserFollowers.removeEventListener(listenerForGettingOtherUserFollowers);
        dbRefForUpdatingCurrentUserFollowing.removeEventListener(listenerForUpdatingCurrentUserFollowing);
        userImagesReference.removeEventListener(valueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        userImagesReference.addListenerForSingleValueEvent(valueEventListener);
        dbRefForUpdatingOtherUserFollowers.addValueEventListener(listenerForGettingOtherUserFollowers);
        dbRefForUpdatingCurrentUserFollowing.addValueEventListener(listenerForUpdatingCurrentUserFollowing);
        userImagesReference.addListenerForSingleValueEvent(valueEventListener);

    }

    public void attachAdapter() {

        if (userImages.isEmpty()) {
            noPhotosToShow.setVisibility(View.VISIBLE);
        } else {

            recyclerAdapter = new RecyclerAdapterFirOtherInstagramUsersPhotos(userImages, this, userID);

            userPostsView.setAdapter(recyclerAdapter);

            if (noPhotosToShow.getVisibility() == View.VISIBLE)
                noPhotosToShow.setVisibility(View.INVISIBLE);
        }
    }

    private void createListeners(){

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean contains = false;

                numberOfPosts.setText(String.valueOf(dataSnapshot.getChildrenCount()));

                try {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        for (int i = 0; i < userImages.size(); i++) {
                            if ((userImages.get(i).photoURL.equals(snapshot.getValue(Photo.class).photoURL))) {
                                contains = true;
                                break;
                            }
                        }

                        if (!contains) {
                            userImages.add(snapshot.getValue(Photo.class));
                        }
                        contains = false;
                    }
                }catch (Exception e){
                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                if (recyclerAdapter == null)
                    attachAdapter();
                else
                    recyclerAdapter.notifyDataSetChanged();
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerForProfilePhoto = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    HashMap hashMap = (HashMap) dataSnapshot.getValue();

                    profileImageUrl = (String) hashMap.get("imageurl");

                    if (userProfileImage.getDrawable() == null){
                        Picasso.with(UserProfileActivity.this).load(profileImageUrl).fit().into(userProfileImage);
                    }

                } catch (Exception e) {
                    Toast.makeText(UserProfileActivity.this, "Couldn't Fetch Profile Pic", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerForGettingOtherUserFollowers = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                otherUserFollowers = dataSnapshot.getChildrenCount();

                datasnapshotForOtherUserFollowers = dataSnapshot;

                if (otherUserFollowers > 0) {

                    try {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            if (snapshot.getValue().equals(currentUserID)) {
                                followed = true;
                                break;
                            }

                        }

                        if (followed) {
                            FollowButton.setText("following");
                            FollowButton.setTextColor(Color.BLACK);
                            FollowButton.setBackgroundResource(R.drawable.following_button);

                        }
                    }catch (Exception e){
                        Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerForUpdatingCurrentUserFollowing = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentUserFollowing = dataSnapshot.getChildrenCount();

                datasnapshotForCurrentUserFollowing = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
