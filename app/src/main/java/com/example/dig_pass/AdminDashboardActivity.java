package com.example.dig_pass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;



public class AdminDashboardActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);


    }
    public void OnRequestClick(View view) {
        Intent intent1 = getIntent();
        int usertype = intent1.getIntExtra("usertype",2);
        String adminid = intent1.getStringExtra("adminid");

        Intent intent = new Intent(this, ManageRequestActivity.class);
        intent.putExtra("usertype",usertype);
        intent.putExtra("adminid",adminid);
        startActivity(intent);
    }

    public void OnUserClick(View view) {
        // Create an Intent to open the StudentLoginActivity
        Intent intent = new Intent(this, ManageUserActivity.class);
        startActivity(intent);
    }
    public void OnLogout(View view) {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AdminDashboardActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminDashboardActivity.this , Dashboard.class));
                finish();
            }
        });
        
    }
    public void OnEdit(View view) {
        Intent intent = new Intent(this, Edit_admin.class);
        startActivity(intent);
    }


}