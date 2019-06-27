package com.devp.sid.socialmediaapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //views
    ImageView avtarIv;
    TextView nameTv;
    TextView emailTv;
    TextView phoneTv;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth= FirebaseAuth.getInstance();
        user= firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");

        //init
        avtarIv= view.findViewById(R.id.avtarIv);
        nameTv= view.findViewById(R.id.nameTv);
        emailTv= view.findViewById(R.id.emailTv);
        phoneTv= view.findViewById(R.id.phoneTv);

        //To get info of current user from DB(firebase)
        Query query= databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until we get req data
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    //get data
                    String name= ""+ds.child("name").getValue();
                    String email= ""+ds.child("email").getValue();
                    String phone= ""+ds.child("phone").getValue();
                    String image= ""+ds.child("image").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //if image is recieved
                        Picasso.get().load(image).into(avtarIv);
                    }
                    catch (Exception e){
                        //if any exception while recieving image
                        Picasso.get().load(R.drawable.ic_add_image).into(avtarIv);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        return view;
    }

}
