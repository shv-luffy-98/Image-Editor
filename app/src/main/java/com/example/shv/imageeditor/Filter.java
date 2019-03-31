package com.example.shv.imageeditor;

import android.graphics.Bitmap;

public class Filter {
    public String card_title;
    public int filterMatrix[][];

    Filter(String title, int [][]filterMatrix){
        this.card_title = title;
        this.filterMatrix = filterMatrix;
    }
}
