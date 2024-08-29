//package com.example.dig_pass;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.appcompat.app.AppCompatActivity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.journeyapps.barcodescanner.ScanContract;
//import com.journeyapps.barcodescanner.ScanOptions;
//
//public class QR extends AppCompatActivity
//{
////    Button btn_scan;
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_qr);
//
//
////        btn_scan =findViewById(R.id.btn_scan);
////        btn_scan.setOnClickListener(v->
////        {
////            scanCode();
////        });
//    }
//
//    public void onScanCode(View view)
//    {
//        ScanOptions options = new ScanOptions();
//        options.setPrompt("Volume up to flash on");
//        options.setBeepEnabled(true);
//        options.setOrientationLocked(true);
//        options.setCaptureActivity(CaptureAct.class);
//        barLaucher.launch(options);
//    }
//
//    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
//    {
//        if(result.getContents() !=null)
//        {
//            AlertDialog.Builder builder = new AlertDialog.Builder(QR.this);
//            builder.setTitle("Result");
//            builder.setMessage(result.getContents());
//            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
//            {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i)
//                {
//                    dialogInterface.dismiss();
//                }
//            }).show();
//        }
//    });
//    public void onLogout(View view){
//        FirebaseAuth.getInstance().signOut();
//        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
//        startActivity(new Intent(QR.this , Dashboard.class));
//        finish();
//
//    }
//
//}
package com.example.dig_pass;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QR extends AppCompatActivity {

    private DatabaseReference rootRef;
    private ActivityResultLauncher<ScanOptions> barLauncher;
    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        rootRef = FirebaseDatabase.getInstance().getReference();
        initializeBarLauncher();
    }

    private void initializeBarLauncher() {
        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String gateId = result.getContents();
                displayDetails(gateId);
            }
        });
    }

    public void onScanCode(View view) {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    private void displayDetails(String gateId) {
        fetchDetailsFromDatabase(gateId);
    }

    private void fetchDetailsFromDatabase(String gateId) {

        flag = 0;
        rootRef=FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("User").child("UserDetails");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    DatabaseReference gatepassRef = userSnapshot.child("Gatepass").getRef();

                    gatepassRef.child(gateId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot gatepassSnapshot) {
                            if (gatepassSnapshot.exists()) {
                                flag = 1;
                                int app = gatepassSnapshot.child("Approval").getValue(Integer.class);
                                if (app == 4){
                                String passno = gatepassSnapshot.child("GatePassNumber").getValue().toString();
                                String date = gatepassSnapshot.child("Date").getValue().toString();
                                String name = userSnapshot.child("Name").getValue().toString();
                                String email = userSnapshot.child("Email").getValue().toString();
                                String rollno = userSnapshot.child("RollNo").getValue().toString();
                                String mobile = userSnapshot.child("MobileNo").getValue().toString();
                                String reason = gatepassSnapshot.child("Reason").getValue().toString();
                                String leavingtime = gatepassSnapshot.child("LeavingTime").getValue().toString();
                                String year = userSnapshot.child("Year").getValue().toString();
                                String dept = userSnapshot.child("Dept").getValue().toString();

                                AlertDialog.Builder builder = new AlertDialog.Builder(QR.this);

                                // Set the message show for the Alert time
                                String details = passno+"\n"+
                                        date+"\n"+
                                        "Name : "+name+"\n"+
                                        "Email : "+email+"\n"+
                                        "Roll No : "+rollno+"\n"+
                                        "Mobile : "+mobile+"\n"+
                                        "Reason : "+reason+"\n"+
                                        "Leaving Timr : "+leavingtime+"\n"+
                                        "Year : "+year+"\n"+
                                        "Dept : "+dept;
                                Log.i("details",""+details);
                                builder.setMessage(details);

                                // Set Alert Title
                                builder.setTitle("Gate Pass ");

                                // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                                builder.setCancelable(false);

                                // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                                    // When the user click yes button then app will close
                                    gatepassRef.child(gateId).child("Approval").setValue(5);
                                });

                                // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                                    // If user click no then dialog box is canceled.
                                    gatepassRef.child(gateId).child("Approval").setValue(0);
                                    dialog.cancel();
                                });

                                // Create the Alert dialog
                                AlertDialog alertDialog = builder.create();
                                // Show the Alert Dialog box
                                alertDialog.show();
                            }else {
                                    Toast.makeText(QR.this, "Gate pass is invalid", Toast.LENGTH_SHORT).show();
                                    flag=-1;
                                }
                            } 
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle possible errors
                            Log.w("FirebaseData", "loadGatepass:onCancelled", databaseError.toException());
                        }
                    });
                }
                if (flag == 0){
                    Toast.makeText(QR.this, "No Gate pass found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Log.w("FirebaseData", "loadUsers:onCancelled", databaseError.toException());
            }
        });
        

        
    }

    public void onLogout(View view) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(QR.this, Dashboard.class));
        finish();
    }
}
