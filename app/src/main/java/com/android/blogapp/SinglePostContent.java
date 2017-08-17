package com.android.blogapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SinglePostContent extends AppCompatActivity {
    ImageView postImage;
    TextView postTitle, postDesc;
    String post_key;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post_content);

        post_key = getIntent().getExtras().getString("post_key");
        postImage = (ImageView) findViewById(R.id.image_post_content);
        postTitle = (TextView) findViewById(R.id.title_post_content);
        postDesc = (TextView) findViewById(R.id.desc_post_content);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("blog").child(post_key);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String title = dataSnapshot.child("title").getValue().toString();
                String desc = dataSnapshot.child("desc").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String uid = dataSnapshot.child("uid").getValue().toString();

                postTitle.setText(title);
                postDesc.setText(desc);
                Glide.with(SinglePostContent.this).load(image).into(postImage);

                //TODO enable delete button
                //  if (firebaseAuth.getCurrentUser().getUid().equals(uid)){
                //  databaseReference.child(post_key).removeValue();
                //  }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
