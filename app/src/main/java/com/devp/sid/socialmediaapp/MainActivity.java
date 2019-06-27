package com.devp.sid.socialmediaapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //Views
    Button mRegisterBtn;
    Button mLoginBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mRegisterBtn= findViewById(R.id.register_btn);
        mLoginBtn= findViewById(R.id.login_btn);

        //Register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start register activity
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
            }
        });

        //Login btn click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start login activity
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });

    }
}
