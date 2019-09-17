package com.example.ahsanz.instagramfirebase;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener{


    EditText CommentText;
    ImageButton DoneCommentButton;
    String ownerID;
    String photoKey;
    String currUserName;
    String currUserDpUrl;

    DatabaseReference commentsDBref;
    DatabaseReference commenterUnameDBref;
    DatabaseReference DBrefForGettingAllComments;
    DatabaseReference DBrefForGettingCurrentUserDPurl;

    ValueEventListener listenerForGettingUname;
    ValueEventListener listenerForGettingCurrentUserDPurl;

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.doneComment){

            if (!String.valueOf(CommentText.getText()).equals("")) {

                try {
                    commentsDBref.push().setValue(new Comment(String.valueOf(CommentText.getText()), currUserName, currUserDpUrl))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(CommentActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(CommentActivity.this, "Comment Failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }else
                Toast.makeText(this, "Please Enter Some Text", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        String title = "Add Comment";

        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        CommentText = (EditText) findViewById(R.id.commenText);
        DoneCommentButton = (ImageButton) findViewById(R.id.doneComment);
        DoneCommentButton.setOnClickListener(this);
        CommentText.setText(null);

        try {
            ownerID = getIntent().getStringExtra("userID");
            photoKey = getIntent().getStringExtra("photokey");

            Log.i("PHOTO KEY " + photoKey, "OWNER ID " + ownerID);

            commentsDBref = FirebaseDatabase.getInstance().getReference().child("Users").child(ownerID).child("user_photos")
                    .child(photoKey).child("Comments");
            commenterUnameDBref = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("userName");
            DBrefForGettingAllComments = FirebaseDatabase.getInstance().getReference().child("Users").child(ownerID)
                    .child(photoKey).child("Comments");

            DBrefForGettingCurrentUserDPurl = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("imageurl");
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        CreateListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        commenterUnameDBref.removeEventListener(listenerForGettingUname);
        DBrefForGettingCurrentUserDPurl.removeEventListener(listenerForGettingCurrentUserDPurl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        commenterUnameDBref.addListenerForSingleValueEvent(listenerForGettingUname);
        DBrefForGettingCurrentUserDPurl.addListenerForSingleValueEvent(listenerForGettingCurrentUserDPurl);
    }

    private void CreateListeners(){

        listenerForGettingUname = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currUserName = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerForGettingCurrentUserDPurl = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currUserDpUrl = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }
}
