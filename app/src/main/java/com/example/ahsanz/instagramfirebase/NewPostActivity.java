package com.example.ahsanz.instagramfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewPostActivity extends AppCompatActivity implements View.OnClickListener {


    ImageView SelectNewImage;
    TextView Caption;
    ImageView AddNewPost;

    FirebaseAuth firebaseAuth;

    DatabaseReference mDatabaseReference;

    String userID;
    StorageReference mStorageReference;
    FirebaseStorage firebaseStorage;
    TextView mProgressText;


    String mCurrentPhotoPath;
    String newPhotoKey;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 0;
    Uri imageHoldUri = null;

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.selectImage) {

            imagePicker();

        } else if (view.getId() == R.id.addPost) {

            mProgressText.setVisibility(View.VISIBLE);
            mProgressText.setText("Uploading Photo...\nThis may take a while.");

            try {

                mStorageReference = firebaseStorage.getReference().child(firebaseAuth.getCurrentUser().getUid())
                        .child("user_photos")
                        .child(imageHoldUri.getLastPathSegment());

                mStorageReference.putFile(imageHoldUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri downloadURL = taskSnapshot.getDownloadUrl();

                        newPhotoKey = mDatabaseReference.push().getKey();

                        mDatabaseReference.child(newPhotoKey)
                                .setValue(new Photo(String.valueOf(downloadURL), Caption.getText().toString(),
                                        0, newPhotoKey, FirebaseAuth.getInstance().getCurrentUser().getUid()));

                        mProgressText.setVisibility(View.INVISIBLE);

                        Toast.makeText(NewPostActivity.this, "Upload Complete!", Toast.LENGTH_SHORT).show();

                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        mProgressText.setVisibility(View.INVISIBLE);

                        Toast.makeText(NewPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(NewPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        SelectNewImage = (ImageView) findViewById(R.id.selectImage);
        Caption = (TextView) findViewById(R.id.caption);
        AddNewPost = (ImageView) findViewById(R.id.addPost);
        mProgressText = (TextView) findViewById(R.id.progressText);
        mProgressText.setVisibility(View.INVISIBLE);
        SelectNewImage.setOnClickListener(this);
        AddNewPost.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        userID = String.valueOf(firebaseAuth.getCurrentUser().getUid());
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("user_photos");

    }

    private void imagePicker() {

        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
        builder.setTitle("Add Photo!");

        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    try {
                        dispatchTakePictureIntent();
                    } catch (Exception e) {
                        Log.i("INITIATION", e.getMessage());
                    }  //cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void galleryIntent() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageHoldUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageHoldUri);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {

            CropImage.activity(imageHoldUri).setGuidelines(CropImageView.Guidelines.ON).
                    start(this);

        } else if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {

            imageHoldUri = data.getData();

            CropImage.activity(imageHoldUri).setGuidelines(CropImageView.Guidelines.ON).
                    start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                imageHoldUri = resultUri;

                Picasso.with(this).load(imageHoldUri).fit().into(SelectNewImage);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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