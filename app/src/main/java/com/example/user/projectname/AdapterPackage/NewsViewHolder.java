package com.example.user.projectname.AdapterPackage;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.user.projectname.R;

public class NewsViewHolder extends RecyclerView.ViewHolder {
    CardView cardView;
    FloatingActionButton subscribeBtn;
    TextView heading;
    TextView dateTV;

    public NewsViewHolder(View itemView) {
        super(itemView);
        heading = (TextView)itemView.findViewById(R.id.heading);
        dateTV = (TextView)itemView.findViewById(R.id.dateTV);
        subscribeBtn = (FloatingActionButton) itemView.findViewById(R.id.subscribeBtn);
    }
}