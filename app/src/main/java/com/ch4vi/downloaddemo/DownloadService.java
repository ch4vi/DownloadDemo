package com.ch4vi.downloaddemo;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class DownloadService extends IntentService {

    public DownloadService() {
        super("Download Service");
    }

    private int totalFileSize;

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(MainActivity.URL_EXTRA);
        String path = intent.getStringExtra(MainActivity.PATH_EXTRA);
        if (url == null || path == null) {
            Log.e("Service", "path: " + path + "or url: " + url + " are null");
            return;
        }

        initDownload(url, path);
    }

    private void initDownload(String url, String path) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://google.com")
                .build();

        ApiInterface retrofitInterface = retrofit.create(ApiInterface.class);
        Call<ResponseBody> request = retrofitInterface.downloadFile(url);

        try {
            ResponseBody body = request.execute().body();
            if (body == null){
                Log.e("Service","body is null");
                return;
            }
            downloadFile(body, path);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(ResponseBody body, String path) throws IOException {
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(path);
        OutputStream output = new FileOutputStream(outputFile);

        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {
            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {
                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                sendIntent(download);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete();
        output.flush();
        output.close();
        bis.close();
    }

    private void sendIntent(Download download) {
        Intent intent = new Intent(MainActivity.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete() {
        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("Service", "Task removed");
    }

}
