package com.joaosimonassi.pocv3sync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ido.ble.BLEManager;
import com.ido.ble.bluetooth.connect.ConnectFailedReason;
import com.ido.ble.bluetooth.device.BLEDevice;
import com.ido.ble.callback.BindCallBack;
import com.ido.ble.callback.ConnectCallBack;
import com.ido.ble.callback.ScanCallBack;
import com.ido.ble.callback.SyncV3CallBack;
import com.ido.ble.data.manage.database.HealthActivityV3;
import com.ido.ble.data.manage.database.HealthBloodPressureV3;
import com.ido.ble.data.manage.database.HealthGpsV3;
import com.ido.ble.data.manage.database.HealthHeartRateSecond;
import com.ido.ble.data.manage.database.HealthNoise;
import com.ido.ble.data.manage.database.HealthPressure;
import com.ido.ble.data.manage.database.HealthPressureItem;
import com.ido.ble.data.manage.database.HealthSleepV3;
import com.ido.ble.data.manage.database.HealthSpO2;
import com.ido.ble.data.manage.database.HealthSpO2Item;
import com.ido.ble.data.manage.database.HealthSportV3;
import com.ido.ble.data.manage.database.HealthSwimming;
import com.ido.ble.data.manage.database.HealthTemperature;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<String> permissionList = new ArrayList<>();
    private final List<BLEDevice> deviceFound = new ArrayList<>();
    private int requestPermissonCount = 0;
    TextView status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionList.add(Manifest.permission.BLUETOOTH_ADMIN);
        permissionList.add(Manifest.permission.BLUETOOTH);
        getPermission();

        Button startScanBtn = findViewById(R.id.scan);
        Button startSyncBtn = findViewById(R.id.sync);
        status = findViewById(R.id.status);

        startScanBtn.setOnClickListener(view -> {
            BLEManager.startScanDevices();
        });

        startSyncBtn.setOnClickListener(view -> {
            BLEManager.startSyncV3Health();
        });


        BLEManager.registerScanCallBack(scanCallback);
        BLEManager.registerConnectCallBack(connectCallback);
        BLEManager.registerBindCallBack(bindCallBack);
        BLEManager.registerSyncV3CallBack(syncCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status.setText("Init SDK");
        BLEManager.init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 127) {
            for (int i = 0; i < permissions.length; i++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    permissionList.remove(permissions[i]);
                }
            }

            getPermission();
        }

    }

    private void getPermission() {
        if (requestPermissonCount < 5) {
            if (permissionList.size() != 0) {
                requestPermissonCount++;
                requestPermissions(permissionList.toArray(new String[permissionList.size()]), 127);
            }
        }
    }

    //Scan ____________________________________
    private ScanCallBack.ICallBack scanCallback = new ScanCallBack.ICallBack() {
        @Override
        public void onStart() {
            Toast.makeText(MainActivity.this, "Scan init", Toast.LENGTH_LONG).show();
            status.setText("Scan Started");
        }

        @Override
        public void onFindDevice(BLEDevice device) {
            deviceFound.add(device);
            status.setText("Find: " + device.mDeviceName + " - Searching...");
        }

        @Override
        public void onScanFinished() {
            Toast.makeText(MainActivity.this, "Scan Finish", Toast.LENGTH_LONG).show();
            for(BLEDevice currentDevice : deviceFound){
                if(currentDevice.mDeviceAddress.equals("E6:54:54:E0:56:5A")){
                    status.setText("Start Connect");
                    BLEManager.connect(currentDevice);
                }
            }
        }
    };

    //Connect ____________________________________

    private final ConnectCallBack.ICallBack connectCallback = new ConnectCallBack.ICallBack() {
        @Override
        public void onConnectStart(String s) {
            Toast.makeText(MainActivity.this, "onConnectStart", Toast.LENGTH_LONG).show();
            status.setText("onConnectStart");
        }

        @Override
        public void onConnecting(String s) {
            status.setText("onConnectStart");
        }

        @Override
        public void onRetry(int i, String s) {

        }

        @Override
        public void onConnectSuccess(String s) {
            Toast.makeText(MainActivity.this, "CONNECTED!!!", Toast.LENGTH_LONG).show();
            status.setText("onConnectSuccess");
            BLEManager.bind();
        }

        @Override
        public void onConnectFailed(ConnectFailedReason connectFailedReason, String s) {
            Toast.makeText(MainActivity.this, "FAILED: " + connectFailedReason.name(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onConnectBreak(String s) {

        }

        @Override
        public void onInDfuMode(BLEDevice bleDevice) {

        }

        @Override
        public void onDeviceInNotBindStatus(String s) {

        }

        @Override
        public void onInitCompleted(String s) {

        }
    };

    //Bind ____________________________________

    private BindCallBack.ICallBack bindCallBack = new BindCallBack.ICallBack() {
        @Override
        public void onSuccess() {
            status.setText("Bind Success");
        }

        @Override
        public void onFailed(BindCallBack.BindFailedError bindFailedError) {
            status.setText("Bind onFailed" + bindFailedError.name());
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onReject() {

        }

        @Override
        public void onNeedAuth(int i) {

        }
    };

    //Sync

    private final SyncV3CallBack.ICallBack syncCallback = new SyncV3CallBack.ICallBack() {
        @Override
        public void onStart() {
            status.setText("Start Sync V3");
        }

        @Override
        public void onProgress(int i) {
            status.setText("Start Sync V3: " + i);
        }

        @Override
        public void onStop() {

        }

        @Override
        public void onSuccess() {
            status.setText("Sync V3 onSuccess");
        }

        @Override
        public void onFailed() {
            status.setText("Sync V3 onFailed");
        }

        @Override
        public void onGetHealthSpO2Data(HealthSpO2 healthSpO2, List<HealthSpO2Item> list, boolean b) {

        }

        @Override
        public void onGetHealthPressureData(HealthPressure healthPressure, List<HealthPressureItem> list, boolean b) {

        }

        @Override
        public void onGetHealthHeartRateSecondData(HealthHeartRateSecond healthHeartRateSecond, boolean b) {

        }

        @Override
        public void onGetHealthSwimmingData(HealthSwimming healthSwimming) {

        }

        @Override
        public void onGetHealthActivityV3Data(HealthActivityV3 healthActivityV3) {

        }

        @Override
        public void onGetHealthSportV3Data(HealthSportV3 healthSportV3) {

        }

        @Override
        public void onGetHealthSleepV3Data(HealthSleepV3 healthSleepV3) {

        }

        @Override
        public void onGetHealthGpsV3Data(HealthGpsV3 healthGpsV3) {

        }

        @Override
        public void onGetHealthNoiseData(HealthNoise healthNoise) {

        }

        @Override
        public void onGetHealthTemperature(HealthTemperature healthTemperature) {

        }

        @Override
        public void onGetHealthBloodPressure(HealthBloodPressureV3 healthBloodPressureV3) {

        }
    };
}
