package com.android.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.R.attr.password;

public class StartActivity extends AppCompatActivity {
    TextInputLayout edt_email, edt_password;
    Button btn_sign_up, btn_sign_in;
    // ProgressDialog progressDialog;

    private final static int RC_SIGN_IN = 1;
    static GoogleApiClient mGoogleApiClient;

    SignInButton signInButton;

    FirebaseAuth firebaseAuth;
    //  FirebaseAuth.AuthStateListener authStateListener;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        firebaseAuth = FirebaseAuth.getInstance();
      /*  authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                isUserExist();
            }
        };*/
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.keepSynced(true);
        // this path will be synced in all activities

        //   progressDialog = new ProgressDialog(this);

        edt_email = (TextInputLayout) findViewById(R.id.loginemail);
        edt_password = (TextInputLayout) findViewById(R.id.loginpassword);
        btn_sign_up = (Button) findViewById(R.id.sign_up);
        btn_sign_in = (Button) findViewById(R.id.sign_in);
        signInButton = (SignInButton) findViewById(R.id.google);

        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = edt_email.getEditText().getText().toString();
                String userpassword = edt_password.getEditText().getText().toString();

                //   progressDialog.setMessage("signing in");
                //  progressDialog.show();
                firebaseAuth.signInWithEmailAndPassword(userEmail, userpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            //  progressDialog.dismiss();
                            isUserExist();
                            finish();
                        }
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //  progressDialog.dismiss();
                                String error_msg = e.getMessage();
                                //    Toast.makeText(StartActivity.this, error_msg, Toast.LENGTH_SHORT).show();

                                if (error_msg.contains("badly formatted")) {
                                    Toast.makeText(StartActivity.this, "please enter a valid email", Toast.LENGTH_SHORT).show();
                                } else if (error_msg.contains("no user record")) {
                                    Toast.makeText(StartActivity.this, "create account", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(StartActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //  firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //   progressDialog.setMessage("signing in...");
            //   progressDialog.show();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                //   progressDialog.dismiss();
                //  isUserExist();

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                //   progressDialog.dismiss();
            }
        }
    }


    // trigger when google sign in clicked
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //  Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        // showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //   Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            //    updateUI(user);
                            isUserExist();
                        } else {
                            // If sign in fails, display a message to the user.
                            //    Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //    updateUI(null);
                        }
                    }
                });


    }

    private void isUserExist() {

        // check if the user uid exist in the database or not
        // when you sign in using email it will exist
        // but when you sign in with google or fb it won`t exist
        // so in this case direct the user to register page

        if (firebaseAuth.getCurrentUser() != null) {

            final String user_id = firebaseAuth.getCurrentUser().getUid();
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(user_id)) {
                        // sign in the user and go to main activity
                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        // go to profile page
                        Intent intent = new Intent(StartActivity.this, Profile.class);
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
