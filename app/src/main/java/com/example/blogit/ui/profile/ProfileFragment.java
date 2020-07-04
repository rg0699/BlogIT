package com.example.blogit.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.blogit.R;
import com.example.blogit.ui.Models.Post;
import com.example.blogit.ui.Models.User;
import com.example.blogit.ui.activities.EditProfileActivity;
import com.example.blogit.ui.activities.LoginActivity;
import com.example.blogit.ui.activities.MyPostsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {


    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView mUserName;
    private TextView mUserEmail;
    private TextView mUserBio;
    private TextView mUserWebsite;
    private TextView mUserDob;
    private ImageView mUserPhoto;
    private Button btnEditProfile;
    private DatabaseReference mDatabase;
    private ValueEventListener listener;
    private LinearLayout btnItemPost;
    private DatabaseReference databaseReference;
    private List<Post> postList;
    private TextView no_of_posts;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    onStop();
                    //finish();
                }
            }
        };

        ProfileViewModel profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mDatabase= FirebaseDatabase.getInstance().getReference("users");

        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        mUserName=root.findViewById(R.id.userName);
        mUserEmail=root.findViewById(R.id.userEmail);
        mUserPhoto=root.findViewById(R.id.userPhoto);
        mUserBio=root.findViewById(R.id.userBio);
        mUserWebsite=root.findViewById(R.id.userWebsite);
        mUserDob=root.findViewById(R.id.userDob);
        btnEditProfile=root.findViewById(R.id.btn_edit_profile);
        btnItemPost=root.findViewById(R.id.item_post);
        no_of_posts = root.findViewById(R.id.no_of_posts);

        mUserWebsite.setClickable(true);
        mUserWebsite.setMovementMethod(LinkMovementMethod.getInstance());

        //addUserChangeListener();

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        btnItemPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MyPostsActivity.class));
            }
        });

        return root;
    }


    private void addUserChangeListener() {
        // User data change listener
        FirebaseUser user=mAuth.getCurrentUser();
        if (user != null) {
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    // Check for null
                    if (user == null) {
                        //Log.e(TAG, "User data is null!");
                        Toast.makeText(
                                getContext(),
                                "User data is null!",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    else {
                        if(user.name!=null){
                            mUserName.setText(user.name);
                        }
                        else {
                            mUserName.setText(getResources().getString(R.string.user_name));
                        }
                        if(user.email!=null){
                            mUserEmail.setText(user.email);
                        }
                        else {
                            mUserEmail.setText(getResources().getString(R.string.hint_email));
                        }
                        if(user.bio!=null){
                            mUserBio.setText(user.bio);
                        }
                        else {
                            mUserBio.setText(getResources().getString(R.string.hint_bio));
                        }
                        if(user.website!=null){
                            mUserWebsite.setText(user.website);
                        }
                        else {
                            mUserWebsite.setText(getResources().getString(R.string.hint_website));
                        }
                        if(user.dob!=null){
                            mUserDob.setText(user.dob);
                        }
                        else {
                            mUserDob.setText(getResources().getString(R.string.hint_dob));
                        }
                        if(user.photo!=null){
                            Glide.with(requireContext()).load(user.photo).into(mUserPhoto);
                        }
                        else {
                            Glide.with(requireContext()).load(getResources().getDrawable(R.drawable.default_profile_picture)).into(mUserPhoto);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Toast.makeText(
                            getContext(),
                            "Failed to read user!"
                                    + " Please try again later",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            };
            mDatabase.child(user.getUid()).addValueEventListener(listener);
        }
    }

    private void addPostChangeListener() {
        // User data change listener
        FirebaseUser user=mAuth.getCurrentUser();
        if (user != null) {
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    postList = new ArrayList<>();
                    for (DataSnapshot postsnap: dataSnapshot.getChildren()) {

                        Post post = postsnap.getValue(Post.class);
                        if(user.getUid().equals(post.getUserId())){
                            postList.add(post);
                        }
                    }

                    no_of_posts.setText(String.valueOf(postList.size()));

//                    postAdapter = new PostAdapter(getApplicationContext(),postList);
////                    postRecyclerView.setAdapter(postAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReference.addValueEventListener(listener);
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        //progressBar.setVisibility(View.GONE);
//        mAuth.addAuthStateListener(authListener);
//        mDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(listener);
//    }
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (authListener != null) {
//            mAuth.removeAuthStateListener(authListener);
//        }
//        if (listener != null){
//            mDatabase.child(currentUser.getUid()).removeEventListener(listener);
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(authListener);
        addUserChangeListener();
        addPostChangeListener();
        //mDatabase.child(currentUser.getUid()).addValueEventListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
        if (listener != null){
            mDatabase.child(currentUser.getUid()).removeEventListener(listener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(listener != null){
            mDatabase.child(currentUser.getUid()).removeEventListener(listener);
        }
    }

}
