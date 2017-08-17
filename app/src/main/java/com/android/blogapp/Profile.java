package com.android.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.doctoror.particlesdrawable.ParticlesDrawable;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    EditText username, profession;
    Uri imageUri;

    RecyclerView recyclerView;
    ProgressDialog progressDialog;

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference;

    CircleImageView circleImageView;

    public static final int GALLERY_REQ = 1;
    private final ParticlesDrawable mDrawable = new ParticlesDrawable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.particlesView).setBackground(mDrawable);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");

        progressDialog = new ProgressDialog(this);

        recyclerView = (RecyclerView) findViewById(R.id.profilerec);
        username = (EditText) findViewById(R.id.profile_user_name);
        profession = (EditText) findViewById(R.id.profile_user_profession);
        circleImageView = (CircleImageView) findViewById(R.id.profile_image);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQ);
            }
        });

        getUserInfo();
    }

    private void getUserInfo() {
        final String user_id = firebaseAuth.getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())) {
                    String name = dataSnapshot.child(user_id).child("name").getValue().toString();
                     String prof = dataSnapshot.child(user_id).child("profession").getValue().toString();
                    String image = dataSnapshot.child(user_id).child("image").getValue().toString();

                    Glide.with(Profile.this).load(image).into(circleImageView);
                    username.setText(name);
                     profession.setText(prof);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupAccount() {
        final String user_name = username.getText().toString().trim();
        final String user_profession = profession.getText().toString().trim();

        //  if (!TextUtils.isEmpty(user_name) && imageUri != null) {

        progressDialog.setMessage("Finishing account...");
        progressDialog.show();
        // upload image to storage and get its url !
        // TODO
       // if (imageUri == null){}
        StorageReference image_path = storageReference.child(imageUri.getLastPathSegment());
        image_path.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String user_id = firebaseAuth.getCurrentUser().getUid();
                String image_download_url = taskSnapshot.getDownloadUrl().toString();
                databaseReference.child(user_id).child("name").setValue(user_name);
                databaseReference.child(user_id).child("profession").setValue(user_profession);
                databaseReference.child(user_id).child("image").setValue(image_download_url);

                progressDialog.dismiss();
                Intent intent = new Intent(Profile.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });


        //  }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            //   profile_pic.setImageURI(imageUri);
            CropImage.activity(imageUri)
                    .setFixAspectRatio(true)
                    .setAspectRatio(1, 1)
                    // .setMaxCropResultSize(9600,9600)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                circleImageView.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_LONG).show();
                //  Exception exception = result.getError();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawable.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDrawable.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.saveProfile) {
            setupAccount();
        }
        return super.onOptionsItemSelected(item);
    }
}
