package com.example.shv.imageeditor;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditorView extends AppCompatActivity {
    private List<Filter> filters;
    private RecyclerView thumbListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_view);

        filters = createFilters();
        thumbListView = (RecyclerView) findViewById(R.id.filterRecyclerView);
        RecyclerAdapter adapter = new RecyclerAdapter(filters, getApplication());
        thumbListView.setAdapter(adapter);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);

        thumbListView.setLayoutManager(lm);
        thumbListView.setHasFixedSize(true);
        thumbListView.addOnItemTouchListener(new CustomRVItemTouchListener(this, thumbListView, new RecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(getBaseContext(), Integer.toString(position), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }
    public List<Filter> createFilters() {
        List<Filter> filters = new ArrayList<>();
        int a[][] = new int[3][3];
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                a[i][j] = 1;
        filters.add(new Filter("Normal", a));

        a = new int[3][3];
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                a[i][j] = 1;

        filters.add(new Filter("Blur", a));

        a = new int[3][3];
        for(int i = 0; i < 3; ++i)
            for(int j = 0; j < 3; ++j)
                if(i == 1 && j == 1)
                    a[i][j] = 8;
                else
                    a[i][j] = -1;
        filters.add(new Filter("Edges", a));

        return filters;
    }
}