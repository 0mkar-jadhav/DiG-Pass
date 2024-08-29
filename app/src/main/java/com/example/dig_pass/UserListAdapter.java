package com.example.dig_pass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> userList;

    public UserListAdapter(@NonNull Context context, List<User> userList) {
        super(context, 0, userList);
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
        }

        User currentUser = userList.get(position);

        TextView nameTextView = listItem.findViewById(R.id.nameTextView);
        nameTextView.setText("Name:"+currentUser.getName());

        TextView emailTextView = listItem.findViewById(R.id.emailTextView);
        emailTextView.setText("Email id:"+currentUser.getEmail());

        return listItem;
    }
}
