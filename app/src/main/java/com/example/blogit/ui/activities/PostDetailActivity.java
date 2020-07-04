package com.example.blogit.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.blogit.ui.activities.MainActivity.getTimeAgo;

public class PostDetailActivity extends AppCompatActivity {

    ImageView imgPost,authorImg,imgCurrentUser;
    TextView postDesc, postDate, postTitle;
    EditText editTextComment;
    Button btnAddComment;
    String PostKey;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    FirebaseDatabase mDatabase;
    //RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "Comment" ;
    TextView authorName;
    private TextView allComments;
    private DatabaseReference commentRef;
    private ValueEventListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Post");

        //RvComment = findViewById(R.id.rv_comment);
        imgPost =findViewById(R.id.post_detail_img);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img);

        allComments = findViewById(R.id.all_comments);

        postTitle = findViewById(R.id.post_detail_title);
        postDesc = findViewById(R.id.post_detail_desc);
        postDate = findViewById(R.id.post_detail_date);
        authorName = findViewById(R.id.post_detail_authorName);
        authorImg = findViewById(R.id.post_detail_authorImg);

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

        allComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PostDetailActivity.this, CommentDetailActivity.class);

                intent.putExtra("postKey",PostKey);
                startActivity(intent);
                //finish();
            }
        });


        String postImage = getIntent().getExtras().getString("postImage") ;
        Glide.with(this).load(postImage).into(imgPost);

        String postTitle = getIntent().getExtras().getString("title");
        this.postTitle.setText(postTitle);

        String postAuthorName = getIntent().getExtras().getString("authorName");
        authorName.setText(postAuthorName);

        String userPostId = getIntent().getExtras().getString("userId");
        setAuthorImg(userPostId);

        String postDescription = getIntent().getExtras().getString("description");
        postDesc.setText(postDescription);

        //addUserChangeListener();
        // get post id
        PostKey = getIntent().getExtras().getString("postKey");

        String date = getTimeAgo(getIntent().getExtras().getLong("postDate"));
        postDate.setText(date);

        commentRef = mDatabase.getReference(COMMENT_KEY).child(PostKey);

        //iniRvComment();

    }

    private void setAuthorImg(String userPostId) {

        mDatabase.getReference("users").child(userPostId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        Glide.with(getApplicationContext()).load(user.photo).into(authorImg);
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

        //RvComment.setLayoutManager(new LinearLayoutManager(this));

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComment = new ArrayList<>();
                for (DataSnapshot snap:dataSnapshot.getChildren()) {

                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment) ;

                }

                int n=listComment.size();
                String s;
                if(n==0){
                    s="Add a comment";
                }
                else  if (n==1){
                    s="View " + n + " comment";
                }
                else {
                    s="View all " + n + " comments";
                }
                allComments.setText(s);

//                commentAdapter = new CommentAdapter(getApplicationContext(),listComment);
//                RvComment.setAdapter(commentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        commentRef.addValueEventListener(listener);

    }


    private String timestampToString(long time) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy",calendar).toString();
        return date;

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
