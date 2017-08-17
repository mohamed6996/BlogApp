package com.android.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout edt_name, edt_email, edt_password;
    Button createAccount;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");


        edt_name = (TextInputLayout) findViewById(R.id.textInputName);
        edt_email = (TextInputLayout) findViewById(R.id.textInputEmail);
        edt_password = (TextInputLayout) findViewById(R.id.textInputPass);

        createAccount = (Button) findViewById(R.id.createAccount);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user_name = edt_name.getEditText().getText().toString();
                String user_email = edt_email.getEditText().getText().toString();
                String user_password = edt_password.getEditText().getText().toString();

                registerUser(user_name, user_email, user_password);
            }
        });
    }

    private void registerUser(final String user_name, String user_email, String user_password) {
        firebaseAuth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "successful", Toast.LENGTH_SHORT).show();
                            uploaduserinfo(user_name);
                            Intent intent = new Intent(RegisterActivity.this, Profile.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "failed",
                                    Toast.LENGTH_SHORT).show();


                        }

                        // ...
                    }
                });
    }

    private void uploaduserinfo(String user_name) {
        String user_id = firebaseAuth.getCurrentUser().getUid(); // unique key for each user
        DatabaseReference user_info = databaseReference.child(user_id);  // instead of push , as it`s already unique
        user_info.child("name").setValue(user_name);
        user_info.child("image").setValue("default");

    }
}
