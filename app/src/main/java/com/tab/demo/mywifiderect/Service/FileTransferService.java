package com.tab.demo.mywifiderect.Service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.tab.demo.mywifiderect.Task.FileServerAsyncTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {
    private static final String TAG = "FileTransferService";
    private static final int SOCKET_TIMEOUT = 5000;//milliseconds
    public static final String ACTION_SEND_FILE = "com.tab.demo.wifidirect.sendfile";
    public static final String EXTRAS_FILE_PATH = "file_path";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "group_owner_address";
    public static final String EXTRAS_GROUP_OWNER_PORT = "group_owner_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String address = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            Socket socket = new Socket();
            try {
                /**
                 * Binds the socket to a local address.
                 * If the address is null, then the system will pick up
                 * an ephemeral port and a valid local address to bind the socket.*/
                socket.bind(null);
                /*Connects this socket to the server with a specified timeout value.*/
                socket.connect((new InetSocketAddress(address, port)), SOCKET_TIMEOUT);

                Log.d(TAG, "the connection state of the socket is" + socket.isConnected());

                /*returns an output stream to write data into this socket*/
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(TAG, e.toString());
                }
                FileServerAsyncTask.copyFile(is, stream);
                Log.d(TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
