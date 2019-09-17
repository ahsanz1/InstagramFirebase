package com.example.ahsanz.instagramfirebase;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PopularPosts extends Fragment {

    RecyclerView UserFeedRecyclerView;
    RecyclerAdapterForUserFeed recyclerAdapterForUserFeed;

    ArrayList<Photo> photosArrayList;

    String userID;
    String userKey;

    DatabaseReference userFollowingRef;
    DatabaseReference otherUserPhotosRef;

    ValueEventListener listenerForFollowing;
    ValueEventListener listenerForRetrievingPosts;

    LinearLayoutManager layoutManager;

    public PopularPosts() {
        // Required empty public constructor
    }


    public static PopularPosts newInstance() {
        PopularPosts fragment = new PopularPosts();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popular_posts, container, false);

        layoutManager = new LinearLayoutManager(getContext());


        UserFeedRecyclerView = (RecyclerView) view.findViewById(R.id.userFeedRecyclerView);
        UserFeedRecyclerView.setHasFixedSize(true);
        UserFeedRecyclerView.setLayoutManager(layoutManager);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userFollowingRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userID).child("following");
        otherUserPhotosRef = FirebaseDatabase.getInstance().getReference().child("Users");

        photosArrayList = new ArrayList<>();

        recyclerAdapterForUserFeed = new RecyclerAdapterForUserFeed(photosArrayList, getContext());

        UserFeedRecyclerView.setAdapter(recyclerAdapterForUserFeed);

        CreateListeners();

        Log.i("CREATED", "");


        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        //userFollowingRef.removeEventListener(listenerForFollowing);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("CREATED", "");
        userFollowingRef.addValueEventListener(listenerForFollowing);

        try {

            otherUserPhotosRef.child(userKey).child("user_photos")
                    .addValueEventListener(listenerForRetrievingPosts);
        }catch (Exception e){}

    }

    private void CreateListeners() {

        listenerForFollowing = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("CREATED", "");
                try {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        userKey = (String) snapshot.getValue();

                        if (listenerForRetrievingPosts != null) {
                            otherUserPhotosRef.child(userKey).child("user_photos")
                                    .addValueEventListener(listenerForRetrievingPosts);
                        }

                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerForRetrievingPosts = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //try {

                    //boolean contains = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Log.i("INTO SNAPSJOT", "");

                        /*for (int i = 0; i < photosArrayList.size(); i++) {
                            if ((photosArrayList.get(i).photoURL.equals(snapshot.getValue(Photo.class).photoURL))) {
                                contains = true;
                                break;
                            }
                        }

                        //if (!contains) {
                          //  Log.i("ADDED", "");*/
                            photosArrayList.add(snapshot.getValue(Photo.class));
                        //}
                        //contains = false;
                    //}
                    //recyclerAdapterForUserFeed.notifyDataSetChanged();
                //}}catch (Exception e){
                   // Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                //}

                recyclerAdapterForUserFeed.notifyDataSetChanged();

            }}

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }
}
