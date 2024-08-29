package com.example.dig_pass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dig_pass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageUserActivity extends AppCompatActivity {

    private ListView listView;
    private UserListAdapter adapter;
    private List<User> userList;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, handle this accordingly
            Toast.makeText(this, "Login First", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Admin_login.class);
            finish();
            return;
        }
        String userId = currentUser.getUid();
        listView = findViewById(R.id.listView);
        userList = new ArrayList<>();
        adapter = new UserListAdapter(this, userList);
        listView.setAdapter(adapter);
        userRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails");
        getAdmin(userId);
    }

    private void getAdmin(String admin) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails").child(admin);
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String adminDept = snapshot.child("Dept").getValue(String.class);
                    showUser(adminDept);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageUserActivity.this, "Failed to fetch admin data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showUser(String adminDept) {
        DatabaseReference staffRef = FirebaseDatabase.getInstance().getReference().child("User").child("StaffDetails");
        staffRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot staffSnapshot) {
                for (DataSnapshot staffUserSnapshot : staffSnapshot.getChildren()) {
                    String staffDept = staffUserSnapshot.child("Dept").getValue(String.class);
                    if (staffDept != null && staffDept.equals(adminDept)) {
                        String name = staffUserSnapshot.child("Name").getValue(String.class);
                        String email = staffUserSnapshot.child("Email").getValue(String.class);
                        User user = new User(name, email);
                        userList.add(user);
                    }
                }

                DatabaseReference userDetailsRef = FirebaseDatabase.getInstance().getReference().child("User").child("UserDetails");
                userDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userDetailsSnapshot) {
                        for (DataSnapshot userDetailsUserSnapshot : userDetailsSnapshot.getChildren()) {
                            String userDetailsDept = userDetailsUserSnapshot.child("Dept").getValue(String.class);
                            if (userDetailsDept != null && userDetailsDept.equals(adminDept)) {
                                String name = userDetailsUserSnapshot.child("Name").getValue(String.class);
                                String email = userDetailsUserSnapshot.child("Email").getValue(String.class);
                                User user = new User(name, email);
                                userList.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError userDetailsError) {
                        Toast.makeText(ManageUserActivity.this, "Failed to fetch user details: " + userDetailsError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError staffError) {
                Toast.makeText(ManageUserActivity.this, "Failed to fetch staff details: " + staffError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
