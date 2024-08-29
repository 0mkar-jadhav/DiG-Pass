package com.example.dig_pass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class GatePassAdapter extends ArrayAdapter<GatePass> {

    private Context mContext;
    private List<GatePass> mGatePassList;

    public GatePassAdapter(Context context, List<GatePass> gatePassList) {
        super(context, 0, gatePassList);
        mContext = context;
        mGatePassList = gatePassList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item_gatepass, parent, false);
        }

        GatePass currentGatePass = mGatePassList.get(position);

        TextView gatePassNumberTextView = listItem.findViewById(R.id.gatePassNumberTextView);
        gatePassNumberTextView.setText(currentGatePass.getGatePassNumber());

        TextView leavingTimeTextView = listItem.findViewById(R.id.leavingTimeTextView);
        leavingTimeTextView.setText("Leaving Time: "+currentGatePass.getLeavingTime());

        TextView dateTextView = listItem.findViewById(R.id.dateTextView);
        dateTextView.setText(currentGatePass.getDate());

        TextView reasonTextView = listItem.findViewById(R.id.reasonTextView);
        reasonTextView.setText("Reason: "+currentGatePass.getReason());

        return listItem;
    }
}
