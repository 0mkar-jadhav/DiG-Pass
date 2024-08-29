package com.example.dig_pass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class edit_staff extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nameEditText, mobileNoEditText;
    private Spinner deptSpinner, postSpinner;
    private Button signUpButton;
    private ImageView photo;
    private FloatingActionButton uplbtn;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageReference;

    private static final int GALLERY_REQ_CODE = 1000;

    Loading_alert loader = new Loading_alert(edit_staff.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_staff);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, handle this accordingly
            return;
        }
        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference();

        nameEditText = findViewById(R.id.signup_name);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_password);
        mobileNoEditText = findViewById(R.id.signup_no);
        deptSpinner = findViewById(R.id.dept);
        postSpinner = findViewById(R.id.post);
        signUpButton = findViewById(R.id.signup_button);
        photo = findViewById(R.id.photo);
        uplbtn = findViewById(R.id.uplphoto);
        Spinner dept_spinner = findViewById(R.id.dept);
        ArrayAdapter<CharSequence> d_adapter = ArrayAdapter.createFromResource(
                this,
                R.array.dept,
                android.R.layout.simple_spinner_item
        );
        d_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dept_spinner.setAdapter(d_adapter);

        Spinner post_spinner = findViewById(R.id.post);
        ArrayAdapter<CharSequence> p_adapter = ArrayAdapter.createFromResource(
                this,
                R.array.post,
                android.R.layout.simple_spinner_item
        );
        p_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        post_spinner.setAdapter(p_adapter);

        // Retrieve user data
        retrieveUserData();

        // Set up photo upload button click listener
        uplbtn.setOnClickListener(view -> openGallery());

        signUpButton.setOnClickListener(view -> updateStaffDetails());


    }

    private void updateStaffDetails() {
        loader.startalertdialog();
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String encryptedPassword = hashPassword(password);

        String mobileNo = mobileNoEditText.getText().toString().trim();

        String dept = deptSpinner.getSelectedItem().toString();
        String post = postSpinner.getSelectedItem().toString();

        // Check if any field is empty
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || dept.isEmpty() || mobileNo.isEmpty() || post.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            loader.closealertdialog();
            return;
        }

        // Update user data in Firebase
        userRef.child("Name").setValue(name);
        userRef.child("Email").setValue(email);
        userRef.child("Password").setValue(encryptedPassword);
        userRef.child("MobileNo").setValue(mobileNo);
        userRef.child("Dept").setValue(dept);
        userRef.child("Post").setValue(post);

        Toast.makeText(this, "User details updated successfully", Toast.LENGTH_SHORT).show();
        loader.closealertdialog();
    }

    private void retrieveUserData() {
        loader.startalertdialog();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("Name").getValue(String.class);
                    String email = dataSnapshot.child("Email").getValue(String.class);
                    String encryptedPassword = dataSnapshot.child("Password").getValue(String.class);
                    String dept = dataSnapshot.child("Dept").getValue(String.class);
                    String post = dataSnapshot.child("Post").getValue(String.class);
                    String mobileNo = dataSnapshot.child("MobileNo").getValue(String.class);
                    String imageUrl = dataSnapshot.child("ProfileImageUrl").getValue(String.class);
                    String decryptedPassword = decryptPassword(encryptedPassword);

                    // Update UI with retrieved data
                    nameEditText.setText(name);
                    emailEditText.setText(email);
                    mobileNoEditText.setText(mobileNo);
                    passwordEditText.setText(decryptedPassword);

                    if (dept != null) {
                        int deptPosition = ((ArrayAdapter) deptSpinner.getAdapter()).getPosition(dept);
                        deptSpinner.setSelection(deptPosition);
                    }
                    if (post != null) {
                        int postPosition = ((ArrayAdapter) postSpinner.getAdapter()).getPosition(post);
                        postSpinner.setSelection(postPosition);
                    }
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).transform(new CircleTransformation()).into(photo);
                    } else {
                        // Handle case where user data does not exist
                    }
                    loader.closealertdialog();
                }else{
                    loader.closealertdialog();
                    Toast.makeText(edit_staff.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private String decryptPassword(String encryptedPassword) {
        // Implement your decryption logic here
        // For example, you can use the existing hashPassword function
        StringBuilder decryptedPasswordBuilder = new StringBuilder(encryptedPassword);
        decryptedPasswordBuilder.reverse();
        return decryptedPasswordBuilder.toString();
    }
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class CircleTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery, GALLERY_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_REQ_CODE && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Update the photo ImageView with the selected image
                photo.setImageURI(imageUri);
            }
        }
    }
    private void uploadImage(Uri imageUri) {
        StorageReference fileRef = storageReference.child("images/" + System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    // Get the download URL and update the user's profile image
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        userRef.child("ProfileImageUrl").setValue(imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    // Image URL updated successfully
                                    Picasso.get().load(imageUrl).into(photo);
                                    Toast.makeText(edit_staff.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(edit_staff.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(edit_staff.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }
}


