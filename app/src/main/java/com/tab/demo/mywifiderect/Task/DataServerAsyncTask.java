package com.tab.demo.mywifiderect.Task;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tab.demo.mywifiderect.Activity.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class DataServerAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "DataServerAsyncTask";

    private TextView statusText;
    private MainActivity activity;

    public DataServerAsyncTask(MainActivity activity, View statusText) {
        this.statusText = (TextView) statusText;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            Log.i(TAG, "doInBackground");
            ServerSocket serverSocket = new ServerSocket(8888);

            Log.i(TAG, "ServerSocket has been created");
            Socket client = serverSocket.accept();
            Log.i(TAG, "ServerSocket has accepted");
            InputStream inputstream = client.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i;
            while ((i = inputstream.read()) != -1) {
                baos.write(i);
            }

            String str = baos.toString();
            serverSocket.close();
            return str;

        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }


    @Override
    protected void onPostExecute(String result) {

        Log.i(TAG, "onPostExecute");

        Toast.makeText(activity, "result" + result, Toast.LENGTH_SHORT).show();

        if (result != null) {
            statusText.setText("Data-String is " + result);
        }
    }

    @Override
    protected void onPreExecute() {

    }

}