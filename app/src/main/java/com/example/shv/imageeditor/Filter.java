package com.example.shv.imageeditor;

import android.graphics.Bitmap;

public class Filter {
    public String card_title;
    public int filterMatrix[][];
    public  int size;
    public int factor;

    Filter(String title, int [][]filterMatrix, int size, int factor){
        this.card_title = title;
        this.filterMatrix = filterMatrix;
        this.size = size;
        this.factor = factor;
    }
}
