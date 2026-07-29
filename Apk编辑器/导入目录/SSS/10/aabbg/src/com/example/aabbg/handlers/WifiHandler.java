package com.example.aabbg.handlers;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

import java.util.List;

public class WifiHandler {
    private Context context;
    private TextView txtWifiResults;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiReceiver;

    public WifiHandler(Context ctx, TextView txtWifiResults) {
        this.context = ctx;
        this.txtWifiResults = txtWifiResults;
        wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        registerReceiver();
    }

    private void registerReceiver() {
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifiManager.getScanResults();
                StringBuilder sb = new StringBuilder();
                for (ScanResult r : results) {
                    sb.append(r.SSID).append(" (" + r.level + ")\n");
                }
                txtWifiResults.setText(sb.toString());
            }
        };
        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void startWifiScan() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
    }

    public void stop() {
        try { context.unregisterReceiver(wifiReceiver); } catch (Exception e) {}
    }
}
