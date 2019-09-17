package com.example.ahsanz.instagramfirebase;

import android.content.Context;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;


public class RecyclerAdapterForUserFeed extends RecyclerView.Adapter<RecyclerAdapterForUserFeed.PostHolder> {

    static FirebaseAuth mFirebaseAuth;
    private static ArrayList<Photo> PostsList;
    static Context context;

    static String currPhotoUrl;
    static String l = "  Likes";
    static String currPhotoKey;
    static String currPhotoOwner;
    static String currPhotoCaption;
    static long currPhotoLikes;




    DatabaseReference DBrefForCheckingIfCurrentUserLikesThisPhoto;
    DatabaseReference DBrefForGettingOtherPhotoUserName;


    RecyclerAdapterForUserFeed(ArrayList<Photo> list, Context c) {

        PostsList = list;
        context = c;

        try {
            DBrefForCheckingIfCurrentUserLikesThisPhoto = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child("liked_photos");

            DBrefForGettingOtherPhotoUserName = FirebaseDatabase.getInstance().getReference().child("Users");
        }catch (Exception e){
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
    }


    public static class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView postImage;
        ImageView likeIcon;
        ImageView commentIcon;
        ImageView cornerProfileImage;

        TextView userNameText;
        TextView captionText;
        TextView likesText;

        public PostHolder(View itemView) {
            super(itemView);

            postImage = (ImageView) itemView.findViewById(R.id.postImage);
            postImage.setOnClickListener(this);
            commentIcon = (ImageView) itemView.findViewById(R.id.addCommentIcon);
            commentIcon.setOnClickListener(this);
            cornerProfileImage = (CircularImageView) itemView.findViewById(R.id.cardProfileImage);

            userNameText = (TextView) itemView.findViewById(R.id.usernametext);
            captionText = (TextView) itemView.findViewById(R.id.ShowCaptionText);
            likesText = (TextView) itemView.findViewById(R.id.likesTextView);
        }

        @Override
        public void onClick(View view) {

            /*(if (view.getId() == R.id.postImage){

                Intent intent = new Intent(context, ShowPhotoActivity.class);
                intent.putExtra("photokey", currPhotoKey);
                intent.putExtra("imageurl", currPhotoUrl);
                intent.putExtra("userID", currPhotoOwner);
                intent.putExtra("imagecaption", currPhotoCaption);
                intent.putExtra("otheruser", 1);
                intent.putExtra("totallikes", String.valueOf(currPhotoLikes));

                context.startActivity(intent);

            }else*/ if (view.getId() == R.id.addCommentIcon){

                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("photokey", currPhotoKey);
                intent.putExtra("userID", currPhotoOwner);

                context.startActivity(intent);
            }

        }

    }

    @Override
    public RecyclerAdapterForUserFeed.PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_feed_recycler_row, parent, false);

        return new PostHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(final PostHolder holder, int position) {

        Picasso.with(context).load(PostsList.get(position).photoURL).fit().into(holder.postImage);
        holder.likesText.setText(String.valueOf(PostsList.get(position).totalLikes) + l);

        currPhotoUrl = PostsList.get(position).photoURL;
        currPhotoKey = PostsList.get(position).photoKey;
        currPhotoOwner = PostsList.get(position).ownerID;
        currPhotoCaption = PostsList.get(position).Caption;
        currPhotoLikes = PostsList.get(position).totalLikes;

        Log.i("POSITION " + position, "ADAPTER " + holder.getAdapterPosition());

        try{notifyDataSetChanged();}
        catch (Exception e){}

        final int x = position;

        DBrefForCheckingIfCurrentUserLikesThisPhoto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (snapshot.getValue().equals(currPhotoUrl)) {

                            holder.likeIcon.setImageResource(R.drawable.heart);
                            break;
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {

            DBrefForGettingOtherPhotoUserName.child(currPhotoOwner).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    HashMap hashMap = (HashMap) dataSnapshot.getValue();

                    Picasso.with(context).load(hashMap.get("imageurl").toString()).fit().into(holder.cornerProfileImage);

                    holder.userNameText.setText(hashMap.get("userName").toString());
                    holder.captionText.setText(PostsList.get(x).Caption);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return PostsList.size();
    }
}

