package com.example.ahsanz.instagramfirebase;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowCommentsActivity extends AppCompatActivity {

    RecyclerView CommentsRecyclerView;
    RecyclerAdapterForShowingComments CommentsRecyclerAdapter;

    String ownerID;
    String photoKey;

    ArrayList<Comment> CommentsList;

    DatabaseReference DBrefForGettingPhotoComments;

    ValueEventListener listenerForGettingPhotoComments;

    TextView NoCommentsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comments);

        String title = "Comments";

        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        CommentsRecyclerView = (RecyclerView) findViewById(R.id.CommentsRecyclerView);
        CommentsRecyclerView.setHasFixedSize(true);
        CommentsRecyclerView.setLayoutManager(new LinearLayoutManager(ShowCommentsActivity.this));

        NoCommentsText = (TextView) findViewById(R.id.nocommentstext);

        try {
            ownerID = getIntent().getStringExtra("userID");
            photoKey = getIntent().getStringExtra("photokey");

            DBrefForGettingPhotoComments = FirebaseDatabase.getInstance().getReference().child("Users").child(ownerID)
                    .child("user_photos").child(photoKey).child("Comments");
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        CommentsList = new ArrayList<>();

        CommentsRecyclerAdapter = new RecyclerAdapterForShowingComments(CommentsList, ShowCommentsActivity.this);

        CommentsRecyclerView.setAdapter(CommentsRecyclerAdapter);

        CreateListeners();

        if (CommentsList.isEmpty()){
            NoCommentsText.setVisibility(View.VISIBLE);
        }else
            NoCommentsText.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        DBrefForGettingPhotoComments.removeEventListener(listenerForGettingPhotoComments);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DBrefForGettingPhotoComments.addValueEventListener(listenerForGettingPhotoComments);

        if (CommentsList.isEmpty()){
            NoCommentsText.setVisibility(View.VISIBLE);
        }

    }

    private void CreateListeners(){

        listenerForGettingPhotoComments = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        CommentsList.add((snapshot.getValue(Comment.class)));

                    }

                    if (!CommentsList.isEmpty() && NoCommentsText.getVisibility() == View.VISIBLE)
                        NoCommentsText.setVisibility(View.INVISIBLE);

                    CommentsRecyclerAdapter.notifyDataSetChanged();
                }catch (Exception e) {
                    Toast.makeText(ShowCommentsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
