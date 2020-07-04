package com.example.blogit.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.blogit.R;
import com.example.blogit.ui.Adapters.PostAdapter;
import com.example.blogit.ui.Models.Post;
import com.firebase.ui.auth.viewmodel.AuthViewModelBase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    RecyclerView postRecyclerView ;
    PostAdapter postAdapter ;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference ;
    SwipeRefreshLayout mSwipeRefreshLayout;
    List<Post> postList;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Posts");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        postRecyclerView  = findViewById(R.id.postRV);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        postRecyclerView.setHasFixedSize(true);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Posts");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;

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

                    postAdapter = new PostAdapter(getApplicationContext(),postList);
                    postRecyclerView.setAdapter(postAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReference.addListenerForSingleValueEvent(listener);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        addPostChangeListener();
        //mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (authListener != null) {
//            mAuth.removeAuthStateListener(authListener);
//        }
        if (listener != null){
            databaseReference.removeEventListener(listener);
        }
    }

}
