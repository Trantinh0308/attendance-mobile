package com.example.attendance;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendance.model.AttendanceRequest;
import com.example.attendance.model.CustomToast;
import com.example.attendance.model.Utils;
import com.example.attendance.service.ApiClient;
import com.example.attendance.service.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    RelativeLayout menuCheckIn, menuCheckOut, menuHistory;
    String bssidCurrent = "";
    TextView textCheckIn, textCheckOut;
    int backgroundColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backgroundColor = ContextCompat.getColor(this, R.color.light_gray);
        initView();
        setOnClick();
        setViewButton();
        checkPermissionAccessLocation();
    }

    private void initView() {
        menuCheckIn = findViewById(R.id.block_checkin);
        menuCheckOut = findViewById(R.id.block_checkout);
        menuHistory = findViewById(R.id.block_history);
        textCheckIn = findViewById(R.id.text_checkIn);
        textCheckOut = findViewById(R.id.text_checkOut);
    }
    private void setOnClick() {
        menuCheckIn.setOnClickListener(this);
        menuCheckOut.setOnClickListener(this);
        menuHistory.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.block_checkin){
            if (bssidCurrent.isEmpty()){
                CustomToast.showToast(this,"Cấp quyền truy cập vị trí cho ứng dụng",1000);
            }
            else {
                checkWifiBSSID(bssidCurrent,1);
            }
        }
        else if (v.getId() == R.id.block_checkout){
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isBefore(LocalTime.of(17, 0))) {
                CustomToast.showToast(this,"Ngoài thời gian checkout",1000);
                return;
            }
            if (bssidCurrent.isEmpty()){
                CustomToast.showToast(this,"Cấp quyền truy cập vị trí cho ứng dụng",1000);
            }
            else {
                checkWifiBSSID(bssidCurrent,2);
            }
        }
        else if (v.getId() == R.id.block_history){
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        }
    }

    private void setViewButton() {
        Map<String, Object> params = new HashMap<>();
        String employeeIdStr = Utils.getSharedPreferences(getApplicationContext(),"employeeId");
        params.put("employeeId", Long.parseLong(employeeIdStr));
        ApiClient.getNumberAttendance(params, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(String response) {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    int number = jsonResponse.getInt("number");
                    if (number > 0){
                        setupButtonView(1);
                    }
                    if (number > 1){
                        setupButtonView(2);
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("API Error", error);
            }
        });
    }

    private void checkPermissionAccessLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            getWifiBSSID();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getWifiBSSID();
                    }
                });
            } else {
                Log.e(TAG, "Không thể quy cập thông tin Wifi");
            }
        }
    }

    private void getWifiBSSID() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        bssidCurrent = wifiInfo.getBSSID();
        Log.d(TAG, "getWifiBSSID: "+bssidCurrent);
    }
    private void checkWifiBSSID(String bssid, int status) {
        if (bssid.equalsIgnoreCase(Constants.BSSID_NETWORK_COMPANY)){
            attendance(status);
        }
        else {
            CustomToast.showToast(this,"Không thể điểm danh với Wifi hiện tại",1000);
        }
    }

    private void attendance(int status) {
        long currentTimeMillis = System.currentTimeMillis();
        long employeeId = Long.parseLong(Utils.getSharedPreferences(getApplicationContext(), "employeeId"));

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String date = dateFormat.format(new java.util.Date(currentTimeMillis));
        String time = timeFormat.format(new java.util.Date(currentTimeMillis));

        AttendanceRequest attendanceRequest = new AttendanceRequest();
        attendanceRequest.setEmployeeId(employeeId);
        attendanceRequest.setAttendanceDevice("Mobile");
        attendanceRequest.setImageCode(null);
        attendanceRequest.setDate(date);
        attendanceRequest.setTime(time);

        saveAttendance(attendanceRequest,status);
    }

    private void saveAttendance(AttendanceRequest attendanceRequest, int status) {
        ApiClient.postAttendance(attendanceRequest, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupButtonView(status);
                        CustomToast.showToast(MainActivity.this,"Điểm danh thành oông",1000);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CustomToast.showToast(MainActivity.this,"Điểm danh không thành oông",1000);
                    }
                });
            }
        });
    }

    private void setupButtonView(int status) {
        if (status == 1){
            textCheckIn.setText("Đã check in");
            menuCheckIn.setEnabled(false);
            menuCheckIn.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        }
        else {
            textCheckOut.setText("Đã check out");
            menuCheckOut.setEnabled(false);
            menuCheckOut.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        }
    }
}