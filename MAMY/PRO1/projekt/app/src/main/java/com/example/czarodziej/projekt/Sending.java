package com.example.czarodziej.projekt;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;


public class Sending extends AsyncTask<Void, Void, Void> {
    MyTaskParams params;

    public Sending(MyTaskParams params) {
        this.params = params;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        FTPClient con = null;
        try
        {
            con = new FTPClient();
            con.connect("138.68.108.101");
            if (con.login("root", "android"))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                Log.d("Wysyłanie pliku: ", params.file.getPath());
                String data = params.file.getPath();

                FileInputStream in = new FileInputStream(new File(data));
                boolean result = con.storeFile(params.azimuth + "_" + params.coords + "_" +
                        params.file.getLastPathSegment(), in);
                in.close();
                if (result){
                    params.activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(params.activity, "Plik pomyślnie wysłany :)", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            params.activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(params.activity, "Coś poszło nie tak :(", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
        return null;
    }
}
