package com.example.shv.imageeditor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.shv.imageeditor.MainActivity.selectedImage;

public class EditorView extends AppCompatActivity {
    public Button apply, save;
    public int redV, greenV, blueV;
    public int isInc;
    public boolean bnw;
    private boolean changed_rbgVs;
    private List<Filter> filters;
    private RecyclerView thumbListView;
    private ImageView originalView;
    private Bitmap originalImage, tempImage;
    private SeekBar redBar, greenBar, blueBar;
    private CardView colorAdjust;
    private LinearLayoutManager lm;
    private int redMax, redMin, greenMax, greenMin, blueMax, blueMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_view);
        originalView = (ImageView) findViewById(R.id.originalImage);
        apply = (Button) findViewById(R.id.Apply);
        save = (Button) findViewById(R.id.Save);
        redBar = (SeekBar) findViewById(R.id.redBar);
        greenBar = (SeekBar) findViewById(R.id.greenBar);
        blueBar = (SeekBar) findViewById(R.id.blueBar);
        colorAdjust = (CardView) findViewById(R.id.colorAdjust);

        filters = createFilters();
        thumbListView = (RecyclerView) findViewById(R.id.filterRecyclerView);
        RecyclerAdapter adapter = new RecyclerAdapter(filters, getApplication());
        thumbListView.setAdapter(adapter);

        lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);

        thumbListView.setLayoutManager(lm);
        thumbListView.setHasFixedSize(true);
        thumbListView.addOnItemTouchListener(new CustomRVItemTouchListener(this, thumbListView, new RecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position == 0) {
                    colorAdjust.setVisibility(View.INVISIBLE);
                    EditorView.this.imageDisplay();
                    EditorView.this.apply.setEnabled(false);
                } else if (position == 11) {
                    colorAdjust.setVisibility(View.VISIBLE);
                    redV = blueV = greenV = 0;
                    EditorView.this.blackNWhite();
                    redBar.setMax(1);
                    redBar.setProgress(0);

                    greenBar.setMax(1);
                    greenBar.setProgress(0);

                    blueBar.setMax(1);
                    blueBar.setProgress(0);
                    EditorView.this.bnw = true;
                    EditorView.this.apply.setEnabled(true);
                } else if (position == 12) {
                    EditorView.this.imageDisplay();
                    EditorView.this.tempImage = Bitmap.createBitmap(originalImage);
                    EditorView.this.findMin();
                    EditorView.this.isInc = 1;
                    redBar.setMax(255 - redMin);
                    redBar.setProgress(0);

                    greenBar.setMax(255 - greenMin);
                    greenBar.setProgress(0);

                    blueBar.setMax(255 - blueMin);
                    blueBar.setProgress(0);

                    EditorView.this.bnw = false;
                    colorAdjust.setVisibility(View.VISIBLE);
                } else if (position == 13) {
                    EditorView.this.imageDisplay();
                    EditorView.this.tempImage = Bitmap.createBitmap(originalImage);
                    EditorView.this.findMax();
                    EditorView.this.isInc = -1;
                    redBar.setMax(redMax);
                    redBar.setProgress(redMax);

                    greenBar.setMax(greenMax);
                    greenBar.setProgress(greenMax);

                    blueBar.setMax(blueMax);
                    blueBar.setProgress(blueMax);

                    EditorView.this.bnw = false;
                    colorAdjust.setVisibility(View.VISIBLE);
                } else {
                    colorAdjust.setVisibility(View.INVISIBLE);
                    EditorView.this.applyFilter(position);
                    EditorView.this.apply.setEnabled(true);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                EditorView.this.redV = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (bnw) {
                    EditorView.this.blackNWhite();
                } else {
                    if (isInc == -1)
                        redV = 255 - redV;
                    EditorView.this.apply.setEnabled(true);
                    EditorView.this.colorViewR();
                }
            }
        });
        greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                EditorView.this.greenV = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (bnw) {
                    EditorView.this.blackNWhite();
                } else {
                    if (isInc == -1)
                        greenV = 255 - greenV;
                    EditorView.this.colorViewG();
                    EditorView.this.apply.setEnabled(true);
                }
            }
        });
        blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                EditorView.this.blueV = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (bnw) {
                    EditorView.this.blackNWhite();
                } else {
                    if (isInc == -1)
                        blueV = 255 - blueV;
                    EditorView.this.colorViewB();
                    EditorView.this.apply.setEnabled(true);
                }
            }
        });
        initImage();
    }

    public void findMax() {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int pixel, c;
        redMax = greenMax = blueMax = 0;
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j) {
                pixel = originalImage.getPixel(i, j);

                c = Color.red(pixel);
                if (c > redMax)
                    redMax = c;

                c = Color.green(pixel);
                if (c > greenMax)
                    greenMax = c;

                c = Color.blue(pixel);
                if (c > blueMax)
                    blueMax = c;
            }
    }

    public void findMin() {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int pixel, c;
        blueMin = greenMin = redMin = 1000;
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j) {
                pixel = originalImage.getPixel(i, j);

                c = Color.red(pixel);
                if (c < redMin)
                    redMin = c;

                c = Color.green(pixel);
                if (c < greenMin)
                    greenMin = c;

                c = Color.blue(pixel);
                if (c < blueMin)
                    blueMin = c;
            }
    }

    public void blackNWhite() {
        if (redV != 1 && blueV != 1 && greenV != 1) {
            redV = blueV = greenV = 1;
            changed_rbgVs = true;
        } else
            changed_rbgVs = false;
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int pixel, val;

        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j) {
                pixel = originalImage.getPixel(i, j);
                val = (Color.red(pixel) * redV + Color.green(pixel) * greenV + Color.blue(pixel) * blueV)
                        / (redV + blueV + greenV);
                tempImage.setPixel(i, j, Color.argb(Color.alpha(pixel), val, val, val));
            }
        imageDisplay(tempImage);
        if (changed_rbgVs)
            redV = blueV = greenV = 0;
    }

    public void colorViewR() {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int pixel, a, r, g, b;
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j) {
                pixel = tempImage.getPixel(i, j);
                a = Color.alpha(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);
                r = Color.red(originalImage.getPixel(i, j));
                r = r + (isInc * redV);
                if (r > 255)
                    r = 255;
                if (r < 0)
                    r = 0;
                tempImage.setPixel(i, j, Color.argb(a, r, g, b));
            }
        imageDisplay(tempImage);
    }

    public void colorViewG() {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int pixel, a, r, g, b;
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j) {
                pixel = tempImage.getPixel(i, j);
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                b = Color.blue(pixel);
                g = Color.green(originalImage.getPixel(i, j));
                g = g + (isInc * greenV);
                if (g > 255)
                    g = 255;
                if (g < 0)
                    g = 0;
                tempImage.setPixel(i, j, Color.argb(a, r, g, b));
            }
        imageDisplay(tempImage);
    }

    public void colorViewB() {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int pixel, a, r, g, b;
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j) {
                pixel = tempImage.getPixel(i, j);
                a = Color.alpha(pixel);
                g = Color.green(pixel);
                r = Color.red(pixel);
                b = Color.blue(originalImage.getPixel(i, j));
                b = b + (isInc * blueV);
                if (b > 255)
                    b = 255;
                if (b < 0)
                    b = 0;
                tempImage.setPixel(i, j, Color.argb(a, r, g, b));
            }
        imageDisplay(tempImage);
    }

    private void initImage() {
        if (getIntent().getStringExtra("option").equals("1")) {
            String currentPhotoPath = getIntent().getStringExtra("photoPath");

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            int scaleFactor = Math.min(photoW / 300, photoH / 400);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            originalImage = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            imageDisplay();
        } else {
            Bitmap bmp = null;
            try {
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            originalImage = Bitmap.createBitmap(bmp);
            tempImage = Bitmap.createBitmap(originalImage);
            imageDisplay();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();


        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();

        int height = image.getHeight();
        int width = image.getWidth();
        double scale = Math.min(300.0 / width, 400.0 / height);

        Matrix mat = new Matrix();
        mat.postScale((float) scale, (float) scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height, mat, false);
        return resizedBitmap;
    }

    public void applyFilter(int position) {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int matrix[][] = filters.get(position).filterMatrix;
        int size = filters.get(position).size;
        int factor = filters.get(position).factor;
        int s2 = size / 2;

        Bitmap temp = Bitmap.createBitmap(width, height, originalImage.getConfig());
        int A, R, G, B;
        int sumR, sumG, sumB;
        int[][] pixels = new int[size][size];

        for (int y = s2; y < height - s2; ++y) {
            for (int x = s2; x < width - s2; ++x) {
                for (int i = 0; i < size; ++i) {
                    for (int j = 0; j < size; ++j) {
                        pixels[i][j] = originalImage.getPixel(x + i - s2, y + j - s2);
                    }
                }
                A = Color.alpha(pixels[1][1]);
                sumR = 0;
                sumG = 0;
                sumB = 0;
                for (int i = 0; i < size; ++i) {
                    for (int j = 0; j < size; ++j) {
                        sumR += Color.red(pixels[i][j]) * matrix[i][j];
                        sumG += Color.green(pixels[i][j]) * matrix[i][j];
                        sumB += Color.blue(pixels[i][j]) * matrix[i][j];
                    }
                }

                R = sumR / factor;
                G = sumG / factor;
                B = sumB / factor;

                if (position == 6) {
                    R += 128;
                    G += 128;
                    B += 128;
                }

                if (R < 0) R = 0;
                else if (R > 255) R = 255;

                if (G < 0) G = 0;
                else if (G > 255) G = 255;

                if (B < 0) B = 0;
                else if (B > 255) B = 255;
                temp.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        imageDisplay(temp);
        tempImage = temp;
    }

    public void imageDisplay(Bitmap image) {
        originalView.setImageBitmap(image);
    }

    public void imageDisplay() {
        originalView.setImageBitmap(originalImage);
    }

    public void applyChanges(View view) {
        originalImage = Bitmap.createBitmap(tempImage);
        save.setEnabled(true);
        apply.setEnabled(false);
        lm.scrollToPosition(0);
        colorAdjust.setVisibility(View.INVISIBLE);
    }

    public void saveChanges(View view) {
        applyChanges(view);
        if (storagePermissionGranted()) {
            storeImage();
        }
    }

    public boolean storagePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
                return true;
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            EditorView.this.storeImage();
        else
            Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_LONG).show();
    }


    private void storeImage() {
        Bitmap image = Bitmap.createBitmap(originalImage);
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Toast.makeText(getApplicationContext()
                    , "Error creating media file, check storage permissions: ", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext()
                    , "File not found: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext()
                    , "Error accessing file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(getApplicationContext(), "Image saved in " + pictureFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        save.setEnabled(false);
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Pictures");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public List<Filter> createFilters() {
        List<Filter> filters = new ArrayList<>();
        int a[][] = new int[][]{
                {0, 0, 0},
                {0, 1, 0},
                {0, 0, 0}
        };
        filters.add(new Filter("Normal", a, 3, 1));

        a = new int[][]{
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };
        filters.add(new Filter("Gauss Blur 3x3", a, 3, 9));

        a = new int[][]{
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        };
        filters.add(new Filter("Gauss Blur 5x5", a, 5, 25));

        a = new int[][]{
                {-1, -1, -1},
                {-1, 8, -1},
                {-1, -1, -1}
        };
        filters.add(new Filter("Edge Detect", a, 3, 1));

        a = new int[][]{
                {1, 0, 0, 0, 0},
                {0, 1, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 1, 0},
                {0, 0, 0, 0, 1}
        };
        filters.add(new Filter("Motion Blur", a, 5, 5));

        a = new int[][]{
                {-1, -1, -1},
                {-1, 9, -1},
                {-1, -1, -1}
        };
        filters.add(new Filter("Sharpen", a, 3, 1));

        a = new int[][]{
                {-1, -1, 0},
                {-1, 0, 1},
                {0, 1, 1}
        };
        filters.add(new Filter("Emboss", a, 3, 1));

        a = new int[][]{
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
        filters.add(new Filter("Sobel H", a, 3, 1));

        a = new int[][]{
                {-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}
        };
        filters.add(new Filter("Sobel V", a, 3, 1));

        a = new int[][]{
                {-1, 0, 1},
                {-1, 0, 1},
                {-1, 0, 1}
        };
        filters.add(new Filter("Prewitt H", a, 3, 1));

        a = new int[][]{
                {-1, -1, -1},
                {0, 0, 0},
                {1, 1, 1}
        };
        filters.add(new Filter("Prewitt V", a, 3, 1));


        a = new int[][]{
                {1, 1},
                {1, 1}
        };
        filters.add(new Filter("Black n White", a, 2, 4));

        a = new int[][]{
                {1, 1},
                {1, 1}
        };
        filters.add(new Filter("Color inc", a, 2, 4));

        a = new int[][]{
                {1, 1},
                {1, 1}
        };
        filters.add(new Filter("Color dec", a, 2, 4));
        return filters;
    }
}