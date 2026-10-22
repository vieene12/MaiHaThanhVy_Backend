package com.ltw.studentattendance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.ltw.studentattendance.R;
import com.ltw.studentattendance.models.AttendanceRecord;
import com.ltw.studentattendance.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordAdapter.RecordViewHolder> {

    private final Context context;
    private List<AttendanceRecord> recordList = new ArrayList<>();

    public AttendanceRecordAdapter(Context context) {
        this.context = context;
    }

    public void setRecords(List<AttendanceRecord> records) {
        this.recordList = (records != null) ? records : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        AttendanceRecord item = recordList.get(position);

        String className = item.getClassName();
        if (className == null || className.isEmpty()) {
            className = "Buổi học #" + item.getSessionId();
        }
        holder.tvRecordClassName.setText(className);

        String date = item.getSessionDate();
        if (date == null || date.isEmpty()) date = "Hôm nay";
        holder.tvRecordDate.setText("Ngày: " + date);

        String checkInTime = item.getCheckInTime();
        if (checkInTime == null || checkInTime.isEmpty()) checkInTime = "--:--";
        holder.tvRecordTime.setText("Giờ: " + checkInTime);

        String method = item.getMethod();
        if (method == null || method.isEmpty()) method = "Thủ công";
        holder.tvRecordMethod.setText("PT: " + method);

        String status = item.getStatus();
        if (Constants.STATUS_PRESENT.equalsIgnoreCase(status)) {
            holder.tvRecordStatus.setText(context.getString(R.string.status_present));
            holder.tvRecordStatus.setTextColor(ContextCompat.getColor(context, R.color.present));
            holder.tvRecordStatus.setBackgroundResource(R.drawable.badge_present);
        } else if (Constants.STATUS_LATE.equalsIgnoreCase(status)) {
            holder.tvRecordStatus.setText(context.getString(R.string.status_late));
            holder.tvRecordStatus.setTextColor(ContextCompat.getColor(context, R.color.late));
            holder.tvRecordStatus.setBackgroundResource(R.drawable.badge_late);
        } else {
            holder.tvRecordStatus.setText(context.getString(R.string.status_absent));
            holder.tvRecordStatus.setTextColor(ContextCompat.getColor(context, R.color.absent));
            holder.tvRecordStatus.setBackgroundResource(R.drawable.badge_absent);
        }
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecordClassName;
        TextView tvRecordDate;
        TextView tvRecordTime;
        TextView tvRecordMethod;
        TextView tvRecordStatus;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecordClassName = itemView.findViewById(R.id.tvRecordClassName);
            tvRecordDate = itemView.findViewById(R.id.tvRecordDate);
            tvRecordTime = itemView.findViewById(R.id.tvRecordTime);
            tvRecordMethod = itemView.findViewById(R.id.tvRecordMethod);
            tvRecordStatus = itemView.findViewById(R.id.tvRecordStatus);
        }
    }
}
