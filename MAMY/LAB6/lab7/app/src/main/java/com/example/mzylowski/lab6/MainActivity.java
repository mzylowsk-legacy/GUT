package com.example.mzylowski.lab6;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    ESSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new ESSurfaceView(this);
        setContentView(R.layout.activity_main);
        RelativeLayout tempView = (RelativeLayout) findViewById(R.id.activity_main);
        tempView.addView(surfaceView, 0);
        Button whiteButton = (Button) findViewById(R.id.white_color_button);
        whiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.renderer.lightColor = Color.WHITE;
            }
        });
        Button orangeButton = (Button) findViewById(R.id.orange_color_button);
        orangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.renderer.lightColor = Color.ORANGE;
            }
        });

        SeekBar directionSeekBar = (SeekBar) findViewById(R.id.light_direction_seek_bar);
        directionSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float value = (float)progress / 10.0f - 5.0f;
        Log.d("KSG", String.valueOf(value));
        surfaceView.renderer.z = value;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
