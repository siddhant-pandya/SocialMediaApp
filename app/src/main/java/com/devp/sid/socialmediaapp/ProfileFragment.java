package com.devp.sid.socialmediaapp;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;
    //path where images of profile and cover will be stored
    String storagePath= "User_Profile_Cover_Imgs/";

    //views
    ImageView avtarIv;
    ImageView coverIv;
    TextView nameTv;
    TextView emailTv;
    TextView phoneTv;
    FloatingActionButton fab;

    //Progress dialog
    ProgressDialog pd;

    //permission Constants
    private static final int CAMERA_REQUEST_CODE= 100;
    private static final int STORAGE_REQUEST_CODE= 200;
    private static final int IMAGE_PICK_GALLERY_CODE= 300;
    private static final int IMAGE_PICK_CAMERA_CODE= 400;

    //Arrays of permission to be requested
    String cameraPermission[];
    String storagePermission[];

    //uri of picked image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;


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
        storageReference= getInstance().getReference();     //firebase storage reference

        //init arrays of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init
        avtarIv= view.findViewById(R.id.avtarIv);
        coverIv= view.findViewById(R.id.coverIv);
        nameTv= view.findViewById(R.id.nameTv);
        emailTv= view.findViewById(R.id.emailTv);
        phoneTv= view.findViewById(R.id.phoneTv);
        fab= view.findViewById(R.id.fab);
        pd= new ProgressDialog(getActivity());

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
                    String cover= ""+ds.child("cover").getValue();

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
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avtarIv);

                    }
                    try {
                        //if image is recieved
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                        //if any exception while recieving image

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });


        return view;
    }

    private boolean checkStoragePermission(){
        //checks if the storage permission is enable or not
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //checks if the storage permission is enable or not
        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result2= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result1 && result2;
    }
    private void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }


    private void showEditProfileDialog() {
        /**Dialog contains options
         * 1)Edit Profile pic
         * 2)Edit Cover photo
         * 3)Edit Name
         * 4)Edit Phone
         */

        //Options to show
        String option[]= {"Edit Profile pic","Edit Cover photo","Edit Name","Edit Phone"};

        //alert dialog box
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set items
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //any item clicked
                if(which==0){
                    //Edit Profile pic
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto= "image";   //changing profile flag
                    showImagePicDialog();
                }
                else if (which == 1) {
                    //Edit Cover photo
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto= "cover";   //changing cover flag
                    showImagePicDialog();
                }
                else if (which == 2) {
                    //Edit Name
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");
                }
                else if (which == 3) {
                    //Edit Phone
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        //create and show dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        //parameter key contains "name"/"phone"
        //as per users wish to update

        //custom dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);       //Update name or Update phone
        //set layout of dialog box
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);     //Hint: Enter name or Enter phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog
        builder.setPositiveButton("Update "+key, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value= editText.getText().toString().trim();
                //validate something is written or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result= new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //updated, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed, dismiss progress, error handling
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(), "Please Enter "+key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //create and show dialog
        builder.create().show();

    }

    private void showImagePicDialog() {
        //show dialog containing options- camera or gallery
        String option[]= {"Camera","Gallery"};

        //alert dialog box
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image from");
        //set items
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //any item clicked
                if(which==0){
                    //Camera
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }

                }
                else if (which == 1) {
                    //Gallery
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });

        //create and show dialog
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called when user choose any of the answer from permission request dialog
        //we will handle permission cases(allowed or denied)
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //picking from camera, first check if camera and storage permission allowed or not
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageaAccepted){
                        //permissions enabled
                        pickFromCamera();
                    }
                    else {
                        //permission Denied
                        Toast.makeText(getActivity(), "Please enable camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //picking from gallery, first check if storage permission allowed or not
                if(grantResults.length >0){
                    boolean writeStorageaAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageaAccepted){
                        //permissions enabled
                        pickFromGallery();
                    }
                    else {
                        //permission Denied
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //this method will be called after picking image from camea or gallery
        if(resultCode == RESULT_OK){
            if(requestCode== IMAGE_PICK_GALLERY_CODE){
                //image picked from gallery, get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        //show progress
        pd.show();
        //same function for both profile pic and cover photo

        //path and name of image to be stored in firebase storage
        //e.g. User_Profile_Cover_Imgs/image_e12f345e42.jpg
        //e.g. User_Profile_Cover_Imgs/cover_q22s3233t1.jpg
        String filePathAndName = storagePath + ""+ profileOrCoverPhoto+"_"+user.getUid();

        StorageReference storageReference2nd= storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get its url and store in user's db
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not and url is recieved
                        if(uriTask.isSuccessful()){
                            //image uploaded
                            //add/update url in user's database
                            HashMap<String ,Object> results = new HashMap<>();
                            //first parameter has value "image" or "cover"
                            //second parameter contains the url of image stored in firebase db
                            results.put(profileOrCoverPhoto,downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in db is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating...", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error, get and show error message, dismiss progress dialog box
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pickFromCamera() {
        //pick pic from camera
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera
        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent= new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }
}
