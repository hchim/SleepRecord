package im.hch.sleeprecord.activities.records;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.SleepRecordUtils;

public class SleepRecordsAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<SleepRecord> mDataSource;
    private int[] qualityLevelColors;

    public SleepRecordsAdapter(Context context, List<SleepRecord> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        qualityLevelColors = mContext.getResources().getIntArray(R.array.quality_level);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SleepRecordViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_sleep_record, parent, false);
            viewHolder = new SleepRecordViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SleepRecordViewHolder) convertView.getTag();
        }

        SleepRecord sleepRecord = (SleepRecord) getItem(position);
        viewHolder.update(sleepRecord);

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        //Override this method and return false to make the listview item unslectable.
        return false;
    }

    public void updateSleepRecords(List<SleepRecord> sleepRecords) {
        if (sleepRecords != null) {
            this.mDataSource = sleepRecords;
            notifyDataSetChanged();
        }
    }

    public void addSleepRecords(List<SleepRecord> sleepRecords) {
        if (sleepRecords != null) {
            this.mDataSource.addAll(sleepRecords);
            notifyDataSetChanged();
        }
    }

    public void addSleepRecord(Date from, Date to) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTime(from);
        Calendar toCal = Calendar.getInstance();
        toCal.setTime(to);

        if (fromCal.get(Calendar.DATE) != toCal.get(Calendar.DATE)) {
            Calendar toCal1 = (Calendar) fromCal.clone();
            toCal.set(Calendar.HOUR_OF_DAY, 23);
            toCal.set(Calendar.MINUTE, 59);
            toCal.set(Calendar.SECOND, 59);

            realAddSleepRecord(fromCal, toCal1);

            Calendar fromCal2 = (Calendar) toCal.clone();
            fromCal.set(Calendar.HOUR_OF_DAY, 0);
            fromCal.set(Calendar.MINUTE, 0);
            fromCal.set(Calendar.SECOND, 0);
            realAddSleepRecord(fromCal2, toCal);
        } else {
            realAddSleepRecord(fromCal, toCal);
        }
    }

    private void realAddSleepRecord(Calendar from, Calendar to) {
        for (SleepRecord record : mDataSource) {
            if (DateUtils.sameDay(from, record.getDateTime())) {
                record.addSleepTime(new Pair<Date, Date>(from.getTime(), to.getTime()));
                break;
            }
        }
    }

    class SleepRecordViewHolder {

        View itemView;
        @BindView(R.id.monthTextView) TextView monthTextView;
        @BindView(R.id.dateTextView) TextView dateTextView;
        @BindView(R.id.sleepRecordView) SleepRecordView sleepRecordView;
        @BindArray(R.array.month_short) String[] monthShort;

        SleepRecordViewHolder(View itemView) {
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }

        void update(SleepRecord sleepRecord) {
            monthTextView.setText(monthShort[sleepRecord.getMonth()]);
            dateTextView.setText(String.valueOf(sleepRecord.getDate()));
            sleepRecordView.setSleepQuality(sleepRecord.getSleepQuality());
            sleepRecordView.setSleepTimePairs(sleepRecord.getSleepTimePairs());

            int color = SleepRecordUtils.getQualityColor(qualityLevelColors, sleepRecord.getSleepQuality());
            sleepRecordView.setSleepQualityColor(color);
            //force to update sleep record view
            sleepRecordView.invalidate();
        }
    }
}
