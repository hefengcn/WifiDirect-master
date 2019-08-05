// Copyright 2011 Google Inc. All Rights Reserved.

package com.tab.demo.mywifiderect.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class DataTransferService extends IntentService {
    private static final String TAG = "DataTransferService";

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_DATA = "com.example.android.wifidirect.senddata";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "group_owner_address";
    public static final String EXTRAS_GROUP_OWNER_PORT = "group_owner_port";

    public DataTransferService(String name) {
        super(name);
    }

    public DataTransferService() {
        super("DataTransferService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_DATA)) {

            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            Socket socket = new Socket();
            try {
                /**
                 * Binds the socket to a local address.
                 * If the address is null, then the system will pick up
                 * an ephemeral port and a valid local address to bind the socket.*/
                socket.bind(null);
                /*Connects this socket to the server with a specified timeout value.*/
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(TAG, "the connection state of the socket is" + socket.isConnected());
                /*returns an output stream to write data into this socket*/
                OutputStream stream = socket.getOutputStream();

                stream.write("hehehe".getBytes());
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
