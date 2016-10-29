package com.example.czarodziej.projekt;

import android.app.Activity;
import android.net.Uri;

public class MyTaskParams {
    Uri file;
    Activity activity;
    String azimuth;
    String coords;

    MyTaskParams(Uri file, Activity activity, String azimuth, String coords) {
        this.file = file;
        this.activity = activity;
        this.azimuth = azimuth;
        this.coords = coords;
    }
}