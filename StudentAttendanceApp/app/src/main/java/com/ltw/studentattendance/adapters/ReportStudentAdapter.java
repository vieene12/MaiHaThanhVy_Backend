package com.ltw.studentattendance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.models.ReportSummary;
import java.util.ArrayList;
import java.util.List;

public class ReportStudentAdapter extends RecyclerView.Adapter<ReportStudentAdapter.ReportViewHolder> {

    private final Context context;
    private List<ReportSummary.StudentReportItem> reportList = new ArrayList<>();

    public ReportStudentAdapter(Context context) {
        this.context = context;
    }

    public void setReports(List<ReportSummary.StudentReportItem> reports) {
        this.reportList = (reports != null) ? reports : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_student, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportSummary.StudentReportItem item = reportList.get(position);
        holder.tvReportStudentName.setText(item.getFullName());
        holder.tvReportStudentCode.setText("Mã SV: " + item.getStudentCode());

        int rateInt = (int) Math.round(item.getAttendanceRate());
        holder.tvReportRate.setText(rateInt + "%");
        holder.pbReportAttendance.setProgress(rateInt);

        holder.tvReportPresent.setText("Có mặt: " + item.getPresentCount());
        holder.tvReportLate.setText("Muộn: " + item.getLateCount());
        holder.tvReportAbsent.setText("Vắng: " + item.getAbsentCount());
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportStudentName;
        TextView tvReportStudentCode;
        TextView tvReportRate;
        ProgressBar pbReportAttendance;
        TextView tvReportPresent;
        TextView tvReportLate;
        TextView tvReportAbsent;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportStudentName = itemView.findViewById(R.id.tvReportStudentName);
            tvReportStudentCode = itemView.findViewById(R.id.tvReportStudentCode);
            tvReportRate = itemView.findViewById(R.id.tvReportRate);
            pbReportAttendance = itemView.findViewById(R.id.pbReportAttendance);
            tvReportPresent = itemView.findViewById(R.id.tvReportPresent);
            tvReportLate = itemView.findViewById(R.id.tvReportLate);
            tvReportAbsent = itemView.findViewById(R.id.tvReportAbsent);
        }
    }
}
