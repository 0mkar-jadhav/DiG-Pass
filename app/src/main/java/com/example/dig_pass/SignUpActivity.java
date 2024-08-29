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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class  SignUpActivity extends AppCompatActivity {
    private static final int GALLERY_REQ_CODE = 1000;
    private Uri imageUri;
    // Views
    private EditText emailEditText, passwordEditText, nameEditText, rollEditText, mobileNoEditText, parentNoEditText;
    private Spinner yearSpinner, branchSpinner, divisionSpinner;
    private Button signUpButton;
    private TextView loginRedirectText;
    private ImageView photo;

    // Firebase
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    Loading_alert loader = new Loading_alert(SignUpActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Bind views
        initializeViews();

        // Set up spinner adapters
        setUpSpinnerAdapters();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        // Initialize views
        nameEditText = findViewById(R.id.signup_name);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_password);
        rollEditText = findViewById(R.id.signup_roll);
        mobileNoEditText = findViewById(R.id.signup_no);
        parentNoEditText = findViewById(R.id.signup_parent_no);
        yearSpinner = findViewById(R.id.signup_year);
        branchSpinner = findViewById(R.id.signup_branch);
        divisionSpinner = findViewById(R.id.signup_div);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signUpButton = findViewById(R.id.signup_button);
        photo = findViewById(R.id.photo);
    }

    private void setUpSpinnerAdapters() {
        // Set up spinner adapters
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.year,
                android.R.layout.simple_spinner_item
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        ArrayAdapter<CharSequence> branchAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.dept,
                android.R.layout.simple_spinner_item
        );
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchSpinner.setAdapter(branchAdapter);

        ArrayAdapter<CharSequence> divisionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.div,
                android.R.layout.simple_spinner_item
        );
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);
    }

    private void setClickListeners() {
        // Set click listeners
        photo.setOnClickListener(v -> openGallery());

        loginRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
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
        String year = yearSpinner.getSelectedItem().toString();
        String branch = branchSpinner.getSelectedItem().toString();
        String roll = rollEditText.getText().toString();
        String division = divisionSpinner.getSelectedItem().toString();
        String mobileNo = mobileNoEditText.getText().toString();
        String parentNo = parentNoEditText.getText().toString();

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
        if (TextUtils.isEmpty(year)) {
            showToast("Please select your year");
            return;
        }
        if (TextUtils.isEmpty(branch)) {
            showToast("Please select your branch");
            return;
        }
        if (TextUtils.isEmpty(roll)) {
            rollEditText.setError("Please enter your roll number");
            return;
        }
        if (TextUtils.isEmpty(division)) {
            showToast("Please select your division");
            return;
        }
        if (TextUtils.isEmpty(mobileNo) || mobileNo.length() != 10) {
            mobileNoEditText.setError("Invalid mobile number");
            return;
        }
        if (TextUtils.isEmpty(parentNo) || parentNo.length() != 10) {
            parentNoEditText.setError("Invalid parent's mobile number");
            return;
        }

        // If all fields are valid, proceed with registration
        uploadImageAndData(email, password, name, year, branch, roll, division, mobileNo, parentNo);
    }

    private boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void uploadImageAndData(String email, String password, String name, String year, String branch,
                                    String roll, String division, String mobileNo, String parentNo) {
        if (imageUri != null) {
            loader.startalertdialog();
            StorageReference ref = storageReference.child("images/" + name + "_profile.jpg");
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Handle success
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveUserData(email, password, name, year, branch, roll, division, mobileNo, parentNo, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {showToast("Failed to upload image: " + e.getMessage());loader.closealertdialog();});
        } else {
            showToast("Please select an image");
        }
    }

    private void saveUserData(String email, String password, String name, String year, String branch,
                              String roll, String division, String mobileNo, String parentNo, String imageUrl) {
        final int usertype=0;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        currentUser.sendEmailVerification().addOnCompleteListener(SignUpActivity.this,task1 -> {
                            if (task1.isSuccessful()){
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails").child(userId);
                                    HashMap<String, Object> userData = new HashMap<>();
                                    userData.put("Email", email);
                                    userData.put("Name", name);
                                    userData.put("Year", year);
                                    userData.put("Dept", branch);
                                    userData.put("RollNo", roll);
                                    userData.put("Division", division);
                                    userData.put("MobileNo", mobileNo);
                                    userData.put("ParentNo", parentNo);
                                    userData.put("ProfileImageUrl", imageUrl);
                                    userData.put("Password", hashPassword(password));
                                    userData.put("usertype",usertype);

                                    userRef.setValue(userData)
                                            .addOnCompleteListener(task2-> {
                                                if (task2.isSuccessful()) {
                                                    showToast("Data saved successfully");
                                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                    finish();
                                                } else {
                                                    currentUser.delete();
                                                    showToast("Failed to save data: " + task2.getException().getMessage());
                                                    loader.closealertdialog();
                                                }
                                            });
                                }
                            }else {
                                showToast("Registration failed: " + task.getException().getMessage());
                                loader.closealertdialog();
                            }

                        });

                    } else {
                        showToast("Registration failed: " + task.getException().getMessage());
                        loader.closealertdialog();
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
