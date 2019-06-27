package com.devp.sid.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //ActionBar and its title
        actionBar= getSupportActionBar();

        //fragment home transfer to set default(on start)
        actionBar.setTitle("Home");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.cotent,fragment1,"");
        ft1.commit();

        //init
        firebaseAuth= FirebaseAuth.getInstance();

        //Bottom navigation
        BottomNavigationView navigationView= findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListner);



    }

    //bottom options handling
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListner=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //handle item clicked
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            //fragment home transfer
                            actionBar.setTitle("Home");
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.cotent,fragment1,"");
                            ft1.commit();
                            return true;

                        case R.id.nav_profile:
                            //fragment profile transfer
                            actionBar.setTitle("Profile");
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.cotent,fragment2,"");
                            ft2.commit();
                            return true;

                        case R.id.nav_users:
                            //fragment user transfer
                            actionBar.setTitle("Users");
                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.cotent,fragment3,"");
                            ft3.commit();
                            return true;

                    }
                    return false;
            }
    };

    private void checkUserStatus(){
        //get current user
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if(user != null){
            //user is signed in
            //set email of logged in user
            //mProfileTv.setText(user.getEmail() );

        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start
        checkUserStatus();
        super.onStart();
    }

    //Option Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //menu item Click

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id= item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
