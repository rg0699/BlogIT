package com.example.blogit.ui.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blogit.R;
import com.example.blogit.ui.Models.Post;
import com.example.blogit.ui.Models.User;
import com.example.blogit.ui.activities.PostDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.example.blogit.ui.activities.MainActivity.getTimeAgo;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    Context mContext;
    List<Post> mData ;


    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.post_row,parent,false);
        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String s= mData.get(position).getUserId();

        DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users").child(s);

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user == null) {

                    Toast.makeText(
                            mContext,
                            "User data is null!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                else {
                    holder.postUserName.setText(user.name);
                    if(user.photo!=null){
                        Glide.with(mContext).load(user.photo).into(holder.postUserPhoto);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.postTitle.setText(mData.get(position).getTitle());
        holder.postDescription.setText(mData.get(position).getDescription());
        holder.postTime.setText(getTimeAgo((long)mData.get(position).getTimeStamp()));
        Glide.with(mContext).load(mData.get(position).getPostImage()).into(holder.postImage);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView postTitle;
        TextView postDescription;
        TextView postUserName;
        TextView postTime;
        ImageView postImage;
        ImageView postUserPhoto;

        public MyViewHolder(View itemView) {
            super(itemView);

            postTitle = itemView.findViewById(R.id.post_title);
            postDescription = itemView.findViewById(R.id.post_description);
            postImage = itemView.findViewById(R.id.post_image);
            postUserName = itemView.findViewById(R.id.userName);
            postUserPhoto = itemView.findViewById(R.id.userPhoto);
            postTime = itemView.findViewById(R.id.postTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);
                    int position = getAdapterPosition();

                    postDetailActivity.putExtra("title",mData.get(position).getTitle());
                    postDetailActivity.putExtra("postImage",mData.get(position).getPostImage());
                    postDetailActivity.putExtra("description",mData.get(position).getDescription());
                    postDetailActivity.putExtra("postKey",mData.get(position).getPostKey());
                    postDetailActivity.putExtra("userId",mData.get(position).getUserId());
                    postDetailActivity.putExtra("authorName",postUserName.getText().toString());
                    long timestamp  = (long) mData.get(position).getTimeStamp();
                    postDetailActivity.putExtra("postDate",timestamp) ;
                    mContext.startActivity(postDetailActivity);

                }
            });

        }

    }

}
