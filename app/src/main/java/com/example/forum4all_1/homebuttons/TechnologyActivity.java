package com.example.forum4all_1.homebuttons;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.forum4all_1.HomeActivity;
import com.example.forum4all_1.R;
import com.example.forum4all_1.adapters.EducationPostAdapter;
import com.example.forum4all_1.adapters.TechPostAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class TechnologyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PReqCode = 2;
    private static final int REQUESCODE = 2 ;
    private Button MakePostbt, reportforumbt;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    Dialog popAddPost;
    ImageView popupPostImage, popupAddbtn;
    TextView popupTitle, popupDesc;
    ProgressBar popupClickProgress;
    Uri pickedImgUri = null;

    RecyclerView listrecyclerView;
    TechPostAdapter postAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<TechPost> postList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technology);

        MakePostbt = (Button) findViewById(R.id.makepostbtn);
        MakePostbt.setOnClickListener(this);

        reportforumbt = (Button) findViewById(R.id.reportforumbtn);
        reportforumbt.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        iniPopup();
        setupPopupImageClick();
    }

    private void setupPopupImageClick(){

        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkAndRequestForPermission();

            }
        });


    }

    private void checkAndRequestForPermission(){

        if (ContextCompat.checkSelfPermission(TechnologyActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(TechnologyActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(TechnologyActivity.this,"Permission to access media.",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(TechnologyActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqCode);
            }
        }else
            openGallery();
    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESCODE && data != null){
            pickedImgUri = data.getData();
            popupPostImage.setImageURI(pickedImgUri);
        }
    }


    private void iniPopup() {

        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.layout_addpost_tech);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER;

        popupPostImage = popAddPost.findViewById(R.id.techinsertimg);
        popupTitle = popAddPost.findViewById(R.id.techTitle);
        popupDesc = popAddPost.findViewById(R.id.techdesc);
        popupAddbtn = popAddPost.findViewById(R.id.techmakepost);
        popupAddbtn.setOnClickListener(this);
        popupClickProgress = popAddPost.findViewById(R.id.progressbar);


    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.reportforumbtn:
                startActivity(new Intent(this, TechReport.class));
                break;
            case R.id.makepostbtn:
                popAddPost.show();
                break;
            case R.id.techmakepost:
                popupClickProgress.setVisibility(View.VISIBLE);
                if (!popupTitle.getText().toString().isEmpty() && !popupDesc.getText().toString().isEmpty() && pickedImgUri != null){

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Technology Images");
                    final StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    //Create Post Object
                                    TechPost post = new TechPost(popupTitle.getText().toString(),
                                            popupDesc.getText().toString(),
                                            imageDownloadLink,
                                            currentUser.getUid(),
                                            currentUser.getDisplayName());

                                    // Add post to firebase

                                    addPost(post);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Something goes wrong
                                    showMessage(e.getMessage());
                                    popupClickProgress.setVisibility(View.INVISIBLE);

                                }
                            });
                        }
                    });

                }else{
                    showMessage("Please insert all fields tq (:");
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }

        }

    }



    private void addPost(TechPost post) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Technology Posts").push();

        //Get post Unique ID & update post key

        String key = myRef.getKey();
        post.setPostKey(key);

        // Add post data to firebase database

        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Post Successfully Added");
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupTitle.setText("");
                popupDesc.setText("");
                popAddPost.dismiss();
            }
        });
    }

    private void showMessage(String message) {

        Toast.makeText(TechnologyActivity.this,message,Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onStart() {

        super.onStart();
        listrecyclerView = findViewById(R.id.techpostlist);
        listrecyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));
        listrecyclerView.setHasFixedSize(true);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Technology Posts");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot postsnap : snapshot.getChildren()) {

                    TechPost post = postsnap.getValue(TechPost.class);
                    postList.add(post);

                }

                postAdapter = new TechPostAdapter(getApplication(), postList);
                listrecyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}