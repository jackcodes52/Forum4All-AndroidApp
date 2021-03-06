package com.example.forum4all_1.homebuttons;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.forum4all_1.HomeActivity;
import com.example.forum4all_1.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class GamesReport extends AppCompatActivity implements View.OnClickListener {

    private ImageView makerpt;
    private EditText rpttitle, rptdesc;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_games_report);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        makerpt = (ImageView) findViewById(R.id.gamesrptpost);
        makerpt.setOnClickListener(this);

        rpttitle = (EditText) findViewById(R.id.gamesrpttitle);
        rptdesc = (EditText) findViewById(R.id.gamesrptdesc);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);


    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.gamesrptpost:
                progressBar.setVisibility(View.VISIBLE);
                if (!rpttitle.getText().toString().isEmpty() && !rptdesc.getText().toString().isEmpty()){
                    //Create Post Object
                    Gamesreportreport report = new Gamesreportreport(rpttitle.getText().toString(),
                            rptdesc.getText().toString(),
                            currentUser.getUid(),
                            currentUser.getDisplayName());

                    // Add post to firebase
                    addReport(report);

                }else{
                    Toast.makeText(this, "Please fill in again", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }

        }
    }

    private void addReport(Gamesreportreport report) {


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Reports in Games Forum").push();

        //Get post Unique ID & update post key

        String key = myRef.getKey();
        report.setPostKey(key);

        // Add post data to firebase database

        myRef.setValue(report).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(GamesReport.this, "You have reported!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                rpttitle.setText("");
                rptdesc.setText("");
                startActivity(new Intent(String.valueOf(GamesActivity.class)));
            }
        });
    }
}




