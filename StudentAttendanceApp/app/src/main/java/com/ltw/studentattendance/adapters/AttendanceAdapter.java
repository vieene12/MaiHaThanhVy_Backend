package com.ltw.studentattendance.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.models.Student;
import com.ltw.studentattendance.utils.Constants;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    public interface OnAttendanceStatusChangeListener {
        void onStatusChanged(Student student, String newStatus);
    }

    private final Context context;
    private List<Student> studentList = new ArrayList<>();
    private final Map<Integer, String> studentStatusMap = new HashMap<>(); // studentId -> PRESENT/LATE/ABSENT
    private final OnAttendanceStatusChangeListener listener;

    public AttendanceAdapter(Context context, OnAttendanceStatusChangeListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setStudents(List<Student> students) {
        this.studentList = (students != null) ? students : new ArrayList<>();
        studentStatusMap.clear();
        for (Student s : this.studentList) {
            studentStatusMap.put(s.getId(), Constants.STATUS_PRESENT); // Default to PRESENT
        }
        notifyDataSetChanged();
    }

    public void setStudentStatus(int studentId, String status) {
        studentStatusMap.put(studentId, status);
        notifyDataSetChanged();
    }

    public Map<Integer, String> getStudentStatusMap() {
        return studentStatusMap;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance_student, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvAttendanceStudentName.setText(student.getFullName());
        holder.tvAttendanceStudentCode.setText("Mã SV: " + student.getStudentCode());

        if (student.getAvatarUrl() != null && !student.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(student.getAvatarUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.ivAttendanceStudentAvatar);
        } else {
            holder.ivAttendanceStudentAvatar.setImageResource(R.drawable.ic_person);
        }

        String currentStatus = studentStatusMap.get(student.getId());
        if (currentStatus == null) currentStatus = Constants.STATUS_PRESENT;

        updateButtonStates(holder, currentStatus);

        holder.btnStatusPresent.setOnClickListener(v -> {
            studentStatusMap.put(student.getId(), Constants.STATUS_PRESENT);
            updateButtonStates(holder, Constants.STATUS_PRESENT);
            if (listener != null) listener.onStatusChanged(student, Constants.STATUS_PRESENT);
        });

        holder.btnStatusLate.setOnClickListener(v -> {
            studentStatusMap.put(student.getId(), Constants.STATUS_LATE);
            updateButtonStates(holder, Constants.STATUS_LATE);
            if (listener != null) listener.onStatusChanged(student, Constants.STATUS_LATE);
        });

        holder.btnStatusAbsent.setOnClickListener(v -> {
            studentStatusMap.put(student.getId(), Constants.STATUS_ABSENT);
            updateButtonStates(holder, Constants.STATUS_ABSENT);
            if (listener != null) listener.onStatusChanged(student, Constants.STATUS_ABSENT);
        });
    }

    private void updateButtonStates(AttendanceViewHolder holder, String status) {
        int presentColor = ContextCompat.getColor(context, R.color.present);
        int lateColor = ContextCompat.getColor(context, R.color.late);
        int absentColor = ContextCompat.getColor(context, R.color.absent);
        int dimColor = ContextCompat.getColor(context, R.color.gray_light);

        if (Constants.STATUS_PRESENT.equalsIgnoreCase(status)) {
            holder.btnStatusPresent.setBackgroundTintList(ColorStateList.valueOf(presentColor));
            holder.btnStatusLate.setBackgroundTintList(ColorStateList.valueOf(dimColor));
            holder.btnStatusAbsent.setBackgroundTintList(ColorStateList.valueOf(dimColor));
        } else if (Constants.STATUS_LATE.equalsIgnoreCase(status)) {
            holder.btnStatusPresent.setBackgroundTintList(ColorStateList.valueOf(dimColor));
            holder.btnStatusLate.setBackgroundTintList(ColorStateList.valueOf(lateColor));
            holder.btnStatusAbsent.setBackgroundTintList(ColorStateList.valueOf(dimColor));
        } else {
            holder.btnStatusPresent.setBackgroundTintList(ColorStateList.valueOf(dimColor));
            holder.btnStatusLate.setBackgroundTintList(ColorStateList.valueOf(dimColor));
            holder.btnStatusAbsent.setBackgroundTintList(ColorStateList.valueOf(absentColor));
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAttendanceStudentAvatar;
        TextView tvAttendanceStudentName;
        TextView tvAttendanceStudentCode;
        Button btnStatusPresent;
        Button btnStatusLate;
        Button btnStatusAbsent;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAttendanceStudentAvatar = itemView.findViewById(R.id.ivAttendanceStudentAvatar);
            tvAttendanceStudentName = itemView.findViewById(R.id.tvAttendanceStudentName);
            tvAttendanceStudentCode = itemView.findViewById(R.id.tvAttendanceStudentCode);
            btnStatusPresent = itemView.findViewById(R.id.btnStatusPresent);
            btnStatusLate = itemView.findViewById(R.id.btnStatusLate);
            btnStatusAbsent = itemView.findViewById(R.id.btnStatusAbsent);
        }
    }
}
