package com.example.blogit.ui.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.blogit.R;
import com.example.blogit.ui.Adapters.CommentAdapter;
import com.example.blogit.ui.Models.Comment;
import com.example.blogit.ui.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentDetailActivity extends AppCompatActivity {

    ImageView imgCurrentUser;
    EditText editTextComment;
    Button btnAddComment;
    String PostKey;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    FirebaseDatabase mDatabase;
    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "Comment" ;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference commentRef;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Comments");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        RvComment = findViewById(R.id.rv_comment);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img);
        editTextComment = findViewById(R.id.post_detail_comment);
        btnAddComment = findViewById(R.id.post_detail_add_comment_btn);
        btnAddComment.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        editTextComment.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length()==0){
                    btnAddComment.setEnabled(false);
                } else {
                    btnAddComment.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });


        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference commentReference = mDatabase.getReference(COMMENT_KEY).child(PostKey).push();
                String comment_content = editTextComment.getText().toString();

                String uid = mCurrentUser.getUid();
                Comment comment = new Comment(comment_content,uid);

                commentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("comment added");
                        editTextComment.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("failed to add comment : "+e.getMessage());
                    }
                });

            }
        });

        PostKey = getIntent().getExtras().getString("postKey");

        commentRef = mDatabase.getReference(COMMENT_KEY).child(PostKey);

        RvComment.setLayoutManager(new LinearLayoutManager(this));

//        addUserChangeListener();
//
//        iniRvComment();

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

    private void iniRvComment() {

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComment = new ArrayList<>();
                for (DataSnapshot snap:dataSnapshot.getChildren()) {

                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment) ;

                }

                commentAdapter = new CommentAdapter(getApplicationContext(),listComment);
                RvComment.setAdapter(commentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        commentRef.addValueEventListener(listener);

    }

    private void addUserChangeListener() {
        // User data change listener
        FirebaseUser user=mAuth.getCurrentUser();
        mDatabase.getReference("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Check for null
                if (user == null) {
                    //Log.e(TAG, "User data is null!");
                    showMessage("User data is null!");
                }
                else {
                    if(user.photo!=null){
                        Glide.with(getApplicationContext()).load(user.photo).into(imgCurrentUser);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                showMessage("Failed to read user!"
                        + " Please try again later");

            }
        });
    }

    private void showMessage(String message) {

        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        addUserChangeListener();
        iniRvComment();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listener != null){
            commentRef.removeEventListener(listener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener != null){
            commentRef.removeEventListener(listener);
        }
    }
}
