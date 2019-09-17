package com.example.ahsanz.instagramfirebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Created by ahsanz on 7/27/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PhotoHolder> {


    private static ArrayList<Photo> photosList;
    private Context context;
    //private static String profilePicUrl;

    public RecyclerAdapter(ArrayList<Photo> photos, Context c) {

        photosList = photos;
        context = c;
    }

    public static class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView userImage;


        PhotoHolder(View v){
            super(v);
            v.setOnClickListener(PhotoHolder.this);
            userImage = (ImageView) v.findViewById(R.id.userpostitem);
        }

        @Override
        public void onClick(View view) {

            Intent intent = new Intent(userImage.getContext(), ShowPhotoActivity.class);
            intent.putExtra("imageurl", photosList.get(getAdapterPosition()).photoURL)
                    .putExtra("imagecaption", photosList.get(getAdapterPosition()).Caption)
                    .putExtra("photokey", photosList.get(getAdapterPosition()).photoKey);

            userImage.getContext().startActivity(intent);
        }
    }


    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);
        return new PhotoHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.PhotoHolder holder, int position) {

        Picasso.with(context).load(photosList.get(position).photoURL).fit().into(holder.userImage);
    }

    @Override
    public void onViewAttachedToWindow(PhotoHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (holder.userImage.getVisibility() == View.INVISIBLE)
            holder.userImage.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }


}
