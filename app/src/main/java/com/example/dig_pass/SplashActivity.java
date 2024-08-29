package com.example.dig_pass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        auth = FirebaseAuth.getInstance();

        if (getIntent().getExtras() != null) {
            String userTypeString = getIntent().getStringExtra("usertype");
            if (userTypeString != null) {
                int userType = Integer.parseInt(userTypeString);
                handleUserType(userType);
                return; // Exit onCreate to avoid further execution
            }
        }

        // No extras or invalid user type received, proceed with default behavior
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    retrieveUserTypeAndNavigate(currentUser.getUid());
                } else {
                    navigateToDashboard();
                }
            }
        }, 5000); // Splash timer delay
    }

    private void handleUserType(int userType) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            retrieveUserTypeAndNavigate(currentUser.getUid(), userType);
        } else {
            navigateToLogin(userType);
        }
    }

    private void retrieveUserTypeAndNavigate(String userId) {
        FirebaseDatabase.getInstance().getReference()
                .child("User").child("UserDetails").child(userId).child("usertype")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int userType = snapshot.getValue(Integer.class);
                            navigateBasedOnUserType(userType);
                        } else {
                            navigateToDashboard();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        navigateToDashboard();
                    }
                });
    }

    private void retrieveUserTypeAndNavigate(String userId, int userType) {
        FirebaseDatabase.getInstance().getReference()
                .child("User").child("UserDetails").child(userId).child("usertype")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int userTypeDB = snapshot.getValue(Integer.class);
                            if (userType == userTypeDB) {
                                navigateBasedOnUserType(userType);
                            } else {
                                navigateToLogin(userType);
                            }
                        } else {
                            navigateToLogin(userType);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        navigateToLogin(userType);
                    }
                });
    }

    private void navigateBasedOnUserType(int userType) {
        switch (userType) {
            case 0:
                Toast.makeText(SplashActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this, StudentDashboardActivity.class));
                break;
            case 1:
                Toast.makeText(SplashActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this, StaffDashboardActivity.class));
                break;
            case 2:
            case 3:
                Toast.makeText(SplashActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                break;
            default:
                navigateToDashboard();
                break;
        }
        finish();
    }

    private void navigateToLogin(int userType) {
        switch (userType) {
            case 0:
                Toast.makeText(SplashActivity.this, "Login First", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                break;
            case 1:
                Toast.makeText(SplashActivity.this, "Login First", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this, Staff_login.class));
                break;
            case 2:
            case 3:
                Toast.makeText(SplashActivity.this, "Login First", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SplashActivity.this, Admin_login.class));
                break;
            default:
                navigateToDashboard();
                break;
        }
        finish();
    }

    private void navigateToDashboard() {
        startActivity(new Intent(SplashActivity.this, Dashboard.class));
        finish();
    }
}
