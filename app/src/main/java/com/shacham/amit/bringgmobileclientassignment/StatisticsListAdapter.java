package com.shacham.amit.bringgmobileclientassignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class StatisticsListAdapter extends ArrayAdapter<DayStatistics> {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");

    public StatisticsListAdapter(Context context, int resource, List<DayStatistics> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DayStatistics item = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.statistics_list_item, parent, false);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.workTime = (TextView) convertView.findViewById(R.id.work_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String formattedDate = DATE_FORMAT.format(item.getDate());
        viewHolder.date.setText(formattedDate);
        viewHolder.workTime.setText(String.valueOf(item.getTotalWorkTime()));

        return convertView;
    }

    private static class ViewHolder {
        TextView date;
        TextView workTime;
    }
}
