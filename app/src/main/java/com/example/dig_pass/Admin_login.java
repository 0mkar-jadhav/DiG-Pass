package com.example.dig_pass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.StorageReference;

public class Admin_login extends AppCompatActivity {

    private EditText login_username;
    private EditText login_password;
    private Button loginButton;
    private Button login_button;
    private FirebaseAuth auth;
    private StorageReference storageReference;

    SharedPreferences sharedpreferences;
    Loading_alert loader = new Loading_alert(Admin_login.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);
        auth = FirebaseAuth.getInstance();



        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredUsername = login_username.getText().toString();
                String enteredPassword = login_password.getText().toString();

                if (TextUtils.isEmpty(enteredUsername) || TextUtils.isEmpty(enteredPassword)) {
                    Toast.makeText(Admin_login.this, "Please Enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    loader.startalertdialog();
                    login3(enteredUsername, enteredPassword);

                }
            }
        });
    }

    private void cratetoken(String dept,String post,String year,int usertype) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    String token = task.getResult();
                    FirebaseDatabase firebaseDatabase =FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("User").child(post).child(dept).child(year).child("token").setValue(token);
                }
            }
        });
    }

    private void cratetoken(String dept,String post,int usertype) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    String token = task.getResult();
                    FirebaseDatabase firebaseDatabase =FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("User").child(post).child(dept).child("token").setValue(token);
                }
            }
        });
    }

    public void login3(String loginemailtxt,String loginPasswordtxt ){

        auth.signInWithEmailAndPassword(loginemailtxt,loginPasswordtxt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (auth.getCurrentUser().isEmailVerified()) {
                        String uid1 = task.getResult().getUser().getUid();
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        firebaseDatabase.getReference().child("User").child("UserDetails").child(uid1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    int usertype = snapshot.child("usertype").getValue(Integer.class);
                                    String post = snapshot.child("Post").getValue(String.class);
                                    String dept = snapshot.child("Dept").getValue(String.class);
                                    String year = snapshot.child("Year").getValue(String.class);
                                    String name = snapshot.child("Name").getValue(String.class);
                                    if (usertype == 2) {
                                        cratetoken(dept, post,year,usertype);
                                        Toast.makeText(Admin_login.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Admin_login.this, AdminDashboardActivity.class);
                                        intent.putExtra("dept", dept);
                                        intent.putExtra("usertype",usertype);
                                        intent.putExtra("adminid",uid1);
                                        intent.putExtra("name",name);
                                        startActivity(intent);
                                        finish();
                                    }else if (usertype == 3){
                                        cratetoken(dept, post,usertype);
                                        Toast.makeText(Admin_login.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Admin_login.this, AdminDashboardActivity.class);
                                        intent.putExtra("dept", dept);
                                        intent.putExtra("usertype",usertype);
                                        intent.putExtra("name",name);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        FirebaseAuth.getInstance().signOut();
                                        Toast.makeText(Admin_login.this, "User is not Register", Toast.LENGTH_SHORT).show();
                                        loader.closealertdialog();
                                    }
                                } else {
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(Admin_login.this, "Incorrect credential", Toast.LENGTH_SHORT).show();
                                    loader.closealertdialog();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Admin_login.this, "User is not Register" + error, Toast.LENGTH_SHORT).show();
                                loader.closealertdialog();
                                FirebaseAuth.getInstance().signOut();
                            }
                        });
                    } else {
                        Toast.makeText(Admin_login.this, "Please verify Email first ", Toast.LENGTH_SHORT).show();
                        loader.closealertdialog();
                    }
                }else {
                    loader.closealertdialog();
                    Toast.makeText(Admin_login.this, "Incorrect credential", Toast.LENGTH_SHORT).show();
                    login_username.setText("");
                    login_password.setText("");
                }
            }
                
        });

    }
}
