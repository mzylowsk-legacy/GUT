package com.example.czarodziej.projekt;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int CAMERA_CAPTURE_IMAGE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private static final String IMAGE_DIRECTORY_NAME = "MAM_Projekt";

    private Uri fileUri;

    private ImageView imgPreview;
    private Button btnCapturePicture, btnSetAzimuth, btnSetCoords, btnSetFilter, letsSendButton;
    private TextView azimuthValue, lonValue, latValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        btnSetAzimuth = (Button) findViewById(R.id.btnSetAzimuth);
        btnSetCoords = (Button) findViewById(R.id.btnSetCoords);
        letsSendButton = (Button) findViewById(R.id.btnSend);
        azimuthValue = (TextView) findViewById(R.id.azimuth);
        lonValue = (TextView) findViewById(R.id.lon);
        latValue = (TextView) findViewById(R.id.lat);
        btnSetFilter = (Button) findViewById(R.id.btnSetFilter);

        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
        btnSetAzimuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAzimuth();
            }
        });
        btnSetCoords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCoords();
            }
        });
        btnSetFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilter();
            }
        });
        letsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        });
        
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void sendFile() {
        MyTaskParams params = new MyTaskParams(fileUri, this, (String) this.azimuthValue.getText(),
                this.latValue.getText() + "_" + this.lonValue.getText());
        new Sending(params).execute();
    }

    private void showFilter() {
        Intent intent = new Intent(this, opencv_Activity.class);
        startActivity(intent);
    }

    private void setCoords() {
        Intent intent = new Intent(this, GPS_Location_Activity.class);
        startActivityForResult(intent, 56);
    }

    private void setAzimuth() {
        Intent intent = new Intent(this, SensorMakerActivity.class);
        startActivityForResult(intent, 23);
    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_IMAGE) {
            if (resultCode == RESULT_OK) {
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if (requestCode == 23 && resultCode == RESULT_OK && data != null) {
            this.azimuthValue.setText(data.getStringExtra("AZYMUT"));
        }

        if (requestCode == 56 && resultCode == RESULT_OK && data != null) {
            this.lonValue.setText(data.getStringExtra("LONG"));
            this.latValue.setText(data.getStringExtra("LAT"));
        }
    }

    private void previewCapturedImage() {
        try {

            imgPreview.setVisibility(View.VISIBLE);

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}