package com.example.dig_pass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Dashboard extends AppCompatActivity {


    Loading_alert loader = new Loading_alert(Dashboard.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

    }
    public void onStudentLoginClick(View view) {
        // Create an Intent to open the StudentLoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onAdminLoginClick(View view) {
        // Create an Intent to open the AdminLoginActivity
        Intent intent = new Intent(this, Admin_login.class);
        startActivity(intent);
        finish();
    }
    public void onStaffLoginClick(View view){
        Intent intent=new Intent(this,Staff_login.class );
        startActivity(intent);
        finish();
    }
    public void onGuardLoginClick(View view) {
        // Create an Intent to open the GuardLoginActivity
        Intent intent = new Intent(this, security_login.class);
        startActivity(intent);
        finish();
    }



}