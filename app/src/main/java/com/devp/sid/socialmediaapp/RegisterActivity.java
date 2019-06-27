package com.devp.sid.socialmediaapp;

import android.app.ProgressDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText mEmailEt;
    EditText mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;
    //Progress bar
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ActionBar and its title
        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt=findViewById(R.id.emailEt);
        mPasswordEt=findViewById(R.id.passwordEt);
        mRegisterBtn=findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        mAuth=FirebaseAuth.getInstance();

        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        //register btn clicked
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input
                String email= mEmailEt.getText().toString().trim();
                String pass= mPasswordEt.getText().toString().trim();
                //validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //error and focus
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if(pass.length()<6){
                    //error and focus
                    mPasswordEt.setError("Password length at least 6 req");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(email,pass);   //register user
                }


            }
        });
        //handle login textview click listner
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

    }

    private void registerUser(String email, String pass) {
        //email and pass already validated and now show progress dialog
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog box
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            //get user email and uid from auth
                            String email= user.getEmail();
                            String uid= user.getUid();
                            //when user registered store it in firebase database too
                            //using hashmap
                            HashMap<Object, String> hashMap= new HashMap<>();
                            //put info in hashmap
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","");     //will add later
                            hashMap.put("phone","");    //will add later
                            hashMap.put("image","");    //will add later
                            //firebase database instance
                            FirebaseDatabase database= FirebaseDatabase.getInstance();
                            //path to store user data named "Users"
                            DatabaseReference reference= database.getReference("Users");
                            //put data with hashmap in db
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this,"Registered...\n"+user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Errors and dismiss message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();    //go to previous activity
        return super.onSupportNavigateUp();
    }
}
