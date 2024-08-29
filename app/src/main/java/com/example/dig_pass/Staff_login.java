package com.example.dig_pass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Staff_login extends AppCompatActivity {
    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText;
    private FirebaseAuth auth;
    Loading_alert loader = new Loading_alert(Staff_login.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_login);
        loginEmail   = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        auth = FirebaseAuth.getInstance();
        signupRedirectText = findViewById(R.id.signupRedirectText);
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Staff_login.this, staff_signup.class);
                startActivity(intent);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email1 = loginEmail.getText().toString();
                String password1 = loginPassword.getText().toString();
                if (TextUtils.isEmpty(email1) || TextUtils.isEmpty(password1)) {
                    Toast.makeText(Staff_login.this, "Please Enter valid Email and Password", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email1)) {
                    Toast.makeText(Staff_login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password1)) {
                    Toast.makeText(Staff_login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email1).matches()) {
                    Toast.makeText(Staff_login.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else {
                    login3(email1,password1);
                }
            }
        });
    }
    public void login3(String loginEmail,String loginPassword){
        loader.startalertdialog();
        auth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnSuccessListener(Staff_login.this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String uid1 = authResult.getUser().getUid();
                FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("User").child("UserDetails").child(uid1).child("usertype").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int usertype = snapshot.getValue(Integer.class);
                            if (usertype == 1) {
                                Toast.makeText(Staff_login.this, "Login Success", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Staff_login.this, StaffDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(Staff_login.this, "User is not Register", Toast.LENGTH_SHORT).show();
                                loader.closealertdialog();
                            }
                        }else {
                            Toast.makeText(Staff_login.this, "User is not Registered", Toast.LENGTH_SHORT).show();
                            loader.closealertdialog();
                            FirebaseAuth.getInstance().signOut();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Staff_login.this, "User is not Registered"+error, Toast.LENGTH_SHORT).show();
                        loader.closealertdialog();
                        FirebaseAuth.getInstance().signOut();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage = e.getLocalizedMessage();
                assert errorMessage != null;
                loader.closealertdialog();
                if (errorMessage.contains("password1")) {
                    Toast.makeText(Staff_login.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                } else if (errorMessage.contains("email1")) {
                    Toast.makeText(Staff_login.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Staff_login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}




