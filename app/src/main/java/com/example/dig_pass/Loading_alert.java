package com.example.dig_pass;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class Loading_alert {
   private Activity activity;
   private AlertDialog dialog;
    Loading_alert(Activity myactivity){
        activity = myactivity;
    }

    public void startalertdialog(){
        AlertDialog.Builder builder= new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loader, null));

        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void closealertdialog(){
        dialog.dismiss();
    }
}
