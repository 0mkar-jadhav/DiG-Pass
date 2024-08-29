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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Edit_admin extends AppCompatActivity {
    private EditText  passwordEditText, nameEditText, mobileNoEditText;
    private TextView emailEditText,postview,divview,deptview;

    private Button UpdateButton;
    private ImageView photo;
    private FloatingActionButton uplbtn;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageReference;

    Loading_alert loader = new Loading_alert(Edit_admin.this);

    private static final int GALLERY_REQ_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_admin);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, handle this accordingly
            return;
        }
        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Bind views
        nameEditText = findViewById(R.id.edit_name);
        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        mobileNoEditText = findViewById(R.id.edit_no);
        postview = findViewById(R.id.post);
        deptview = findViewById(R.id.dept);
        divview = findViewById(R.id.div);

        UpdateButton = findViewById(R.id.update_button);
        photo = findViewById(R.id.photo);
        uplbtn = findViewById(R.id.uplphoto);


        // Retrieve user data
        retrieveUserData();

        // Set up photo upload button click listener
        uplbtn.setOnClickListener(view -> openGallery());

        // Set up sign up button click listener
        UpdateButton.setOnClickListener(view -> updateUserDetails());
    }

    // Method to update user details

    private void retrieveUserData() {
        loader.startalertdialog();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("Name").getValue(String.class);
                    String email = dataSnapshot.child("Email").getValue(String.class);
                    String password = dataSnapshot.child("Password").getValue(String.class);
                    String mobileNo = dataSnapshot.child("MobileNo").getValue(String.class);
                    String imageUrl = dataSnapshot.child("ProfileImageUrl").getValue(String.class);
                    String post = dataSnapshot.child("Post").getValue(String.class);
                    String dept = dataSnapshot.child("Dept").getValue(String.class);
                    if (post.equals("Class Teacher")) {
                        String div = dataSnapshot.child("Div").getValue(String.class);
                        divview.setVisibility(View.VISIBLE);
                        divview.setText(div);
                    }



                    // Update UI with retrieved data
                    nameEditText.setText(name);
                    emailEditText.setText(email);
                    mobileNoEditText.setText(mobileNo);
                    passwordEditText.setText(password);
                    postview.setText(post);
                    deptview.setText(dept);



                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).transform(new CircleTransformation()).into(photo);
                    } else {
                        // Handle case where user data does not exist
                    }
                    loader.closealertdialog();
                }else{
                    Toast.makeText(Edit_admin.this, "User not found", Toast.LENGTH_SHORT).show();
                    loader.closealertdialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
    private void updateUserDetails() {
        loader.startalertdialog();
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String mobileNo = mobileNoEditText.getText().toString().trim();

        // Check if any field is empty
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()  || mobileNo.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            loader.closealertdialog();
            return;
        }

        // Update user data in Firebase
        userRef.child("Name").setValue(name);
//        userRef.child("Email").setValue(email);
        userRef.child("Password").setValue(password);
        userRef.child("MobileNo").setValue(mobileNo);


        Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Edit_admin.this, AdminDashboardActivity.class);
        startActivity(intent);
        loader.closealertdialog();
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
                // Upload the image to Firebase Storage
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef = storageReference.child("profile_images/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    // Retrieve the download URL and update the user's profile image URL in the database
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userRef.child("ProfileImageUrl").setValue(uri.toString());
                        Toast.makeText(Edit_admin.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        // Handle error getting the download URL
                        Toast.makeText(Edit_admin.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(Edit_admin.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }
}

