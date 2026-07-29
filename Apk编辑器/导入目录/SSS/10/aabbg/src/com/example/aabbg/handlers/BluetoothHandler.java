package com.example.aabbg.handlers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

public class BluetoothHandler {
    private Context context;
    private TextView txtBluetoothResults;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;

    public BluetoothHandler(Context ctx, TextView txtBluetoothResults) {
        this.context = ctx;
        this.txtBluetoothResults = txtBluetoothResults;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceiver();
    }

    private void registerReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String prev = txtBluetoothResults.getText().toString();
                    txtBluetoothResults.setText(prev + device.getName() + " (" + device.getAddress() + ")\n");
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
    }

    public void startBluetoothScan() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        bluetoothAdapter.startDiscovery();
    }

    public void stop() {
        try { context.unregisterReceiver(receiver); } catch (Exception e) {}
    }
}
