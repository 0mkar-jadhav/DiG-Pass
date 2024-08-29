package com.example.dig_pass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class staff_signup extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nameEditText, mobileNoEditText;
    private Spinner deptSpinner, postSpinner;
    private Button signUpButton;
    private TextView loginRedirectText;

    private ImageView photo;

    private final int GALLERY_REQ_CODE = 1000;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;

    private FloatingActionButton uplbtn;
    private FirebaseAuth mAuth;
    Loading_alert loader = new Loading_alert(staff_signup.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_signup);
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        initializeViews();

        // Set up spinner adapters
        setUpSpinnerAdapters();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.signup_name);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_password);
        mobileNoEditText = findViewById(R.id.signup_no);
        deptSpinner = findViewById(R.id.dept);
        postSpinner = findViewById(R.id.post);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signUpButton = findViewById(R.id.signup_button);
        uplbtn = findViewById(R.id.uplphoto);
        photo = findViewById(R.id.photo);
    }

    private void setUpSpinnerAdapters() {
        ArrayAdapter<CharSequence> d_adapter = ArrayAdapter.createFromResource(
                this,
                R.array.dept,
                android.R.layout.simple_spinner_item
        );
        d_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deptSpinner.setAdapter(d_adapter);

        ArrayAdapter<CharSequence> p_adapter = ArrayAdapter.createFromResource(
                this,
                R.array.post,
                android.R.layout.simple_spinner_item
        );
        p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postSpinner.setAdapter(p_adapter);
    }

    private void setClickListeners() {
        // Set click listeners
        photo.setOnClickListener(v -> openGallery());

        loginRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(staff_signup.this, Staff_login.class);
            startActivity(intent);
        });
        signUpButton.setOnClickListener(view -> signUp());
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery, GALLERY_REQ_CODE);
    }

    private void signUp() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String post = postSpinner.getSelectedItem().toString();
        String dept = deptSpinner.getSelectedItem().toString();
        String mobtext = mobileNoEditText.getText().toString();

        // Validate fields
        if (TextUtils.isEmpty(email) || !isValidEmail(email)) {
            emailEditText.setError("Invalid email");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password should be at least 6 characters long");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Please enter your name");
            return;
        }
        if (TextUtils.isEmpty(post)) {
            showToast("Please select your post");
            return;
        }
        if (TextUtils.isEmpty(dept)) {
            showToast("Please select your department");
            return;
        }
        if (TextUtils.isEmpty(mobtext) || mobtext.length() != 10) {
            mobileNoEditText.setError("Invalid mobile number");
            return;
        }

        // If all fields are valid, proceed with registration
        uploadImageAndData(email, password, name, dept, post, mobtext);
    }

    private boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void uploadImageAndData(String email, String password, String name, String dept, String post,
                                    String mobileNo) {
        if (imageUri != null) {
            loader.startalertdialog();
            StorageReference ref = storageReference.child("images/" + name + "_profile.jpg");
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Handle success
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveUserData(email, password, name, dept, post, mobileNo, imageUrl);
                        });
                    })
                    .addOnFailureListener(e ->{ showToast("Failed to upload image: " + e.getMessage());loader.closealertdialog();});
        } else {
            showToast("Please select an image");
        }
    }

    private void saveUserData(String email, String password, String name, String dept, String post,
                              String mobileNo, String imageUrl) {
        final int usertype=1;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(staff_signup.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String userId = currentUser.getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails").child(userId);
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("Email", email);
                            userData.put("Name", name);
                            userData.put("Dept", dept);
                            userData.put("Post", post);
                            userData.put("MobileNo", mobileNo);
                            userData.put("ProfileImageUrl", imageUrl);
                            userData.put("Password", hashPassword(password));
                            userData.put("usertype",usertype);

                            userRef.setValue(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            showToast("Data saved successfully");
                                            startActivity(new Intent(staff_signup.this, Staff_login.class));
                                            finish();
                                        } else {
                                            currentUser.delete();
                                            showToast("Failed to save data: " + task1.getException().getMessage());
                                            loader.closealertdialog();
                                        }
                                    });
                        }
                    } else {
                        showToast("Registration failed: " + task.getException().getMessage());
                    }
                });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_REQ_CODE && data != null) {
            imageUri = data.getData();
            photo.setImageURI(imageUri);
            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photo.setClipToOutline(true);
        }
    }

    // Show toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
