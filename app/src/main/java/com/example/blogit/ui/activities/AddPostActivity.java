package com.example.blogit.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.blogit.R;
import com.example.blogit.ui.Models.Post;
import com.example.blogit.ui.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AddPostActivity extends AppCompatActivity {

    private ImageView postImg;
    private EditText postTitle, postDesc;
    private ImageView mUserPhoto;

    private Uri mImageUri =null;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private ValueEventListener listener;

    private static final int GALLERY_REQUEST = 1;

    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Add Post");

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = mAuth.getCurrentUser();

        //for getting users name from Users
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        postImg = findViewById(R.id.post_image);
        postTitle = findViewById(R.id.post_title);
        postDesc = findViewById(R.id.post_description);
        mProgressBar = findViewById(R.id.progressBar);
        mUserPhoto = findViewById(R.id.userPhoto);


        addUserChangeListener();

        postImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.postButton:
                startPosting();
                break;
        }
        return true;

    }

    private void startPosting() {

        final String title_val = postTitle.getText().toString().trim();
        final String desc_val = postDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null)
        {
            mProgressBar.setVisibility(View.VISIBLE);

            final StorageReference filepath = mStorage.child("Blog_Images").child(random()+".jpg");

            /*from google doc*/
            Log.d("image name", String.valueOf(mImageUri));
            UploadTask uploadTask = filepath.putFile(mImageUri);
            mProgressBar.setVisibility(View.VISIBLE);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();

                        final DatabaseReference newPost = mDatabase.child("Posts").push();

                        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Post post = new Post(title_val,
                                        desc_val,
                                        downloadUri.toString(),
                                        mCurrentUser.getUid());


                                String key = newPost.getKey();
                                post.setPostKey(key);

                                newPost.setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(AddPostActivity.this, "Post Added Successfully!!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(AddPostActivity.this,MainActivity.class));
                                            finish();
                                        }
                                        else
                                        {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(AddPostActivity.this, "Error"+error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        mProgressBar.setVisibility(View.INVISIBLE);

                    } else {
                        // Handle failures
                        String error = task.getException().getMessage();
                        Toast.makeText(AddPostActivity.this, "Failed to add post! "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else
        {
            Toast.makeText(this, "Insert image and text!", Toast.LENGTH_SHORT).show();
        }

    }


    private void openImageIntent() {

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
            cameraIntents.add(intent);
        }

        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                }
                else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if (isCamera) {
                    postImg.setImageURI(mImageUri);
                }
                else {
                    mImageUri = data.getData();
                    postImg.setImageURI(mImageUri);
                }
            }
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
//        {
////            mImageUri = data.getData();
////            imageButton.setImageURI(mImageUri);
//
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            postImg.setImageBitmap(photo);
//        }
//    }

    public static String random() {
        int MAX_LENGTH = 9;
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void addUserChangeListener() {
        // User data change listener
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            listener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    // Check for null
                    if (user == null) {
                        //Log.e(TAG, "User data is null!");
                        Toast.makeText(
                                getApplicationContext(),
                                "User data is null!",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    else {
                        if(user.photo!=null){
                            Glide.with(getApplicationContext()).load(user.photo).into(mUserPhoto);
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    //Log.e(TAG, "Failed to read user", error.toException());
                    Toast.makeText(
                            getApplicationContext(),
                            "Failed to read user!"
                                    + " Please try again later",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            };
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(listener);
        }
    }

}
