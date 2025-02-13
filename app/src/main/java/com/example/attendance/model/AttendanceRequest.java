package com.example.attendance.model;

import java.sql.Date;
import java.sql.Time;

public class AttendanceRequest {
    private Long employeeId;
    private String attendanceDevice;
    private String imageCode;
    private String date;
    private String time;

    public AttendanceRequest() {
    }

    public AttendanceRequest(Long employeeId, String attendanceDevice, String imageCode, String date, String time) {
        this.employeeId = employeeId;
        this.attendanceDevice = attendanceDevice;
        this.imageCode = imageCode;
        this.date = date;
        this.time = time;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getAttendanceDevice() {
        return attendanceDevice;
    }

    public void setAttendanceDevice(String attendanceDevice) {
        this.attendanceDevice = attendanceDevice;
    }

    public String getImageCode() {
        return imageCode;
    }

    public void setImageCode(String imageCode) {
        this.imageCode = imageCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "AttendanceRequest{" +
                "employeeId=" + employeeId +
                ", attendanceDevice='" + attendanceDevice + '\'' +
                ", imageCode='" + imageCode + '\'' +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
