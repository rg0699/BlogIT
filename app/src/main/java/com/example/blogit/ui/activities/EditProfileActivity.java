package com.example.blogit.ui.activities;

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.blogit.R;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.blogit.ui.activities.AddPostActivity.random;

public class EditProfileActivity extends AppCompatActivity {

    Calendar myCalendar;
    private EditText mUserBio,mUserWebsite,mUserName;
    private TextView mUserDob;
    private ImageView mUserPhoto;
    private LinearLayout userDobLayout;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri =null;
    private Uri outputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Edit Profile");

        auth = FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("users");
        mStorage = FirebaseStorage.getInstance().getReference();

        mUserName=findViewById(R.id.edit_name);
        mUserDob=findViewById(R.id.edit_dob);
        userDobLayout=findViewById(R.id.edit_user_dob);
        mUserPhoto=findViewById(R.id.edit_user_photo);
        mUserBio=findViewById(R.id.edit_bio);
        mUserWebsite=findViewById(R.id.edit_website);

        addUserChangeListener();


        myCalendar = Calendar.getInstance();
        setDateToTextView();


        userDobLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        mUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
//                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile_activty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.saveButton:
                if(mUserName.getText().length()==0 && mUserBio.getText().length()==0 && mUserWebsite.getText().length()==0){
                    Toast.makeText(getApplicationContext(), "Enter your Profile Details!", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendDetails();
                    Toast.makeText(
                            getApplicationContext(),
                            "Profile Updated!",
                            Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
                break;
        }
        return true;

    }

    public void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDateToTextView();
            }
        };

        DatePickerDialog datePickerDialog=new DatePickerDialog(EditProfileActivity.this,dateSetListener,
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

    public void setDateToTextView() {
        Date date = myCalendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String dateToBeSet = sdf.format(date);
        mUserDob.setText(dateToBeSet);
    }


    public void sendDetails() {
        FirebaseUser user=auth.getCurrentUser();
        DatabaseReference userRef = mDatabase.child(user.getUid());

        String name = mUserName.getText().toString().trim();
        if(!name.isEmpty()) {
            userRef.child("name").setValue(name);
        }

        String bio = mUserBio.getText().toString().trim();
        if(!bio.isEmpty()){
            userRef.child("bio").setValue(bio);
        }
        else {
            userRef.child("bio").removeValue();
        }

        String website = mUserWebsite.getText().toString().trim();
        if(!website.isEmpty()){
            userRef.child("website").setValue(website);
        }
        else {
            userRef.child("website").removeValue();
        }

        String dob=mUserDob.getText().toString().trim();
        if(!dob.isEmpty()){
            userRef.child("dob").setValue(dob);
        }
        else {
            userRef.child("dob").removeValue();
        }

        if(mImageUri!=null) {

            final StorageReference filepath = mStorage.child("User_Images").child(random()+".jpg");

            /*from google doc*/
            Log.d("image name", String.valueOf(mImageUri));
            UploadTask uploadTask = filepath.putFile(mImageUri);

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
                        userRef.child("photo").setValue(downloadUri.toString());
                    }
                }
            });
        }
    }

    private void addUserChangeListener() {
        // User data change listener
        FirebaseUser user=auth.getCurrentUser();
        mDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    mUserName.setText(user.name);

                    if(user.photo!=null){
                        Glide.with(getApplicationContext()).load(user.photo).into(mUserPhoto);
                    }

                    mUserBio.setText(user.bio);

                    mUserWebsite.setText(user.website);

                    mUserDob.setText(user.dob);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Toast.makeText(
                        getApplicationContext(),
                        "Failed to read user!"
                                + " Please try again later",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void openImageIntent() {

        final File root = new File(getApplicationContext().getExternalCacheDir() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = "img_"+ System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
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
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                    }
                }

                if (isCamera) {
                    mImageUri = outputFileUri;
                    mUserPhoto.setImageURI(mImageUri);
                }
                else {
                    mImageUri = data.getData();
                    mUserPhoto.setImageURI(mImageUri);
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
//            mImageUri = data.getData();
//            mUserPhoto.setImageURI(mImageUri);
//        }
//    }

}
