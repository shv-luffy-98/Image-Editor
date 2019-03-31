package com.example.shv.imageeditor;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ViewHolder extends RecyclerView.ViewHolder{
    CardView cv;
    TextView text;

    ViewHolder (View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        text = (TextView) itemView.findViewById(R.id.card_title);
    }
}
