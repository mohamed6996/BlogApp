package com.android.blogapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.android.blogapp.StartActivity.mGoogleApiClient;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseRecyclerAdapter adapter;

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    DatabaseReference users_database_reference;
    DatabaseReference like_database;

    boolean isLiked = false;
    String post_key;

    // PostViewHolder viewHolder ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        // setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_add) {
                    Intent intent = new Intent(MainActivity.this, PostActivity.class);
                    // intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    return true;
                }
                if (id == R.id.action_logout) {
                    firebaseAuth.signOut();
                    googleSignOut();
                    return true;
                }
                if (id == R.id.action_profile) {
                    Intent intent = new Intent(MainActivity.this, Profile.class);
                    //  intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // check if the user is signed in ( not null ) and update the UI
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    Intent intent = new Intent(MainActivity.this, StartActivity.class);
                    //   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        };

        users_database_reference = FirebaseDatabase.getInstance().getReference().child("users");
        users_database_reference.keepSynced(true);

        like_database = FirebaseDatabase.getInstance().getReference().child("likes");
        like_database.keepSynced(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("blog");
        adapter = new FirebaseRecyclerAdapter<Blog, PostViewHolder>(Blog.class, R.layout.row_list_itm, PostViewHolder.class, ref) {

            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Blog model, final int position) {

              //  viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImageurl());
                post_key = getRef(position).getKey();
                viewHolder.setLikeBtn(post_key);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();  // push id, use that key to launch a new activity
                        //TODO make post content activity
                     //   Intent intent = new Intent(MainActivity.this,SinglePostContent.class);
                     //   intent.putExtra("post_key",post_key);
                     //   startActivity(intent);

                    }
                });

                final String userUid = model.getUserUid();
                users_database_reference.child(userUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String postUserPic = dataSnapshot.child("image").getValue().toString();
                        String postUserName = dataSnapshot.child("name").getValue().toString();
                        String postUserProfession = dataSnapshot.child("profession").getValue().toString();
                        viewHolder.setPostUserName(postUserName);
                        viewHolder.setPostUserProfession(postUserProfession);
                        viewHolder.setPostUserPic(getApplicationContext(), postUserPic);
                        //   Toast.makeText(MainActivity.this, dataSnapshot.child("name").getValue().toString(), Toast.LENGTH_SHORT).show();

                        viewHolder.like.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                isLiked = true;

                                post_key = getRef(position).getKey();

                                like_database.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //  Toast.makeText(MainActivity.this, "worked" + post_key, Toast.LENGTH_SHORT).show();
                                        if (isLiked) {
                                            if (dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                                like_database.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                                                isLiked = false;

                                            } else {
                                                like_database.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).setValue("random");
                                                isLiked = false;

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        recyclerView.setAdapter(adapter);
        isUserExist();


    }

    private void googleSignOut() {
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // updateUI(null);
                    }
                });

    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }


    private void isUserExist() {

        // check if the user uid exist in the database or not
        // when you sign in using email it will exist
        // but when you sign in with google or fb it won`t exist
        // so in this case direct the user to register page

        if (firebaseAuth.getCurrentUser() != null) {

            final String user_id = firebaseAuth.getCurrentUser().getUid();
            users_database_reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(user_id)) {
                        // sign in the user and go to main activity
                        //  Intent intent = new Intent(StartActivity.this, MainActivity.class);
                        //  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //  startActivity(intent);
                    } else {
                        // go to profile page
                        Intent intent = new Intent(MainActivity.this, Profile.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
}
