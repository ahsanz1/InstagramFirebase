package com.example.ahsanz.instagramfirebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

public class ShowPhotoActivity extends AppCompatActivity implements View.OnClickListener {

    TextView captionText;
    TextView userNameTextView;
    TextView likesText;

    ImageView cornerProfileImage;
    ImageView userImage;
    ImageView likeIcon;
    ImageView commentIcon;
    ImageView addCommentIcon;

    FirebaseAuth mFirebaseAuth;

    String cText;
    String photoKey;
    String profileImageUrl = null;
    String userName;
    String keyGeneratedAfterLiked;
    String postImageUrl;
    String userID;
    String keyOfPersonWhoLikedThisPhoto;
    ArrayList<Map.Entry<String, String>> listOfPeopleWhoLikeThisPhoto;
    String currPhotoLikes;

    ValueEventListener valueEventListenerForLikesByUser;
    ValueEventListener listenerForTotalLikes;
    ValueEventListener valueEventListener;

    DatabaseReference userPhotosReferenceForUpdatingLikes;
    DatabaseReference photosLikedByUserReference;
    DatabaseReference mDatabaseReference;
    DatabaseReference mProfilePicReference;

    int totalNumberOfLikes;
    boolean liked = false;

    HashMap hashMapForListOfPeopleWhoLikeThisPhoto;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.likeIcon) {

            if (liked == false) {
                likeIcon.setImageResource(R.drawable.heart);
                liked = true;

                ++totalNumberOfLikes;
                userPhotosReferenceForUpdatingLikes.child("totalLikes").setValue(totalNumberOfLikes);
                keyOfPersonWhoLikedThisPhoto = userPhotosReferenceForUpdatingLikes
                        .child("PeopleWhoLikeThis").push().getKey();
                userPhotosReferenceForUpdatingLikes.child("PeopleWhoLikeThis").child(keyOfPersonWhoLikedThisPhoto)
                        .setValue(mFirebaseAuth.getCurrentUser().getUid());

                keyGeneratedAfterLiked = photosLikedByUserReference.push().getKey();
                photosLikedByUserReference.child(keyGeneratedAfterLiked).setValue(postImageUrl);

            } else {
                likeIcon.setImageResource(R.drawable.whiteheart);
                liked = false;

                --totalNumberOfLikes;
                userPhotosReferenceForUpdatingLikes.child("totalLikes").setValue(totalNumberOfLikes);


                for (int i = 0; i< listOfPeopleWhoLikeThisPhoto.size(); i++) {

                    if (listOfPeopleWhoLikeThisPhoto.get(i).getValue()
                            .equals(mFirebaseAuth.getCurrentUser().getUid())) {

                        userPhotosReferenceForUpdatingLikes.child("PeopleWhoLikeThis")
                                .child(listOfPeopleWhoLikeThisPhoto.get(i).getKey()).removeValue();
                        photosLikedByUserReference.child(keyGeneratedAfterLiked).removeValue();
                        break;
                    }

                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        String title = "Photo";

        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        captionText = (TextView) findViewById(R.id.ShowCaptionText);
        mFirebaseAuth = FirebaseAuth.getInstance();
        userNameTextView = (TextView) findViewById(R.id.usernametext);
        likesText = (TextView) findViewById(R.id.likesTextView);
        commentIcon = (ImageView) findViewById(R.id.messageIcon);
        addCommentIcon = (ImageView) findViewById(R.id.addCommentIcon);

        //try {

            photoKey = getIntent().getStringExtra("photokey");

            postImageUrl = getIntent().getStringExtra("imageurl");

            userID = getIntent().getStringExtra("userID");

            currPhotoLikes = getIntent().getStringExtra("totallikes");
        if (currPhotoLikes == null)
            currPhotoLikes = "0";

        cText = getIntent().getStringExtra("imagecaption");

        Log.i("pKey ", photoKey);

            likesText.setText(currPhotoLikes + " Likes");
        /*}catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }*/

        addCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowPhotoActivity.this, CommentActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("photokey", photoKey);
                startActivity(intent);
            }
        });

        commentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ShowPhotoActivity.this, ShowCommentsActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("photokey", photoKey);
                startActivity(intent);
            }
        });

        try {

            if (getIntent().getIntExtra("otheruser", 0) == 1) {
                mProfilePicReference = FirebaseDatabase.getInstance().getReference().child("Users").
                        child(userID);

                userPhotosReferenceForUpdatingLikes = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(userID).child("user_photos")
                        .child(photoKey);
            } else {
                mProfilePicReference = FirebaseDatabase.getInstance().getReference().child("Users").
                        child(mFirebaseAuth.getCurrentUser().getUid());

                userPhotosReferenceForUpdatingLikes = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(mFirebaseAuth.getCurrentUser().getUid()).child("user_photos")
                        .child(photoKey);
            }

            photosLikedByUserReference = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mFirebaseAuth.getCurrentUser().getUid()).child("liked_photos");
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        CreateListeners();

        userImage = (ImageView) findViewById(R.id.postImage);
        cornerProfileImage = (ImageView) findViewById(R.id.cardProfileImage);
        likeIcon = (ImageView) findViewById(R.id.likeIcon);
        likeIcon.setOnClickListener(this);

        Picasso.with(this).load(postImageUrl).fit().into(userImage);
        captionText.setText(cText);

        mProfilePicReference.addListenerForSingleValueEvent(valueEventListener);
        photosLikedByUserReference.addListenerForSingleValueEvent(valueEventListenerForLikesByUser);
        userPhotosReferenceForUpdatingLikes.addValueEventListener(listenerForTotalLikes);
    }


    public void CreateListeners() {

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();

                    profileImageUrl = (String) hashMap.get("imageurl");
                    userName = (String) hashMap.get("userName");
                    userNameTextView.setText(userName);

                    if (profileImageUrl == null) {
                        Toast.makeText(ShowPhotoActivity.this, "Couldn't Load Profile Pic!", Toast.LENGTH_SHORT).show();
                    } else {

                        Picasso.with(ShowPhotoActivity.this).load(profileImageUrl).fit().into(cornerProfileImage);
                    }


                } catch (Exception e) {
                    Toast.makeText(ShowPhotoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        valueEventListenerForLikesByUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (snapshot.getValue().equals(postImageUrl)) {

                            keyGeneratedAfterLiked = snapshot.getKey().toString();
                            likeIcon.setImageResource(R.drawable.heart);
                            liked = true;
                            break;
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(ShowPhotoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerForTotalLikes = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                try {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    hashMapForListOfPeopleWhoLikeThisPhoto = (HashMap) hashMap.get("PeopleWhoLikeThis");
                    Set<Map.Entry<String, String>> entrySet = hashMapForListOfPeopleWhoLikeThisPhoto.entrySet();

                    listOfPeopleWhoLikeThisPhoto = new ArrayList<Map.Entry<String, String>>(entrySet);
                    Long l = (Long) hashMap.get("totalLikes");

                    totalNumberOfLikes = Integer.parseInt(String.valueOf(l));

                    likesText.setText(String.valueOf(totalNumberOfLikes) + " Likes");

                }
                catch (Exception e){

                    if (!e.getMessage().contains("entrySet"))
                            Toast.makeText(ShowPhotoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }
}
