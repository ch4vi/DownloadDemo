package com.ch4vi.downloaddemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String MESSAGE_PROGRESS = "message_progress";
    public static final String URL_EXTRA = "URL";
    public static final String PATH_EXTRA = "PATH";

    private static final int PERMISSION_REQUEST_CODE = 1;

    private String url;
    private String path;

    @Nullable
    ProgressBar mProgressBar;
    @Nullable
    TextView mProgressText;
    @Nullable
    Button mDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // /storage/emulated/0/Android/data/com.ch4vi.downloaddemo/files/lbp_reg_age.train.model
        path = getExternalFilesDir(null) + File.separator + "lbp_reg_age.train.model";

        // /storage/emulated/0/Download/lbp_reg_age.train.model
//        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "lbp_reg_age.train.model";

        url = "https://emotionresearchlab.com/downloads/urbano/lbp_reg_age.train.model";

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressText = findViewById(R.id.progress_text);
        mDownload = findViewById(R.id.download);
        if (mDownload != null)
        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile();
            }
        });



        registerReceiver();
    }

    public void downloadFile() {
        if (checkPermission()) {
            downloadResource(url, path);
        } else {
            requestPermission();
        }
    }

    private void downloadResource(@NonNull String url, @NonNull String path) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(MainActivity.URL_EXTRA, url);
        intent.putExtra(MainActivity.PATH_EXTRA, path);
        startService(intent);
    }

    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(MESSAGE_PROGRESS)) {
                Download download = intent.getParcelableExtra("download");
                mProgressBar.setProgress(download.getProgress());
                if (download.getProgress() == 100) {
                    mProgressText.setText(R.string.complete_download_message);
                } else {
                    mProgressText.setText(String.format(getString(R.string.download_progress_message), download.getCurrentFileSize(), download.getTotalFileSize()));
                }
            }
        }
    };

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadResource(url, path);
                } else {
                    Toast.makeText(this, "Permission Denied, Please allow to proceed !", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
