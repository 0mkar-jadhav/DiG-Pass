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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;
public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText;
    private FirebaseAuth auth;
    Loading_alert loader = new Loading_alert(LoginActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        auth = FirebaseAuth.getInstance();
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email1 = loginEmail.getText().toString();
                String password1 = loginPassword.getText().toString();
                if (TextUtils.isEmpty(email1) || TextUtils.isEmpty(password1)) {
                    Toast.makeText(LoginActivity.this, "Please Enter valid Email and Password", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email1)) {
                    Toast.makeText(LoginActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password1)) {
                    Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email1).matches()) {
                    Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else {
                    login3(email1,password1);
                }
            }
        });
    }
    public void login3(String loginEmail,String loginPassword){
        loader.startalertdialog();
        auth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnSuccessListener(LoginActivity.this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String uid1 = authResult.getUser().getUid();
                FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("User").child("UserDetails").child(uid1).child("usertype").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int usertype = snapshot.getValue(Integer.class);
                            if (usertype == 0) {
                                Toast.makeText(LoginActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                                cratetoken(uid1);
                                Intent intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(LoginActivity.this, "User is not Register", Toast.LENGTH_SHORT).show();
                                loader.closealertdialog();
                            }
                        }else {
                            Toast.makeText(LoginActivity.this, "User is not Register", Toast.LENGTH_SHORT).show();
                            loader.closealertdialog();
                            FirebaseAuth.getInstance().signOut();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "User is not Register"+error, Toast.LENGTH_SHORT).show();
                        loader.closealertdialog();
                        FirebaseAuth.getInstance().signOut();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage = e.getLocalizedMessage();
                loader.closealertdialog();
                assert errorMessage != null;
                if (errorMessage.contains("password1")) {
                    Toast.makeText(LoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                } else if (errorMessage.contains("email1")) {
                    Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void cratetoken(String uid) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    String token = task.getResult();
                    FirebaseDatabase firebaseDatabase =FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("User").child("UserDetails").child(uid).child("token").setValue(token);
                }
            }
        });
    }
}

