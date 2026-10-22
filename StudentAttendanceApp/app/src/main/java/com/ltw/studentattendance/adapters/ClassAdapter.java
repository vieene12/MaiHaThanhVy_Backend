package com.ltw.studentattendance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.models.ClassRoom;
import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    public interface OnClassClickListener {
        void onClassClick(ClassRoom classRoom);
        void onEditClick(ClassRoom classRoom);
        void onDeleteClick(ClassRoom classRoom);
    }

    private final Context context;
    private List<ClassRoom> classList = new ArrayList<>();
    private final OnClassClickListener listener;
    private boolean showActionButtons = true;

    public ClassAdapter(Context context, OnClassClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setShowActionButtons(boolean show) {
        this.showActionButtons = show;
        notifyDataSetChanged();
    }

    public void setClasses(List<ClassRoom> classes) {
        this.classList = (classes != null) ? classes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassRoom item = classList.get(position);
        holder.tvClassName.setText(item.getClassName());

        String subjectText = "Môn học: " + (item.getSubjectName() != null ? item.getSubjectName() : "N/A");
        holder.tvSubjectName.setText(subjectText);

        String teacherText = "GV: " + (item.getTeacherName() != null ? item.getTeacherName() : "N/A");
        holder.tvTeacherName.setText(teacherText);

        String roomText = "Phòng: " + (item.getRoom() != null && !item.getRoom().isEmpty() ? item.getRoom() : "N/A");
        holder.tvRoom.setText(roomText);

        String scheduleText = item.getSchedule() != null && !item.getSchedule().isEmpty() ? item.getSchedule() : "Chưa xếp lịch";
        holder.tvSchedule.setText(scheduleText);

        if (showActionButtons) {
            holder.btnEditClass.setVisibility(View.VISIBLE);
            holder.btnDeleteClass.setVisibility(View.VISIBLE);

            holder.btnEditClass.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(item);
            });

            holder.btnDeleteClass.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        } else {
            holder.btnEditClass.setVisibility(View.GONE);
            holder.btnDeleteClass.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClassClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName;
        TextView tvSubjectName;
        TextView tvTeacherName;
        TextView tvRoom;
        TextView tvSchedule;
        ImageButton btnEditClass;
        ImageButton btnDeleteClass;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            btnEditClass = itemView.findViewById(R.id.btnEditClass);
            btnDeleteClass = itemView.findViewById(R.id.btnDeleteClass);
        }
    }
}
