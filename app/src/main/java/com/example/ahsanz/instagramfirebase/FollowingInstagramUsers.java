package com.example.ahsanz.instagramfirebase;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FollowingInstagramUsers extends Fragment {

    RecyclerAdapterForInstagramUsers recyclerAdapterForInstagramUsers;
    RecyclerView OtherUsersRecyclerView;

    DatabaseReference usersDatabaseRef;

    ValueEventListener usersValueEventListener;

    ArrayList<User> usersArrayList;
    String currentUserId;

    public FollowingInstagramUsers() {
        // Required empty public constructor
    }


    public static FollowingInstagramUsers newInstance() {
        FollowingInstagramUsers fragment = new FollowingInstagramUsers();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_following_instagram_users, container, false);

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        OtherUsersRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.otherusersrecyclerview);
        OtherUsersRecyclerView.setHasFixedSize(true);
        OtherUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersArrayList = new ArrayList<>();

        recyclerAdapterForInstagramUsers = new RecyclerAdapterForInstagramUsers(usersArrayList, getContext());

        createListenerForUsers();

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        usersDatabaseRef.addListenerForSingleValueEvent(usersValueEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        usersDatabaseRef.removeEventListener(usersValueEventListener);
    }

    private void createListenerForUsers(){

        usersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    boolean contains = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            for (int i = 0; i < usersArrayList.size(); i++) {
                                if ((usersArrayList.get(i).userID.equals(snapshot.getValue(User.class).userID))) {
                                    contains = true;
                                    break;
                                }
                            }

                        if (!snapshot.getValue(User.class).userID.equals(currentUserId) && !contains)
                            usersArrayList.add(snapshot.getValue(User.class));

                        contains = false;
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }


                if (OtherUsersRecyclerView.getAdapter() == null)
                    OtherUsersRecyclerView.setAdapter(recyclerAdapterForInstagramUsers);
                else
                    recyclerAdapterForInstagramUsers.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

}
