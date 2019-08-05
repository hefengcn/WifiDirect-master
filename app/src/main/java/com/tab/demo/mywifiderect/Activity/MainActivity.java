package com.tab.demo.mywifiderect.Activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tab.demo.mywifiderect.Adapter.MyAdapter;
import com.tab.demo.mywifiderect.BroadcastReceiver.WifiDirectBroadcastReceiver;
import com.tab.demo.mywifiderect.R;
import com.tab.demo.mywifiderect.Service.DataTransferService;
import com.tab.demo.mywifiderect.Service.FileTransferService;
import com.tab.demo.mywifiderect.Task.DataServerAsyncTask;
import com.tab.demo.mywifiderect.Task.FileServerAsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private Button discover;
    private Button stopDiscover;
    private Button stopConnect;
    private Button sendPicture;
    private Button sendData;
    private Button beGroupOwner;

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List peers = new ArrayList();
    private List<HashMap<String, String>> peersShow = new ArrayList();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private WifiP2pInfo mP2PInfo;

    private FileServerAsyncTask mServerTask;
    private DataServerAsyncTask mDataTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initIntentFilter();
        initReceiver();
        initEvents();
    }

    private void initView() {
        beGroupOwner = (Button) findViewById(R.id.bt_bgowner);
        stopDiscover = (Button) findViewById(R.id.bt_stopdiscover);
        discover = (Button) findViewById(R.id.bt_discover);
        stopConnect = (Button) findViewById(R.id.bt_stopconnect);
        sendPicture = (Button) findViewById(R.id.bt_sendpicture);
        sendData = (Button) findViewById(R.id.bt_senddata);
        sendPicture.setVisibility(View.GONE);
        sendData.setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new MyAdapter(peersShow);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager
                (this.getApplicationContext()));
    }

    private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initReceiver() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {
                peers.clear();
                peersShow.clear();
                Collection<WifiP2pDevice> deviceList = peersList.getDeviceList();
                peers.addAll(deviceList);

                for (int i = 0; i < deviceList.size(); i++) {
                    WifiP2pDevice device = (WifiP2pDevice) peers.get(i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", device.deviceName);
                    map.put("address", device.deviceAddress);
                    peersShow.add(map);
                }
                mAdapter = new MyAdapter(peersShow);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        CreateConnect(peersShow.get(position).get("address"),
                                peersShow.get(position).get("name"));

                    }

                    @Override
                    public void OnItemLongClick(View view, int position) {

                    }
                });
            }
        };

        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {

            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo info) {

                Log.i(TAG, "onConnectionInfoAvailable");
                mP2PInfo = info;
                TextView view = (TextView) findViewById(R.id.tv_main);
                if (mP2PInfo.groupFormed && mP2PInfo.isGroupOwner) {
                    Log.i(TAG, "owner start");

                    mServerTask = new FileServerAsyncTask(MainActivity.this, view);
                    mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    mDataTask = new DataServerAsyncTask(MainActivity.this, view);
                    mDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else if (mP2PInfo.groupFormed) {
                    SetButtonVisible();
                }
            }
        };
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListerner, mInfoListener);
    }

    private void initEvents() {

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscoverPeers();
            }
        });
        beGroupOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeGroupOwener();
            }
        });

        stopDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopDiscoverPeers();
            }
        });
        stopConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopConnect();
            }
        });
        sendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 20);

            }
        });

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this,
                        DataTransferService.class);

                serviceIntent.setAction(DataTransferService.ACTION_SEND_FILE);

                serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        mP2PInfo.groupOwnerAddress.getHostAddress());
                Log.i("address", "owenerip is " + mP2PInfo.groupOwnerAddress.getHostAddress());
                serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_PORT,
                        8888);
                MainActivity.this.startService(serviceIntent);
            }
        });


        mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                CreateConnect(peersShow.get(position).get("address"),
                        peersShow.get(position).get("name"));
            }

            @Override
            public void OnItemLongClick(View view, int position) {
            }
        });
    }

    private void BeGroupOwener() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20) {
            super.onActivityResult(requestCode, resultCode, data);
            Uri uri = data.getData();
            Intent serviceIntent = new Intent(MainActivity.this,
                    FileTransferService.class);

            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
                    uri.toString());

            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    mP2PInfo.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                    8988);
            MainActivity.this.startService(serviceIntent);
        }
    }

    private void StopConnect() {
        SetButtonGone();
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    /*A demo base on API which you can connect android device by wifidirect,
    and you can send file or data by socket,what is the most important is that you can set
    which device is the client or service.*/

    private void CreateConnect(String address, final String name) {
        WifiP2pDevice device;
        WifiP2pConfig config = new WifiP2pConfig();
        Log.i(TAG, address);

        config.deviceAddress = address;
        /*mac地址*/

        config.wps.setup = WpsInfo.PBC;
        Log.i("address", "MAC IS " + address);
        if (address.equals("9a:ff:d0:23:85:97")) {
            config.groupOwnerIntent = 0;
            Log.i("address", "lingyige shisun");
        }
        if (address.equals("36:80:b3:e8:69:a6")) {
            config.groupOwnerIntent = 15;
            Log.i("address", "lingyigeshiwo");

        }

        Log.i("address", "lingyige youxianji" + String.valueOf(config.groupOwnerIntent));

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {


            }
        });
    }

    private void StopDiscoverPeers() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {


            }
        });
    }


    private void SetButtonVisible() {
        sendPicture.setVisibility(View.VISIBLE);
        sendData.setVisibility(View.VISIBLE);
    }

    private void SetButtonGone() {
        sendPicture.setVisibility(View.GONE);
        sendData.setVisibility(View.GONE);
    }


    private void DiscoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "hehehehehe");
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StopConnect();
    }

    public void ResetReceiver() {

        unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, mFilter);

    }
}
