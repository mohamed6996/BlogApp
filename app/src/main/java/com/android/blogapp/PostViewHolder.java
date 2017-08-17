package com.android.blogapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    //  private final TextView postTitle;
    private final TextView PostDesc, post_user_name, post_user_profession;
    private final ImageView imageView;
    CircleImageView post_user_pic;
    ImageButton like;

    DatabaseReference likeDatabase;
    FirebaseAuth firebaseAuth;

    public PostViewHolder(View itemView) {
        super(itemView);
        //   postTitle = (TextView) itemView.findViewById(R.id.postTitle);
        PostDesc = (TextView) itemView.findViewById(R.id.post_desc);
        imageView = (ImageView) itemView.findViewById(R.id.postImage);
        post_user_pic = (CircleImageView) itemView.findViewById(R.id.post_user_pic);
        post_user_name = (TextView) itemView.findViewById(R.id.post_user_name);
        post_user_profession = (TextView) itemView.findViewById(R.id.post_user_profession);
        like = (ImageButton) itemView.findViewById(R.id.like);

        firebaseAuth = FirebaseAuth.getInstance();
        likeDatabase = FirebaseDatabase.getInstance().getReference().child("likes");

       /* imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/


    }

    /*  public void setTitle(String title) {
          postTitle.setText(title);
      }
  */
    public void setDesc(String desc) {
        PostDesc.setText(desc);
    }

    public void setImage(Context context, String imageurl) {
        Glide.with(context).load(imageurl).into(imageView);
    }

    public void setPostUserName(String postUserName) {
        post_user_name.setText(postUserName);
    }

    public void setPostUserProfession(String PostUserProfession) {
        post_user_profession.setText(PostUserProfession);
    }

    public void setPostUserPic(Context context, String postUserPic) {
        Glide.with(context).load(postUserPic).into(post_user_pic);
    }

    public void setLikeBtn(final String post_key) {

        likeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                   // like.setBackgroundColor(Color.CYAN);
                    like.setImageTintList(ColorStateList.valueOf(Color.BLUE));
                } else {
                   // like.setBackgroundColor(Color.BLACK);
                    like.setImageTintList(ColorStateList.valueOf(Color.BLACK));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}