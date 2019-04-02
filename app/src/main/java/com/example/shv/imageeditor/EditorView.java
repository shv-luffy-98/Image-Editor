package com.example.shv.imageeditor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditorView extends AppCompatActivity {
    private List<Filter> filters;
    private RecyclerView thumbListView;
    private ImageView originalView;
    private Bitmap originalImage, tempImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_view);
        originalView = (ImageView) findViewById(R.id.originalImage);

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
                if(position == 0)
                    EditorView.this.imageDisplay();
                else
                    EditorView.this.applyFilter(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                ;
            }
        }));
        initImage();
    }
    private void initImage(){
        String currentPhotoPath = getIntent().getStringExtra("photoPath");

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW/300, photoH/400);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        originalImage = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageDisplay();
    }
    public void applyFilter(int position) {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int matrix[][] = filters.get(position).filterMatrix;
        int size = filters.get(position).size;
        int factor = filters.get(position).factor;
        int s2 = size/2;

        Bitmap temp = Bitmap.createBitmap(width, height, originalImage.getConfig());
        int A, R, G, B;
        int sumR, sumG, sumB;
        int[][] pixels = new int[size][size];

        for(int y = s2; y < height - s2; ++y) {
            for(int x = s2; x < width - s2; ++x) {
                for(int i = 0; i < size; ++i) {
                    for(int j = 0; j < size; ++j) {
                        pixels[i][j] = originalImage.getPixel(x + i - s2, y + j - s2);
                    }
                }
                A = Color.alpha(pixels[1][1]);
                sumR = 0;
                sumG = 0;
                sumB = 0;
                for(int i = 0; i < size; ++i) {
                    for(int j = 0; j < size; ++j) {
                        sumR += Color.red(pixels[i][j]) * matrix[i][j];
                        sumG += Color.green(pixels[i][j]) * matrix[i][j];
                        sumB += Color.blue(pixels[i][j]) * matrix[i][j];
                    }
                }

                R = sumR / factor;
                G = sumG / factor;
                B = sumB / factor;

                if(position == 6) {
                    R += 128;
                    G += 128;
                    B += 128;
                }

                if(R < 0) R = 0;
                else if(R > 255) R = 255;

                if(G < 0) G = 0;
                else if(G > 255) G = 255;

                if(B < 0) B = 0;
                else if(B > 255) B = 255;
                temp.setPixel(x , y, Color.argb(A, R, G, B));
            }
        }
        imageDisplay(temp);
        tempImage = temp;
    }

    public void imageDisplay(Bitmap image){
        originalView.setImageBitmap(image);
    }
    public void imageDisplay(){
        originalView.setImageBitmap(originalImage);
    }

    public List<Filter> createFilters() {
        List<Filter> filters = new ArrayList<>();
        int a[][] = new int[][] {
                {0, 0, 0},
                {0, 1, 0},
                {0, 0, 0}
        };
        filters.add(new Filter("Normal", a, 3, 1));

        a = new int[][] {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        filters.add(new Filter("Blur 3x3", a, 3, 9));

        a = new int[][] {
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        };
        filters.add(new Filter("Blur 5x5", a, 5, 25));

        a = new int[][] {
                {-1, -1, -1},
                {-1, 8, -1},
                {-1, -1, -1}
        };
        filters.add(new Filter("Edge Detect", a, 3, 1));

        a = new int[][] {
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
        filters.add(new Filter("Motion Blur", a, 3, 3));

        a = new int[][] {
                {-1, -1, -1},
                {-1,  9, -1},
                {-1, -1, -1}
        };
        filters.add(new Filter("Sharpen", a, 3, 1));

        a = new int[][] {
                {-1, -1,  0},
                {-1,  0,  1},
                {0,  1,  1}
        };
        filters.add(new Filter("Emboss", a, 3, 1));
        return filters;
    }
}