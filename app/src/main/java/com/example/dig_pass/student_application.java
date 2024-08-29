package com.example.dig_pass;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class student_application extends AppCompatActivity implements View.OnClickListener {

    private TextView dateTextView, gatepassTextView, applicantEmailTextView, applicantNameTextView, applicantRollTextView, applicantYearTextView, applicantDeptTextView, applicantNoTextView, applicantParentNoTextView, applicantReasonEditText, leavingTimeTextView, vehicleLabelTextView, vehicleNoEditText, fileNameTextView, divTextView;
    private Button btnUpload, btnSubmit, btnTimePicker;
    private RadioGroup vehicleOptionRadioGroup;
    private RadioButton radioYes, radioNo;
    private TextView txtTime;

    private static final int PICK_FILE_REQUEST_CODE = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageReference;

    private Uri selectedFileUri;
    Loading_alert loader = new Loading_alert(student_application.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_application);

        dateTextView = findViewById(R.id.date);
        gatepassTextView = findViewById(R.id.gatepass);
        btnTimePicker = findViewById(R.id.btn_ltime);
        txtTime = findViewById(R.id.ltime);
        applicantEmailTextView = findViewById(R.id.applicant_email);
        applicantNameTextView = findViewById(R.id.applicant_name);
        applicantRollTextView = findViewById(R.id.applicant_roll);
        applicantYearTextView = findViewById(R.id.applicant_year);
        applicantDeptTextView = findViewById(R.id.applicant_branch);
        applicantNoTextView = findViewById(R.id.applicant_no);
        applicantParentNoTextView = findViewById(R.id.applicant_parent_no);
        applicantReasonEditText = findViewById(R.id.applicant_reason);
        divTextView = findViewById(R.id.applicant_div);
        leavingTimeTextView = findViewById(R.id.ltime);
        vehicleLabelTextView = findViewById(R.id.vlable);
        vehicleNoEditText = findViewById(R.id.vehicleno);
        btnUpload = findViewById(R.id.btnUpload);
        btnSubmit = findViewById(R.id.btnSubmit);
        vehicleOptionRadioGroup = findViewById(R.id.vopt);
        radioYes = findViewById(R.id.radioYes);
        radioNo = findViewById(R.id.radioNo);
        fileNameTextView = findViewById(R.id.fileName);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, handle this accordingly
            return;
        }
        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Display current date
        updateCurrentDate();

        // Generate and display gate pass number
        generateAndDisplayGatePassNumber();

        btnTimePicker.setOnClickListener(this);

        vehicleOptionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioYes) {
                vehicleNoEditText.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioNo) {
                vehicleNoEditText.setVisibility(View.GONE);
            }
        });

        // Set an onClickListener for the upload button
        btnUpload.setOnClickListener(v -> openFilePicker());

        // Set an onClickListener for the submit button
        btnSubmit.setOnClickListener(v -> submitForm());

        // Fetch user data from Realtime Database
        retrieveUserData();
    }

    // Update and display the current date
    private void updateCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = dateFormat.format(calendar.getTime());
        dateTextView.setText("Date: " + currentDate);
    }

    // Generate and display gate pass number automatically
    private void generateAndDisplayGatePassNumber() {
        String gatePassNumber = generateGatePassNumber();
        gatepassTextView.setText("Pass no: " + gatePassNumber);
    }

    // Generate gate pass number automatically
    private String generateGatePassNumber() {
        Random rand = new Random();
        int randNumber = rand.nextInt(1000);
        return String.valueOf(randNumber);
    }

    // Fetch user data from Realtime Database
    private void retrieveUserData() {

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("Name").getValue(String.class);
                    String email = dataSnapshot.child("Email").getValue(String.class);
                    String year = dataSnapshot.child("Year").getValue(String.class);
                    String Dept = dataSnapshot.child("Dept").getValue(String.class);
                    String roll = dataSnapshot.child("RollNo").getValue(String.class);
                    String division = dataSnapshot.child("Division").getValue(String.class);
                    String mobileNo = dataSnapshot.child("MobileNo").getValue(String.class);
                    String parentNo = dataSnapshot.child("ParentNo").getValue(String.class);

                    applicantNameTextView.setText(name);
                    applicantEmailTextView.setText(email);
                    applicantDeptTextView.setText(Dept);
                    applicantRollTextView.setText(roll);
                    applicantYearTextView.setText(year);
                    applicantNoTextView.setText(mobileNo);
                    applicantParentNoTextView.setText(parentNo);
                    divTextView.setText(division);

                    for (DataSnapshot requestSnapshot : dataSnapshot.child("Gatepass").getChildren()) {
                        if (requestSnapshot.exists()) {
                            int app = requestSnapshot.child("Approval").getValue(Integer.class);
                            String gatepassid = requestSnapshot.getKey().toString();
                            if (app>0 && app<4){
                                Toast.makeText(student_application.this, "Gatepass is in progress", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(student_application.this, StudentDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (app == 4) {
                                Intent intent = new Intent(student_application.this, QRCodeActivity.class);
                                intent.putExtra("Gatepassid",gatepassid);
                                startActivity(intent);
                                finish();

                            }


                        }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    }

    // Open file picker for uploading
    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    // Handle the result of file picking
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                selectedFileUri = data.getData();
                String fileName = getFileName(selectedFileUri);
                fileNameTextView.setText("File Name: " + fileName);
            }
        }
    }

    // Get file name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // Submit form
    private void submitForm() {
        // Check for empty fields
        loader.startalertdialog();
        if (TextUtils.isEmpty(leavingTimeTextView.getText().toString())) {
            showToast("Enter Leaving Time");
            loader.closealertdialog();
        } else if (TextUtils.isEmpty(applicantReasonEditText.getText().toString())) {
            showToast("Enter Reason");
            loader.closealertdialog();
        } else {
            // Check if a file is selected
            if (selectedFileUri != null) {
                uploadFile(selectedFileUri);
            } else {
                // If no file is selected, proceed with form submission without uploading a file

                saveFormDataToDatabase(null);
            }
        }
    }

    // Upload file to Firebase Storage
    private void uploadFile(Uri fileUri) {
        StorageReference fileRef = storageReference.child("files/" + System.currentTimeMillis() + getFileName(fileUri));
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String fileUrl = uri.toString();
                        saveFormDataToDatabase(fileUrl);
                    });
                })
                .addOnFailureListener(e -> {Toast.makeText(student_application.this, "Failed to upload file", Toast.LENGTH_SHORT).show();loader.closealertdialog();
                                            loader.closealertdialog();});
    }

    // Save form data to Firebase Realtime Database
    private void saveFormDataToDatabase(String fileUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String applicant = applicantNameTextView.getText().toString();
        String dept = applicantDeptTextView.getText().toString();
        String year = applicantYearTextView.getText().toString();
        String date = dateTextView.getText().toString();
        String gatePassNumber = gatepassTextView.getText().toString();
        String leavingTime = leavingTimeTextView.getText().toString();
        String reason = applicantReasonEditText.getText().toString();
        String vehicleNumber = vehicleNoEditText.getText().toString();
        String vehicleOption = (radioYes.isChecked()) ? "Yes" : "No";
        int approv = 1;

                    DatabaseReference userGatepassRef = FirebaseDatabase.getInstance().getReference().child("User")
                            .child("UserDetails")
                            .child(userId)
                            .child("Gatepass")
                            .push();

                    HashMap<String, Object> applicationData = new HashMap<>();
                    applicationData.put("Date", date);
                    applicationData.put("GatePassNumber", gatePassNumber);
                    applicationData.put("LeavingTime", leavingTime);
                    applicationData.put("Reason", reason);
                    applicationData.put("VehicleOption", vehicleOption);
                    if (vehicleOption.equals("Yes") || vehicleOption.equals("yes"))
                        applicationData.put("VehicleNumber", vehicleNumber);
                    if (fileUrl != null) {
                        applicationData.put("FileUrl", fileUrl);
                    }
                    applicationData.put("Approval", approv);


                    userGatepassRef.setValue(applicationData)
                            .addOnSuccessListener(aVoid -> {
                                sendNotification(applicant, dept, year);
                                loader.closealertdialog();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(student_application.this, "Failed to submit form", Toast.LENGTH_SHORT).show();
                                loader.closealertdialog();
                            });
    }

    // Show toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void sendNotification(String applicantName,String dept,String year) {
       userRef = FirebaseDatabase.getInstance().getReference().child("User").child("Class Teacher").child(dept).child(year);
       userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();

                    try {
                        JSONObject jsonObject= new JSONObject();


                        JSONObject notification = new JSONObject();
                        notification.put("title","Gate-pass Request");
                        notification.put("body","New Gate-pass request from " + applicantName);

                        JSONObject dataobj = new JSONObject();
                        dataobj.put("usertype",2);

                        jsonObject.put("to",token);
                        jsonObject.put("data",dataobj);
                        jsonObject.put("notification",notification);


                         callAPI(jsonObject);
//                        Toast.makeText(student_application.this, "Request send successfully", Toast.LENGTH_SHORT).show();



                    }catch (Exception e){
                        Toast.makeText(student_application.this, "Exception"+e, Toast.LENGTH_SHORT).show();
                        Log.d("Exception",e.toString());

                    }
                }else {
                    Toast.makeText(student_application.this, "Admin is offline", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(student_application.this, "no notification", Toast.LENGTH_SHORT).show();

            }
        });


    }

    void callAPI(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization","key="+R.string.firebsekey)
                .addHeader("Content-Type","application/json")
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                }
            });
            Toast.makeText(student_application.this, "Request sent successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, StudentDashboardActivity.class);
            startActivity(intent);
    }
}