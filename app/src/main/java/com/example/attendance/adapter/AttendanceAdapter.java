package com.example.attendance.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance.R;
import com.example.attendance.model.AttendanceResponse;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>{
    List<AttendanceResponse> attendanceResponseList;

    public AttendanceAdapter(List<AttendanceResponse> attendanceResponseList){
        this.attendanceResponseList = attendanceResponseList;
    }
    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new AttendanceAdapter.AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceResponse attendance = attendanceResponseList.get(position);
        deCodeImage(holder,attendance.getEmployeeImage());
        holder.textFullName.setText(attendance.getEmployeeName());
        holder.textDevice.setText("Thiết bị: "+attendance.getDeviceName());
        holder.textDepartment.setText("Phòng ban: "+attendance.getDepartmentName());
        holder.textStatus.setText("Trạng thái: "+attendance.getStatus());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedTime = timeFormat.format(attendance.getAttendanceTime());
        holder.textTime.setText("Thời gian: "+formattedTime);
    }
    private void deCodeImage(AttendanceViewHolder holder, String imageCode) {
        if (imageCode != null && !imageCode.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageCode, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.employeeImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                holder.employeeImage.setImageResource(R.drawable.accountant);
            }
        } else {
            holder.employeeImage.setImageResource(R.drawable.accountant);
        }
    }
    @Override
    public int getItemCount() {
        return attendanceResponseList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder{
        TextView textFullName, textDepartment, textTime, textStatus, textDevice;
        ImageView employeeImage;
        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textFullName = itemView.findViewById(R.id.name_employee);
            textDepartment = itemView.findViewById(R.id.department);
            textDevice = itemView.findViewById(R.id.device);
            textTime = itemView.findViewById(R.id.time);
            textStatus = itemView.findViewById(R.id.status);
            employeeImage = itemView.findViewById(R.id.image_employee);
        }
    }
}
