package com.ltw.studentattendance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.models.Student;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(Student student);
        void onEditClick(Student student);
        void onDeleteClick(Student student);
    }

    private final Context context;
    private List<Student> studentList = new ArrayList<>();
    private final OnStudentClickListener listener;

    public StudentAdapter(Context context, OnStudentClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setStudents(List<Student> students) {
        this.studentList = (students != null) ? students : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvStudentName.setText(student.getFullName());
        holder.tvStudentCode.setText(student.getStudentCode());

        String majorText = student.getMajor();
        if (majorText == null || majorText.isEmpty()) {
            majorText = student.getEmail() != null ? student.getEmail() : "";
        }
        holder.tvStudentMajor.setText(majorText);

        if (student.getAvatarUrl() != null && !student.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(student.getAvatarUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.ivStudentAvatar);
        } else {
            holder.ivStudentAvatar.setImageResource(R.drawable.ic_person);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStudentClick(student);
        });

        holder.btnEditStudent.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(student);
        });

        holder.btnDeleteStudent.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(student);
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivStudentAvatar;
        TextView tvStudentName;
        TextView tvStudentCode;
        TextView tvStudentMajor;
        ImageButton btnEditStudent;
        ImageButton btnDeleteStudent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStudentAvatar = itemView.findViewById(R.id.ivStudentAvatar);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            tvStudentMajor = itemView.findViewById(R.id.tvStudentMajor);
            btnEditStudent = itemView.findViewById(R.id.btnEditStudent);
            btnDeleteStudent = itemView.findViewById(R.id.btnDeleteStudent);
        }
    }
}
