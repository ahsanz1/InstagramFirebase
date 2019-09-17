package com.example.ahsanz.instagramfirebase;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by ahsanz on 8/19/17.
 */

public class RecyclerAdapterFirOtherInstagramUsersPhotos extends
        RecyclerView.Adapter<RecyclerAdapterFirOtherInstagramUsersPhotos.PhotoHolder>{

    private static ArrayList<Photo> photosList;
    private Context context;
    private static String userID;

    public RecyclerAdapterFirOtherInstagramUsersPhotos(ArrayList<Photo> photos, Context c, String id) {

        photosList = photos;
        context = c;
        userID = id;
    }

    public static class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView userImage;


        PhotoHolder(View v){
            super(v);
            v.setOnClickListener(RecyclerAdapterFirOtherInstagramUsersPhotos.PhotoHolder.this);
            userImage = (ImageView) v.findViewById(R.id.userpostitem);
        }

        @Override
        public void onClick(View view) {

            //Log.i("", "Click Captured on " + String.valueOf(getAdapterPosition()));

            Intent intent = new Intent(userImage.getContext(), ShowPhotoActivity.class);
            intent.putExtra("imageurl", photosList.get(getAdapterPosition()).photoURL)
                    .putExtra("imagecaption", photosList.get(getAdapterPosition()).Caption)
                    .putExtra("totallikes", photosList.get(getAdapterPosition()).totalLikes)
                    .putExtra("photokey", photosList.get(getAdapterPosition()).photoKey)
                    .putExtra("otheruser", 1)
                    .putExtra("userID", userID);

            userImage.getContext().startActivity(intent);
        }
    }

    @Override
    public RecyclerAdapterFirOtherInstagramUsersPhotos.PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);
        return new RecyclerAdapterFirOtherInstagramUsersPhotos.PhotoHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapterFirOtherInstagramUsersPhotos.PhotoHolder holder, int position) {

        Picasso.with(context).load(photosList.get(position).photoURL).fit().into(holder.userImage);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerAdapterFirOtherInstagramUsersPhotos.PhotoHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (holder.userImage.getVisibility() == View.INVISIBLE)
            holder.userImage.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }
}
