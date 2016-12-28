package im.hch.sleeprecord.activities.records;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.main.AddRecordDialogFragment;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.utils.DialogUtils;

public class SleepRecordsActivity extends AppCompatActivity implements AddRecordDialogFragment.AddRecordDialogListener {

    private SleepRecordsAdapter sleepRecordsAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.list_view) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_records);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO replace these default data with real data
        List<Pair<Date, Date>> sleepTimes = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date begin, end;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 15);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 20);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 33);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 20);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 30);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        begin = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        end = calendar.getTime();
        sleepTimes.add(new Pair<Date, Date>(begin, end));

        //test data
        ArrayList<SleepRecord> records = new ArrayList<>();
        for (int i = 31; i >= 1; i--) {
            SleepRecord sleepRecord = new SleepRecord(11, i);
            sleepRecord.setSleepTimePairs(sleepTimes);
            records.add(sleepRecord);
        }

        sleepRecordsAdapter = new SleepRecordsAdapter(this, records);
        listView.setAdapter(sleepRecordsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_sleep_record) {
            DialogUtils.showAddRecordDialog(getFragmentManager());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSleepRecordSaved(Date from, Date to) {
        //TODO reload add records
    }
}
