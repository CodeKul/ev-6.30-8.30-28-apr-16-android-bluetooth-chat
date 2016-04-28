package com.codekul.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_BT_DISCOVERABLE = 1002;
    public int REQUEST_ENABLE_BT = 1001;

    BroadcastReceiver receiverFound = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("@codekul", "Name - " + device.getName());
                Log.i("@codekul", "Address - " + device.getAddress());
                Log.i("@codekul", "--------------------------");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        findViewById(R.id.btnBondedDevices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> setBtDevices = adapter.getBondedDevices();
                for (BluetoothDevice btDevice : setBtDevices) {
                    Log.i("@codekul", "Name - " + btDevice.getName());
                    Log.i("@codekul", "Address - " + btDevice.getAddress());
                    Log.i("@codekul", "-------------------");
                }
            }
        });

        findViewById(R.id.btnScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.startDiscovery();
            }
        });

        findViewById(R.id.btnDiscovability).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivityForResult(discoverableIntent, REQUEST_BT_DISCOVERABLE);
            }
        });

        findViewById(R.id.btnBTServer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                                /*(1). listen*/
                            BluetoothServerSocket bluetoothServerSocket = adapter.listenUsingRfcommWithServiceRecord("btchat", UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));

                    /*(2). accept incoming*/
                            BluetoothSocket bluetoothSocket = bluetoothServerSocket.accept();

                    /*(3) read/write to stream*/
                            DataOutputStream dos = new DataOutputStream(bluetoothSocket.getOutputStream());
                            dos.writeUTF("Hello Bt");

                    /*(4). termination of connection*/
                            dos.close();
                            bluetoothSocket.close();
                            bluetoothServerSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.btnBTClient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BluetoothDevice btDevice = adapter.getRemoteDevice("EC:88:92:AA:AC:D4");

                    BluetoothSocket btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));

                    btSocket.connect();

                    DataInputStream dis = new DataInputStream(btSocket.getInputStream());
                    ((TextView) findViewById(R.id.textBtData)).setText("" + dis.readUTF());

                    dis.close();
                    btSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        registerReceiver(receiverFound, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private void initBluetooth(final BluetoothAdapter adapter) {

        if (adapter == null) {
            Toast.makeText(this, "Bluetooth Not Available", Toast.LENGTH_LONG).show();
            return;
        }

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.i("@codekul", "Bluetooth Enabled");
            } else {
                Log.i("@codekul", "Bluetooth Disabled");
            }
        }
        if (requestCode == REQUEST_BT_DISCOVERABLE) {
            if (resultCode == RESULT_OK) {
                Log.i("@codekul", "Bluetooth is Visible");
            } else {
                Log.i("@codekul", "Bluetooth is not visible");
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiverFound);
        super.onDestroy();
    }
}
