package com.example.myapplication_permissions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import android.Manifest;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText textInput;
    private MaterialButton buttonEnter;
    private TextInputEditText textMiss;
    private String passwordInput;
    private WifiManager wifiManager;
    private NfcManager nfcManager;
    private CameraManager cameraManager;
    private boolean isFlashlightOn;
    MaterialButton flashlightButton;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100111;
    private BatteryManager batteryManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        textMiss.setVisibility(View.GONE);
        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordInput = String.valueOf(textInput.getText());
                if (!textInput.getText().toString().isEmpty()) {
                    textMiss.setVisibility(View.VISIBLE);
                    textMiss.setText("Please enter password");
                }
                if (verifyConditions()) { // verify all other conditions
                    moveToNextActivity();
                }
            }
        });
        initialManagers();
        flashlightButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            } else {
                toggleFlashlight();
            }
        });
    }
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission is needed to use the flashlight", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleFlashlight();
            } else {
                Toast.makeText(this, "Camera permission is required to use the flashlight", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void moveToNextActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
    private void initialManagers() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        batteryManager = (BatteryManager) getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
    }
    private boolean verifyConditions() {
        StringBuilder missingText = new StringBuilder();
        boolean result = true;
        if (!wifiManager.isWifiEnabled()) {
            missingText.append("WiFi should be turned on\n");
            result = false;
        }
        if (!(nfcManager.getDefaultAdapter() != null && nfcManager.getDefaultAdapter().isEnabled())) {
            missingText.append("NFC should be turned on\n");
            result = false;
        }
        if (!isFlashlightOn) {
            missingText.append("FLASHlIGHT should be turned on\n");
            result = false;
        }
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        int enteredBatteryLevel = Integer.parseInt(passwordInput.substring(0, 2));
        if (enteredBatteryLevel != batteryLevel) {
            missingText.append("Battery level does not match to password\n");
            result = false;
        }
        if (!result) {
            textMiss.setVisibility(View.VISIBLE);
        }
        textMiss.setText("These permissions are missing:\n\n" + missingText);
        return result;
    }
    private void findViews() {
        textInput = findViewById(R.id.text);
        buttonEnter = findViewById(R.id.ok);
        textMiss = findViewById(R.id.miss_prem);
        flashlightButton = findViewById(R.id.flashlightButton);
    }
    private void toggleFlashlight() {
        try {
            String cameraId = cameraManager.getCameraIdList()[0];  // Assuming first camera
            if (isFlashlightOn) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashlightOn = false;
            } else {
                cameraManager.setTorchMode(cameraId, true);
                isFlashlightOn = true;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}