package com.example.user.mywifidirectclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener,WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "mywifidirect";
    private WifiP2pManager manager;
     boolean isWifiP2pEnabled = false;
     boolean isConnected = false;
    private ClientConnection clientConnectionThread;
    private boolean retryChannel = false;
LinearLayout kamerasSkats;
    AttelaSkats attelaSkats;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
private WifiP2pInfo connectionInfo;
    private TextView textView;
    SeekBar seekBar;
    SeekBar framerateSeekbar;
    double incomingByteCount =0;
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        kamerasSkats = (LinearLayout) findViewById(R.id.kamerasSkats);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        attelaSkats = new AttelaSkats(this, 480);
        kamerasSkats.addView(attelaSkats);
        textView = (TextView)findViewById(R.id.textView);
        textView.bringToFront();
seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarListener);
seekBar.bringToFront();
framerateSeekbar = (SeekBar)findViewById(R.id.seekBar3);
        framerateSeekbar.bringToFront();
    framerateSeekbar.setMax(20);
        framerateSeekbar.setOnSeekBarChangeListener(framerateBarListener);

    }
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

    }
@Override
public void onDestroy(){

    if(clientConnectionThread!=null) {
        Log.d(TAG,"trying to close Client sockets");

        try {
            clientConnectionThread.clientSocket.close();
            clientConnectionThread.sendingSocket.close();
        Log.d(TAG,"Client sockets closed");
        } catch (Exception e) {
            // Give up
            e.printStackTrace();
        }
        clientConnectionThread.isRunning = false;

    }
    super.onDestroy();
}
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.atn_direct_discover:
                discover();
                return true;
            case R.id.atn_direct_connect:
                connect();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
void discover(){
    if (!isWifiP2pEnabled) {
        Toast.makeText(MainActivity.this,"wifi off",
                Toast.LENGTH_SHORT).show();

    }
else {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
this.peers.clear();
        for(WifiP2pDevice device: peers.getDeviceList()) {
  this.peers.add(device);
    textView.setText(  device.deviceName);
    Log.d(TAG, "Device name: " + device.deviceName);
    Log.d(TAG, "Primary type: " + device.primaryDeviceType);
    Log.d(TAG, "Secondary type: " + device.secondaryDeviceType);
    Log.d(TAG, "Is owner: " + device.isGroupOwner());
    Log.d(TAG, "Adress: " + device.deviceAddress);

}    }
SeekBar.OnSeekBarChangeListener framerateBarListener = new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        synchronized (clientConnectionThread.sendingThread) {
            if(clientConnectionThread.dataToSend!=null){
                clientConnectionThread.dataToSend.set(1,(byte)progress);
                clientConnectionThread.dataUpdated=true;

            }
        }
        textView.setText("FrameRate Divider: " + progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
};
    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            synchronized (clientConnectionThread.sendingThread) {
                if(clientConnectionThread.dataToSend!=null){
                clientConnectionThread.dataToSend.set(0,(byte)progress);
           clientConnectionThread.dataUpdated=true;
            }
            }
            textView.setText("Jpeg Quality: " + progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    public void connect() {
        // Picking the first device found on the network.
        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        isConnected = true;
        Log.d(TAG, "connectionInfoAvailable, owner adress:  " + info.groupOwnerAddress);
        Log.d(TAG, "connectionInfoAvailable, this device owner :  " + info.isGroupOwner);
        Log.d(TAG, "connectionInfoAvailable, group formed:  " + info.groupFormed);
        if (info.groupFormed && info.isGroupOwner) {
           textView.setText("this device is owner");

        } else if (info.groupFormed) {
if(clientConnectionThread==null){
           connectionInfo = info;
clientConnectionThread = new ClientConnection(mHandler,info);
        clientConnectionThread.start();}
        }else {
            discover();//start auto discover, ja nav savienojuma
        }
    }
    private Handler mHandler = new Handler() {//a ui tredaa
        @Override
        public void handleMessage(Message msg) {
            if ((msg.getData().getString("msg")).compareTo("kadrs") != 0) {
               // statusText.append(msg.getData().getString("msg"));
            } else
//
            {

                attelaSkats.mSetBitmap(clientConnectionThread.getBitmap());
                incomingByteCount += msg.getData().getInt(null);
               textView.setText(incomingByteCount / 1000000 + " Mb");
            }
//skats.invalidate();
        }
    };


}

