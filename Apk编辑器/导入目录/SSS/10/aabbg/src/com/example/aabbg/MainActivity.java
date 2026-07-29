package com.example.aabbg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aabbg.handlers.*;
import com.example.aabbg.utils.PermissionUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private TextView txtLocation;
    private TextView txtSystemInfo;
    private TextView txtSensorData;
    private TextView txtWifiResults;
    private TextView txtBluetoothResults;
    private TextView txtRecordStatus;
    private SurfaceView surfaceViewFront;
    private SurfaceView surfaceViewBack;
    private EditText vibrateInput;
    private EditText brightnessInput;
    private EditText frontZoomInput;
    private EditText backZoomInput;
    private Button btnVibrateToggle;

    private LocationHandler locationHandler;
    private SensorHandler sensorHandler;
    private WifiHandler wifiHandler;
    private BluetoothHandler bluetoothHandler;
    private CameraHandler cameraHandler;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFilePath;
    private boolean isRecording = false;
    private Vibrator vibrator;
    private boolean isVibrating = false;

    private static final String SAVE_PATH = "/storage/emulated/0/11/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        createSaveDirectory();

        if (PermissionUtil.checkPermissions(this)) {
            initViews();
            initHandlers();
            setupAudioPath();
        }
    }

    private void createSaveDirectory() {
        File directory = new File(SAVE_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private void startVibrating(long intensity) {
        if (vibrator != null && !isVibrating) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, intensity}, 
                    new int[]{0, VibrationEffect.DEFAULT_AMPLITUDE},
                    0)); // 0 means repeat indefinitely
            } else {
                vibrator.vibrate(new long[]{0, intensity}, 0);
            }
            isVibrating = true;
            btnVibrateToggle.setText("停止振动");
        }
    }

    private void stopVibrating() {
        if (vibrator != null && isVibrating) {
            vibrator.cancel();
            isVibrating = false;
            btnVibrateToggle.setText("开始振动");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (cameraHandler != null) cameraHandler.releaseCamera();
    }

    private void initViews() {
        txtLocation = findViewById(R.id.txtLocation);
        txtSystemInfo = findViewById(R.id.txtSystemInfo);
        txtSensorData = findViewById(R.id.txtSensorData);
        txtWifiResults = findViewById(R.id.txtWifiResults);
        txtBluetoothResults = findViewById(R.id.txtBluetoothResults);
        txtRecordStatus = findViewById(R.id.txtRecordStatus);
        surfaceViewFront = findViewById(R.id.surfaceViewFront);
        surfaceViewBack = findViewById(R.id.surfaceViewBack);
        
        vibrateInput = findViewById(R.id.vibrateInput);
        brightnessInput = findViewById(R.id.brightnessInput);
        frontZoomInput = findViewById(R.id.frontZoomInput);
        backZoomInput = findViewById(R.id.backZoomInput);
        btnVibrateToggle = findViewById(R.id.btnVibrateToggle);

        surfaceViewFront.getHolder().addCallback(this);
        surfaceViewBack.getHolder().addCallback(this);

        // 振动控制
        btnVibrateToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVibrating) {
                    try {
                        long intensity = Long.parseLong(vibrateInput.getText().toString());
                        startVibrating(intensity);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "请输入有效的振动强度", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopVibrating();
                }
            }
        });

        // 亮度控制
        findViewById(R.id.btnSetBrightness).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int brightness = Integer.parseInt(brightnessInput.getText().toString());
                    setScreenBrightness(brightness);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "请输入有效的亮度值(0-255)", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 前摄像头变焦
        findViewById(R.id.btnSetFrontZoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    float zoom = Float.parseFloat(frontZoomInput.getText().toString());
                    cameraHandler.setFrontZoom(zoom);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "请输入有效的变焦倍数", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 后摄像头变焦
        findViewById(R.id.btnSetBackZoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    float zoom = Float.parseFloat(backZoomInput.getText().toString());
                    cameraHandler.setBackZoom(zoom);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "请输入有效的变焦倍数", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 相机控制
        findViewById(R.id.btnStartBothCameras).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHandler.startBothCameras(surfaceViewFront.getHolder(), 
                    surfaceViewBack.getHolder());
            }
        });

        findViewById(R.id.btnTakePhotoFront).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHandler.takeFrontPhoto();
            }
        });

        findViewById(R.id.btnTakePhotoBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHandler.takeBackPhoto();
            }
        });

        findViewById(R.id.btnTakeBothPhotos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHandler.takeBothPhotos();
            }
        });

        findViewById(R.id.btnFlash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHandler.toggleFlash();
            }
        });

        findViewById(R.id.btnStartRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        findViewById(R.id.btnStopRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        findViewById(R.id.btnPlayRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecording();
            }
        });

        findViewById(R.id.btnGetLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationHandler.startLocationUpdates();
            }
        });

        findViewById(R.id.btnWifiScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiHandler.startWifiScan();
            }
        });

        findViewById(R.id.btnBluetoothScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothHandler.startBluetoothScan();
            }
        });

        findViewById(R.id.btnRefreshInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSystemInfo();
            }
        });
    }

    private void initHandlers() {
        locationHandler = new LocationHandler(this, txtLocation);
        sensorHandler = new SensorHandler(this, txtSensorData);
        wifiHandler = new WifiHandler(this, txtWifiResults);
        bluetoothHandler = new BluetoothHandler(this, txtBluetoothResults);
        cameraHandler = new CameraHandler(this);
    }

    private void setupAudioPath() {
        audioFilePath = SAVE_PATH + "record_" + getCurrentTimeString() + ".mp3";
    }

    private void refreshSystemInfo() {
        StringBuilder info = new StringBuilder();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        try {
            info.append("===== 系统信息 =====\n");
            info.append("制造商: ").append(android.os.Build.MANUFACTURER).append("\n");
            info.append("型号: ").append(android.os.Build.MODEL).append("\n");
            info.append("Android版本: ").append(android.os.Build.VERSION.RELEASE).append("\n");
            info.append("API级别: ").append(android.os.Build.VERSION.SDK_INT).append("\n\n");

            info.append("===== 网络信息 =====\n");
            if (tm != null) {
                if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                    info.append("运营商: ").append(tm.getNetworkOperatorName()).append("\n");
                    info.append("网络类型: ").append(getNetworkType(tm.getNetworkType())).append("\n");
                    info.append("SIM卡状态: ").append(getSimState(tm.getSimState())).append("\n");
                } else {
                    info.append("需要READ_PHONE_STATE权限\n");
                }
            }

            txtSystemInfo.setText(info.toString());
        } catch (SecurityException e) {
            Toast.makeText(this, "需要系统权限", Toast.LENGTH_SHORT).show();
        }
    }

    private String getNetworkType(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            case 20:  // TelephonyManager.NETWORK_TYPE_NR
                return "5G";
            default:
                return "未知";
        }
    }

    private String getSimState(int state) {
        switch (state) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return "无SIM卡";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "需要网络PIN解锁";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "需要SIM卡PIN解锁";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "需要SIM卡PUK解锁";
            case TelephonyManager.SIM_STATE_READY:
                return "就绪";
            case TelephonyManager.SIM_STATE_UNKNOWN:
                return "未知状态";
            default:
                return "状态码: " + state;
        }
    }

    private void startRecording() {
        if (isRecording) {
            Toast.makeText(this, "正在录音中", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            isRecording = true;
            txtRecordStatus.setText("录音中...");
            Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "录音失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (!isRecording) {
            return;
        }

        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            txtRecordStatus.setText("录音已保存: " + audioFilePath);
            Toast.makeText(this, "录音已保存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "停止录音失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void playRecording() {
        if (isRecording) {
            Toast.makeText(this, "请先停止录音", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    Toast.makeText(MainActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
                }
            });
            
            txtRecordStatus.setText("正在播放录音...");
        } catch (IOException e) {
            Toast.makeText(this, "播放失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void setScreenBrightness(int brightness) {
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            Toast.makeText(this, "请开启修改系统设置权限", Toast.LENGTH_LONG).show();
            startActivity(intent);
            return;
        }

        try {
            brightness = Math.max(0, Math.min(255, brightness));
            Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightness);
            
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = brightness / 255f;
            getWindow().setAttributes(params);
        } catch (Exception e) {
            Toast.makeText(this, "设置亮度失败: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtil.hasPermissions(this)) {
            sensorHandler.startSensorMonitoring();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: 释放/暂停需要的资源（例如停止传感器、停止录音、保存状态等）
        if (sensorHandler != null) sensorHandler.stopSensorMonitoring();
        if (wifiHandler != null) wifiHandler.stop();
        if (bluetoothHandler != null) bluetoothHandler.stop();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
        if (isRecording) { try { stopRecording(); } catch (Exception e) {} }
    }
}
