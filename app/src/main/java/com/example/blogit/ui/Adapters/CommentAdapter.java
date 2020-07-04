package com.example.blogit.ui.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
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
import com.example.blogit.ui.Models.Comment;
import com.example.blogit.ui.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.blogit.ui.activities.MainActivity.getTimeAgo;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private List<Comment> mData;


    public CommentAdapter(Context mContext, List<Comment> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.comment_row,parent,false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

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
                    holder.comment_userName.setText(user.name);
                    if(user.photo!=null){
                        Glide.with(mContext).load(user.photo).into(holder.comment_userImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.comment_content.setText(mData.get(position).getContent());
        holder.comment_date.setText(getTimeAgo((long)mData.get(position).getTimeStamp()));

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{

        ImageView comment_userImg;
        TextView comment_userName, comment_content, comment_date;

        public CommentViewHolder(View itemView) {
            super(itemView);
            comment_userImg = itemView.findViewById(R.id.comment_user_img);
            comment_userName = itemView.findViewById(R.id.comment_username);
            comment_content = itemView.findViewById(R.id.comment_content);
            comment_date = itemView.findViewById(R.id.comment_date);

        }

    }


    private String timestampToString(long time) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm",calendar).toString();
        return date;

    }

}
