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
import com.ltw.studentattendance.models.Subject;
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject);
        void onEditClick(Subject subject);
        void onDeleteClick(Subject subject);
    }

    private final Context context;
    private List<Subject> subjectList = new ArrayList<>();
    private final OnSubjectClickListener listener;

    public SubjectAdapter(Context context, OnSubjectClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjectList = (subjects != null) ? subjects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.tvSubjectName.setText(subject.getSubjectName());
        holder.tvSubjectCode.setText(subject.getSubjectCode());
        holder.tvCredits.setText(subject.getCredits() + " Tín chỉ");

        String desc = subject.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.tvDescription.setText(desc);
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSubjectClick(subject);
        });

        holder.btnEditSubject.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(subject);
        });

        holder.btnDeleteSubject.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(subject);
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName;
        TextView tvSubjectCode;
        TextView tvCredits;
        TextView tvDescription;
        ImageButton btnEditSubject;
        ImageButton btnDeleteSubject;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSubjectCode = itemView.findViewById(R.id.tvSubjectCode);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEditSubject = itemView.findViewById(R.id.btnEditSubject);
            btnDeleteSubject = itemView.findViewById(R.id.btnDeleteSubject);
        }
    }
}
