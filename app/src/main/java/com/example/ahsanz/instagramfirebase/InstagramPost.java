package com.example.ahsanz.instagramfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alexzh.circleimageview.CircleImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

public class InstagramPost extends Fragment {

    TextView numberOfPosts;
    TextView totalFollowers;
    TextView totalFollowing;
    TextView noPhotosToShow;
    TextView Name;
    TextView BIO;
    CircularImageView userProfileImage;

    RecyclerView userPostsView;
    RecyclerAdapter recyclerAdapter;

    FirebaseDatabase PostsDatabase;
    DatabaseReference userImagesReference;
    FirebaseAuth firebaseAuth;
    StorageReference mUserProfilePhotoStorageReference;
    DatabaseReference userProfilePhotoDatabaseReference;
    DatabaseReference userDetailsReference;

    String userID;
    String mCurrentPhotoPath;
    String profileImageUrl;

    Uri imageHoldUri;
    Bitmap bitmap;

    ArrayList<Photo> userImages;

    ValueEventListener listenerForProfilePhoto;
    ValueEventListener valueEventListener;
    ValueEventListener listenerForUserDetails;

    Button editProfileButton;

    Intent editProfileIntent;

    private static final int SELECT_FILE = 2;
    private static final int REQUEST_CAMERA = 3;


    public InstagramPost() {
        // Required empty public constructor
    }

    public static InstagramPost newInstance() {
        InstagramPost fragment = new InstagramPost();
        return fragment;
    }

    private void pushProfilePic() {

        try {

            mUserProfilePhotoStorageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(getContext(), "Photo Deleted!", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            mUserProfilePhotoStorageReference.putFile(imageHoldUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            userProfilePhotoDatabaseReference.child("imageurl").setValue(downloadUrl.toString());
                            Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instagram_post, container, false);

        editProfileButton = (Button) rootView.findViewById(R.id.editProfileButton);
        userProfileImage = (CircularImageView) rootView.findViewById(R.id.profileImageCircular);
        noPhotosToShow = (TextView) rootView.findViewById(R.id.noPhotos);
        noPhotosToShow.setVisibility(View.INVISIBLE);
        totalFollowers = (TextView) rootView.findViewById(R.id.totalfollowers);
        totalFollowing = (TextView) rootView.findViewById(R.id.totalfollowing);
        numberOfPosts = (TextView) rootView.findViewById(R.id.totalposts);
        Name = (TextView) rootView.findViewById(R.id.usernameView);
        BIO = (TextView) rootView.findViewById(R.id.bioView);


        firebaseAuth = FirebaseAuth.getInstance();
        userID = String.valueOf(firebaseAuth.getCurrentUser().getUid());

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editProfileIntent = new Intent(getContext(), EditProfileActivity.class);
                editProfileIntent.putExtra("profileimage", profileImageUrl);
                startActivity(editProfileIntent);
            }
        });

        try {

            mUserProfilePhotoStorageReference = FirebaseStorage.getInstance().getReference()
                    .child(userID).child("User_Profile_Photo");

            userProfilePhotoDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").
                    child(userID);


            PostsDatabase = FirebaseDatabase.getInstance();
            userImagesReference = PostsDatabase.getReference().child("Users").child(userID).child("user_photos");
            userDetailsReference = PostsDatabase.getReference().child("Users").child(userID);
        }catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProfilePhotoDialogue();

            }
        });


        userImages = new ArrayList<>();

        userPostsView = (RecyclerView) rootView.findViewById(R.id.recyclerInstaPost);
        userPostsView.setHasFixedSize(true);
        userPostsView.setLayoutManager(new GridLayoutManager(getContext(), 3));


        CreateListeners();

        userProfilePhotoDatabaseReference.addListenerForSingleValueEvent(listenerForProfilePhoto);
        userImagesReference.addValueEventListener(valueEventListener);
        userDetailsReference.addListenerForSingleValueEvent(listenerForUserDetails);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        userImagesReference.removeEventListener(valueEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        userImagesReference.addValueEventListener(valueEventListener);
    }

    public void attachAdapter() {

        if (userImages.isEmpty()) {
            noPhotosToShow.setVisibility(View.VISIBLE);
        } else {

            recyclerAdapter = new RecyclerAdapter(userImages, getContext());

            userPostsView.setAdapter(recyclerAdapter);

            if (noPhotosToShow.getVisibility() == View.VISIBLE)
                noPhotosToShow.setVisibility(View.INVISIBLE);
        }
    }

    private void CreateListeners(){

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean contains = false;

                try {
                    numberOfPosts.setText(String.valueOf(dataSnapshot.getChildrenCount()));

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
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Picasso.with(getContext()).load(profileImageUrl).fit().into(userProfileImage);

                    }

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Couldn't Fetch Profile Pic", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listenerForUserDetails = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();

                    Name.setText(hashMap.get("fullName").toString());

                    BIO.setText(hashMap.get("bio").toString());
                }catch (Exception e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void showProfilePhotoDialogue(){
        final CharSequence[] items = {"Show Profile Picture", "Change Profile Picture", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Profile Pic");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Show Profile Picture")) {

                    Intent showProfiePicIntent = new Intent(getContext(), ShowProFilePic.class);
                    userProfilePhotoDatabaseReference.addListenerForSingleValueEvent(listenerForProfilePhoto);
                    //Log.i("PROFILE IMAGE URL", String.valueOf(profileImageUrl));
                    showProfiePicIntent.putExtra("picurl", profileImageUrl);
                    startActivity(showProfiePicIntent);

                } else if (items[item].equals("Change Profile Picture")) {
                    imagePickerDialog();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void imagePickerDialog() {

        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");

        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    dispatchTakePictureIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageHoldUri = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageHoldUri);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void galleryIntent() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {

            bitmap = (Bitmap) data.getExtras().get("data");
            userProfileImage.setImageBitmap(bitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "Title", null);
            imageHoldUri = Uri.parse(path);

            try {
                pushProfilePic();
            } catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {

            profileImageUrl = imageHoldUri.toString();

            Picasso.with(getContext()).load(imageHoldUri).fit().into(userProfileImage);

            try {
                pushProfilePic();
            } catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
