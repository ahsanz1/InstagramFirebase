package com.example.ahsanz.instagramfirebase;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerAdapterForInstagramUsers extends RecyclerView.Adapter<RecyclerAdapterForInstagramUsers.UserHolder> {

    static ArrayList<User> Users;
    Context context;

    RecyclerAdapterForInstagramUsers(ArrayList<User> U, Context c){

        Users = U;
        context = c;
    }


    public static class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        TextView fullname;
        TextView username;
        CircularImageView profilePic;

        UserHolder(View view){
            super(view);
            view.setOnClickListener(this);

            fullname = (TextView) view.findViewById(R.id.instauserfullname);
            username = (TextView) view.findViewById(R.id.instauserUsername);
            profilePic = (CircularImageView) view.findViewById(R.id.instaProfileImageCircular);

        }

        @Override
        public void onClick(View view) {

            Intent intent = new Intent(view.getContext(), UserProfileActivity.class);

            intent.putExtra("userName", Users.get(getAdapterPosition()).userName)
                    .putExtra("bio", Users.get(getAdapterPosition()).bio)
                    .putExtra("fullName", Users.get(getAdapterPosition()).fullName)
                    .putExtra("imageurl", Users.get(getAdapterPosition()).imageurl)
                    .putExtra("userID", Users.get(getAdapterPosition()).userID);


            view.getContext().startActivity(intent);

        }
    }


    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_adapter_for_insta_users, parent, false);

        return new UserHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {

        Picasso.with(context).load(Users.get(position).imageurl).fit().into(holder.profilePic);
        holder.fullname.setText(Users.get(position).fullName);
        holder.username.setText(Users.get(position).userName);

    }

    @Override
    public int getItemCount() {

        return Users.size();
    }
}
