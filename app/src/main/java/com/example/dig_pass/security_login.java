package com.example.dig_pass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class security_login extends AppCompatActivity {

    private EditText login_username;
    private EditText login_password;
    private Button login_button;
    private FirebaseAuth auth;
    private DatabaseReference dbref;
    Loading_alert loader = new Loading_alert(security_login.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_login);

        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);
        auth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference().child("Guard");

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredUsername = login_username.getText().toString();
                String enteredPassword = login_password.getText().toString();

                if (TextUtils.isEmpty(enteredUsername) || TextUtils.isEmpty(enteredPassword)) {
                    Toast.makeText(security_login.this, "Please Enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    login(enteredUsername,enteredPassword);
                }
            }
        });
    }

    private void login(String enteredUsername, String enteredPassword) {
        loader.startalertdialog();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String user =snapshot.child("User").getValue(String.class);
                    String pass =snapshot.child("pass").getValue(String.class);

                    if (enteredUsername.equals(user) && enteredPassword.equals(pass)){
                        Toast.makeText(security_login.this, "Login successfully", Toast.LENGTH_SHORT).show();
                        // Start QR scanning activity
                        startActivityForResult(new Intent(security_login.this, QR.class), 1);
                    }
                    else{
                        Toast.makeText(security_login.this, "Incorrect Email and Password", Toast.LENGTH_SHORT).show();
                        loader.closealertdialog();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loader.closealertdialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String scannedQRData = data.getStringExtra("QRData");
                // Display scanned QR data in AlertDialog
                displayScannedData(scannedQRData);
            } else {
                Toast.makeText(this, "QR scanning failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayScannedData(String scannedData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanned QR Data");
        builder.setMessage(scannedData);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
