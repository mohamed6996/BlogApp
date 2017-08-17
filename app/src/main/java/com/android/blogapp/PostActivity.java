package com.android.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    EditText postDesc;
    // ImageButton imageSelect;
    // Button btn_submit;
    CircleImageView circleImageView;
    ImageView placeholder;
    Button selectImage, submitPost;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser current_user;

    ProgressDialog progressDialog;

    Uri imageUri;
    public static final int GALLERY_REQ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        circleImageView = (CircleImageView) findViewById(R.id.post_user_image);
        placeholder = (ImageView) findViewById(R.id.post_image_placeholder);
        selectImage = (Button) findViewById(R.id.post_selectimage);
        submitPost = (Button) findViewById(R.id.post_submit);
        postDesc = (EditText) findViewById(R.id.postDesc);


        progressDialog = new ProgressDialog(this);


        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user = firebaseAuth.getCurrentUser();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String postUserPic = dataSnapshot.child("image").getValue().toString();
                Glide.with(PostActivity.this).load(postUserPic).into(circleImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQ);

            }
        });

        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ && resultCode == RESULT_OK) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(placeholder);
        }
    }

    private void startPosting() {

        progressDialog.setMessage("Posting...");
        progressDialog.show();

        final String desc = postDesc.getText().toString().trim();

        final StorageReference file_path = storageReference.child("blog_images").child(imageUri.getLastPathSegment());
        file_path.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri downloadUri = taskSnapshot.getDownloadUrl();
                progressDialog.dismiss();

                DatabaseReference newPost = databaseReference.child("blog").push(); // db name/blog/push_unique_id/title+desc+img
                newPost.child("desc").setValue(desc);
                //   newPost.child("desc").setValue(description);
                newPost.child("imageurl").setValue(downloadUri.toString());
                newPost.child("userUid").setValue(current_user.getUid());

                finish();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
