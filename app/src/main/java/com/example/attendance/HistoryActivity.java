package com.example.attendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance.model.AttendanceResponse;
import com.example.attendance.model.CustomToast;
import com.example.attendance.model.SqlDateTypeAdapter;
import com.example.attendance.model.SqlTimeTypeAdapter;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendance.adapter.AttendanceAdapter;
import com.example.attendance.model.Utils;
import com.example.attendance.service.ApiClient;
import com.google.gson.Gson;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {
    ImageButton btnBack;
    TextView textViewDate;
    AttendanceAdapter attendanceAdapter;
    RecyclerView recyclerViewAttendance;
    RelativeLayout btnChooseDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView ();
        setOnClick();
        setupCalendarView();
        setupAttendanceListView(getTodayInSQLFormat());
    }

    private void initView() {
        btnBack = findViewById(R.id.back_btn);
        textViewDate = findViewById(R.id.text_date);
        recyclerViewAttendance = findViewById(R.id.attendance_list);
        btnChooseDate = findViewById(R.id.btnDate);
    }
    private void setOnClick() {
        btnChooseDate.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_btn){
            finish();
        }
        else if (v.getId() == R.id.btnDate){
            showDialogDate();
        }
    }
    private void setupCalendarView() {
        LocalDate today = LocalDate.now();

        int day = today.getDayOfMonth();
        int month = today.getMonthValue();
        int year = today.getYear();

        String formatDay = (day < 10) ? "0" + day : String.valueOf(day);
        String formatMonth = (month < 10) ? "0" + month : String.valueOf(month);
        String dateStr = formatDay+"-"+formatMonth+"-"+year;
        textViewDate.setText(dateStr);
    }

    private void showDialogDate() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_clendar);
        Window window = dialog.getWindow();
        if (window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.BOTTOM;
        window.setAttributes(windowAttributes);
        if (windowAttributes.gravity == Gravity.BOTTOM) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        } else {
            dialog.setCancelable(false);
        }
        NumberPicker numberPickerDay,numberPickerMonth,numberPickerYear;
        RelativeLayout btnEnter;

        numberPickerDay = dialog.findViewById(R.id.day);
        numberPickerMonth = dialog.findViewById(R.id.month);
        numberPickerYear = dialog.findViewById(R.id.year);
        btnEnter = dialog.findViewById(R.id.btnEnter);

        numberPickerDay.setMinValue(1);
        numberPickerDay.setMaxValue(31);
        numberPickerMonth.setMinValue(1);
        numberPickerMonth.setMaxValue(12);
        numberPickerYear.setMinValue(2025);
        numberPickerYear.setMaxValue(2040);

        setCurrentMonth(numberPickerDay,numberPickerMonth,numberPickerYear);

        numberPickerDay.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d",value);
            }
        });
        numberPickerMonth.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d",value);
            }
        });

        btnEnter.setOnClickListener(v -> {
            textViewDate.setText("");
            getDetail(numberPickerDay,numberPickerMonth,numberPickerYear);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void getDetail(NumberPicker numberPickerDay, NumberPicker numberPickerMonth, NumberPicker numberPickerYear) {
        LocalDate today = LocalDate.now();

        int dayValue = numberPickerDay.getValue();
        int monthValue = numberPickerMonth.getValue();
        int yearValue = numberPickerYear.getValue();
        int day = 0, month = 0, year = 0;
        if (yearValue < today.getYear() || ((yearValue == today.getYear()) && (monthValue < today.getMonthValue()))){
            day = today.getDayOfMonth();
            month = today.getMonthValue();
            year = today.getYear();
        }
        else {
            day = dayValue;
            month = monthValue;
            year = yearValue;
        }
        String formatDay = (day < 10) ? "0" + day : String.valueOf(day);
        String formatMonth = (month < 10) ? "0" + month : String.valueOf(month);
        String dateStr = formatDay+"-"+formatMonth+"-"+year;

        textViewDate.setText(dateStr);
        setupAttendanceListView(year+"-"+formatMonth+"-"+formatDay);
    }

    private void setCurrentMonth(NumberPicker numberPickerDay, NumberPicker numberPickerMonth, NumberPicker numberPickerYear) {
        LocalDate today = LocalDate.now();

        numberPickerDay.setValue(today.getDayOfMonth());
        numberPickerMonth.setValue(today.getMonthValue());
        numberPickerYear.setValue(today.getYear());
    }

    private void setupAttendanceListView(String date) {
        String accountIdStr = Utils.getSharedPreferences(getApplicationContext(), "accountId");
        long accountId = Long.parseLong(accountIdStr);
        Map<String, Object> params = new HashMap<>();
        params.put("accountId", accountId);
        params.put("attendanceDate", date);
        ApiClient.getAttendanceHistory(params, new ApiClient.ApiResponseListener() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Date.class, new SqlDateTypeAdapter())
                        .registerTypeAdapter(Time.class, new SqlTimeTypeAdapter()) // Thêm dòng này
                        .create();
                Type listType = new TypeToken<List<AttendanceResponse>>() {}.getType();
                List<AttendanceResponse> attendanceList = gson.fromJson(response, listType);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAttendanceListView(attendanceList);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }
    private void showAttendanceListView(List<AttendanceResponse> attendanceList) {
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewAttendance.setAdapter(attendanceAdapter);
    }

    private String getTodayInSQLFormat() {
        LocalDate today = LocalDate.now();
        return today.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}