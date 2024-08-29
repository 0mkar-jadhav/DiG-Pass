package com.example.dig_pass;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Student_history extends AppCompatActivity {

    private DatabaseReference gatepassRequestRef;

    private ListView listView1;
    private List<GatePass> gatePassList;
    private GatePassAdapter adapter;
    Loading_alert loader = new Loading_alert(Student_history.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_history);
        listView1 = findViewById(R.id.listView1);
        gatePassList = new ArrayList<>();
        adapter = new GatePassAdapter(this, gatePassList);
        listView1.setAdapter(adapter);

        // Update the reference to point to UserDetails -> Gatepass request
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        gatepassRequestRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails").child(userId).child("Gatepass request");

        displayGatepassRequests();
    }

    private void displayGatepassRequests() {
        loader.startalertdialog();
        gatepassRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String gatePassNumber = snapshot.child("GatePassNumber").getValue(String.class);
                    String leavingTime = snapshot.child("LeavingTime").getValue(String.class);
                    String date = snapshot.child("Date").getValue(String.class);
                    String reason = snapshot.child("Reason").getValue(String.class);
                    String aprov1 = snapshot.child("approv1").getValue(String.class);
                    String aprov2 = snapshot.child("approv2").getValue(String.class);
                    String aprov3 = snapshot.child("approv3").getValue(String.class);


                    GatePass gatePass = new GatePass(gatePassNumber, leavingTime, date, reason,aprov1,aprov2,aprov3);
                    gatePassList.add(gatePass);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
        loader.closealertdialog();
    }
}
