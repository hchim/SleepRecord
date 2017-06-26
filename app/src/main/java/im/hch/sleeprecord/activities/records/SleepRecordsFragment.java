package im.hch.sleeprecord.activities.records;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sleepaiden.androidcommonutils.exceptions.AccountNotExistException;
import com.sleepaiden.androidcommonutils.exceptions.AuthFailureException;
import com.sleepaiden.androidcommonutils.exceptions.ConnectionFailureException;
import com.sleepaiden.androidcommonutils.exceptions.InternalServerException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.Metrics;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;
import im.hch.sleeprecord.activities.main.AddRecordDialogFragment;
import im.hch.sleeprecord.models.SleepRecordsPerDay;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;

public class SleepRecordsFragment extends BaseFragment implements AddRecordDialogFragment.AddRecordDialogListener {
    public static final String TAG = "SleepRecordsFragment";
    /**
     * The number of sleep records to show in the sleep records widget.
     */
    public static final int SHOW_SLEEP_RECORDS_NUM = 30;
    public static final int THRESHOLD = 1;

    private SleepRecordsAdapter sleepRecordsAdapter;

    private int page = 0;

    @BindView(R.id.list_view) ListView listView;
    @BindView(R.id.fab) FloatingActionButton fab;

    @BindString(R.string.progress_message_load) String loadingMessage;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.title_activity_sleep_records) String title;
    @BindString(R.string.error_auth_failure) String authError;

    public static SleepRecordsFragment newInstance() {
        SleepRecordsFragment fragment = new SleepRecordsFragment();
        return fragment;
    }

    public SleepRecordsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_sleep_records, container, false);
        ButterKnife.bind(this, view);

        mainActivity.setTitle(title);

        sleepRecordsAdapter = new SleepRecordsAdapter(getActivity(), new ArrayList<SleepRecordsPerDay>());
        sleepRecordsAdapter.setShowDivider(true);
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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DialogUtils.showSleepRecordsPerDay(getFragmentManager(),
                        (SleepRecordsPerDay) sleepRecordsAdapter.getItem(position));
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showAddRecordDialog(getFragmentManager(), SleepRecordsFragment.this);
            }
        });
        return view;
    }

    @Override
    public void onSleepRecordSaved(Date from, Date to) {
        page = 0; // reset page to first page
        new LoadRemoteDataTask().execute(page);
    }

    private class LoadRemoteDataTask extends AsyncTask<Integer, Void, Boolean> {

        List<SleepRecordsPerDay> sleepRecords;
        ProgressDialog progressDialog;
        String errorMessage;

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(SleepRecordsFragment.this.getActivity(), loadingMessage);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            int page = params[0];
            //update sleep records
            Calendar to = Calendar.getInstance();
            Calendar from = Calendar.getInstance();
            to.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1 * page);
            from.add(Calendar.DATE, SHOW_SLEEP_RECORDS_NUM * -1 * (page + 1));

            try {
                sleepRecords = mainActivity.sleepServiceClient.getSleepRecords(from.getTime(), to.getTime());
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
                metricHelper.errorMetric(Metrics.GET_SLEEP_RECORDS_ERROR_METRIC, e);
            } catch (AuthFailureException | AccountNotExistException e) {
                errorMessage = authError;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();

            if (aBoolean) {
                page += 1;
                sleepRecordsAdapter.addSleepRecords(sleepRecords);
            } else {
                if (errorMessage == authError) {
                    ActivityUtils.navigateToLoginActivity(mainActivity);
                } else {
                    Snackbar.make(listView, errorMessage, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }
    }
}
