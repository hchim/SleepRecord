package im.hch.sleeprecord.activities.records;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.main.AddRecordDialogFragment;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;

public class SleepRecordsActivity extends AppCompatActivity implements AddRecordDialogFragment.AddRecordDialogListener {
    public static final String TAG = "SleepRecordsActivity";
    /**
     * The number of sleep records to show in the sleep records widget.
     */
    public static final int SHOW_SLEEP_RECORDS_NUM = 30;
    public static final int THRESHOLD = 1;

    private SleepRecordsAdapter sleepRecordsAdapter;
    private SessionManager sessionManager;
    private SleepServiceClient sleepServiceClient;
    private LoadRemoteDataTask loadRemoteDataTask;
    private int page = 0;
    private boolean isLoading = false;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.list_view) ListView listView;

    @BindString(R.string.progress_message_load) String loadingMessage;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_records);
        ButterKnife.bind(this);

        sessionManager = new SessionManager(this);
        sleepServiceClient = new SleepServiceClient();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sleepRecordsAdapter = new SleepRecordsAdapter(this, new ArrayList<SleepRecord>());
        listView.setAdapter(sleepRecordsAdapter);
        new LoadRemoteDataTask().execute(page);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int count = listView.getCount();

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (listView.getLastVisiblePosition() >= count - THRESHOLD) {
                        Log.i(TAG, "Loading more data");
                        // Execute LoadMoreDataTask AsyncTask
                        new LoadRemoteDataTask().execute(page);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });
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

    private class LoadRemoteDataTask extends AsyncTask<Integer, Void, Boolean> {

        List<SleepRecord> sleepRecords;
        ProgressDialog progressDialog;
        String errorMessage;

        @Override
        protected void onPreExecute() {
            isLoading = true;
            progressDialog = DialogUtils.showProgressDialog(SleepRecordsActivity.this, loadingMessage);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            String userId = sessionManager.getUserId();
            if (userId == null) {
                Log.wtf(MainActivity.TAG, "User id is null.");
                return false;
            }

            //update sleep records
            Calendar to = Calendar.getInstance();
            Calendar from = Calendar.getInstance();
            to.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1 * page);
            from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1 * (page + 1));

            try {
                sleepRecords = sleepServiceClient.getSleepRecords(userId, from.getTime(), to.getTime());
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            isLoading = false;

            if (aBoolean) {
                page += 1;
                sleepRecordsAdapter.addSleepRecords(sleepRecords);
            } else {
                Snackbar.make(listView, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}
