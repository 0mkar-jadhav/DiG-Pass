package com.example.dig_pass;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class StudentDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView helloText;
    private ImageView staffPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails"); // Assuming staff details are stored under "staff" node

        helloText = findViewById(R.id.hello_text); // Assuming you have a TextView with id hello_text
        staffPhoto = findViewById(R.id.staff_photo); // Assuming you have an ImageView with id staff_photo

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Fetch staff details from database using user's UID
            mDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get staff name and photo URL
                        String name = dataSnapshot.child("Name").getValue(String.class);
                        String photoUrl = dataSnapshot.child("ProfileImageUrl").getValue(String.class);

                        // Set staff name
                        helloText.setText("Hello " + name);

                        // Load staff photo using Glide or any other image loading library
                        // Assuming you have a method to load image from URL
                        loadImage(photoUrl);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                    Toast.makeText(StudentDashboardActivity.this, "Failed to get staff details", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Method to load image from URL
    private void loadImage(String photoUrl) {
        Glide.with(this)
                .load(photoUrl) // Load the image from the given URL
//                .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
//                .error(R.drawable.error_image) // Image to display if loading fails
//                .diskCacheStrategy(DiskCacheStrategy.ALL) // Caching strategy
                .into(staffPhoto); // ImageView to load the image into
    }

    public void OnGateClick(View view){
        Intent intent=new Intent(StudentDashboardActivity.this,student_application.class);
        startActivity(intent);
    }

    public void OnLogout(View view){
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(StudentDashboardActivity.this , Dashboard.class));
        finish();

    }

    public  void OnEdit(View view){
        startActivity(new Intent(StudentDashboardActivity.this,edit_student.class));
    }
    public void OnHistory(View view){
        startActivity(new Intent(StudentDashboardActivity.this,Student_history.class));
    }

}

