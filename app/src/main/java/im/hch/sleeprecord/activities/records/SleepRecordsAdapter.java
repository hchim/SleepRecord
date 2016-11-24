package im.hch.sleeprecord.activities.records;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindArray;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.models.SleepRecord;

public class SleepRecordsAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<SleepRecord> mDataSource;

    public SleepRecordsAdapter(Context context, ArrayList<SleepRecord> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        }
    }
}
