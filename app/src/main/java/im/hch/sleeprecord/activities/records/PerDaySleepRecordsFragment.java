package im.hch.sleeprecord.activities.records;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sleepaiden.androidcommonutils.exceptions.AccountNotExistException;
import com.sleepaiden.androidcommonutils.exceptions.AuthFailureException;
import com.sleepaiden.androidcommonutils.exceptions.ConnectionFailureException;
import com.sleepaiden.androidcommonutils.exceptions.InternalServerException;

import java.util.Date;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.Metrics;
import im.hch.sleeprecord.MyAppConfig;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.models.SleepRecord;
import im.hch.sleeprecord.models.SleepRecordsPerDay;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.MetricHelper;
import im.hch.sleeprecord.utils.SessionManager;
import lombok.Setter;

/**
 * Created by huiche on 6/7/17.
 */

public class PerDaySleepRecordsFragment extends DialogFragment {
    public static final String TAG = PerDaySleepRecordsFragment.class.getSimpleName();
    public static final String ARG_SLEEP_RECORDS = "bundle.SleepRecords";
    public static final String ARG_SLEEP_RECORD_DATE = "bundle.SleepRecordDate";

    @BindView(R.id.sleepRecordsView) RecyclerView sleepRecordsList;

    @BindString(R.string.failed_to_parse_fallasleep_time) String failureParseFallAsleepTime;
    @BindString(R.string.failed_to_parse_wakeup_time) String failureParseWakeupTime;
    @BindString(R.string.wakeup_time_before_fallasleep_time) String wakeupTimeBeforeFallAsleepTime;
    @BindString(R.string.time_between_too_long) String timeBetweenTooLong;
    @BindString(R.string.wakeup_time_after_current_time) String wakeupTimeAfterCurrentTime;
    @BindString(R.string.progress_message_save) String progressMessageSave;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.error_sleep_record_overlap) String sleepRecordOverlapError;
    @BindString(R.string.error_auth_failure) String authError;
    @BindString(R.string.title_activity_sleep_records) String title;
    @BindString(R.string.display_date_format) String displayDateFormat;
    @BindString(R.string.display_datetime_format) String displayDateTimeFormat;

    private ProgressDialog progressDialog;
    private DeleteSleepRecordTask deleteSleepRecordTask;
    private SleepServiceClient sleepServiceClient;
    private SessionManager sessionManager;
    private LayoutInflater layoutInflater;
    @Setter
    private DeleteRecordDialogListener mListener;
    private MetricHelper metricHelper;
    private SleepRecord[] sleepRecords;
    private Long dateTime;
    private SleepRecordAdapter sleepRecordAdapter;

    public static PerDaySleepRecordsFragment newInstance(SleepRecordsPerDay sleepRecord) {
        PerDaySleepRecordsFragment fragment = new PerDaySleepRecordsFragment();
        if (sleepRecord != null) {
            Bundle args = new Bundle();
            args.putParcelableArray(ARG_SLEEP_RECORDS, sleepRecord.getSleepRecords());
            args.putLong(ARG_SLEEP_RECORD_DATE, sleepRecord.getLongTime());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            sleepRecords = (SleepRecord[]) bundle.getParcelableArray(ARG_SLEEP_RECORDS);
            dateTime = bundle.getLong(ARG_SLEEP_RECORD_DATE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        this.layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_per_day_sleep_records, null, false);
        ButterKnife.bind(this, view);
        init(getActivity());

        sleepRecordsList.setHasFixedSize(true);
        sleepRecordsList.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        sleepRecordAdapter = new SleepRecordAdapter();
        sleepRecordsList.setAdapter(sleepRecordAdapter);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setTitle(title + " : " + DateUtils.dateToStr(new Date(dateTime), displayDateFormat));
        return dialog;
    }

    private void init(Activity activity) {
        sessionManager = new SessionManager(activity);
        sleepServiceClient = new SleepServiceClient(MyAppConfig.getAppConfig());
        sleepServiceClient.setAccessToken(sessionManager.getAccessToken());
        metricHelper = new MetricHelper(activity);
    }

    private void handleDeleteFailure(String message) {
        Snackbar.make(sleepRecordsList, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private class DeleteSleepRecordTask extends AsyncTask<Void, Void, Boolean> {
        String recId;
        String errorMessage;

        public DeleteSleepRecordTask(String recId) {
            this.recId = recId;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(PerDaySleepRecordsFragment.this.getActivity(), progressMessageSave);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            deleteSleepRecordTask = null;
            
            if (result) {
                if (mListener != null) {
                    mListener.onSleepRecordDeleted();
                }
                PerDaySleepRecordsFragment.this.dismiss();
            } else {
                if (errorMessage == authError) {
                    ActivityUtils.navigateToLoginActivity(PerDaySleepRecordsFragment.this.getActivity());
                } else {
                    handleDeleteFailure(errorMessage);
                }
            }
        }

        @Override
        protected void onCancelled() {
            progressDialog.dismiss();
            deleteSleepRecordTask = null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                sleepServiceClient.deleteSleepRecord(recId);
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
                metricHelper.errorMetric(Metrics.ADD_SLEEP_RECORD_ERROR_METRIC, e);
            } catch (AuthFailureException | AccountNotExistException e) {
                errorMessage = authError;
            }
            return false;
        }
    }

    public interface DeleteRecordDialogListener {
        void onSleepRecordDeleted();
    }

    private class SleepRecordAdapter extends RecyclerView.Adapter<SleepRecordViewHolder> {

        @Override
        public int getItemCount() {
            return sleepRecords.length;
        }

        @Override
        public SleepRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = layoutInflater.inflate(R.layout.list_item_sleeprecords_perday, parent, false);
            SleepRecordViewHolder viewHolder = new SleepRecordViewHolder(convertView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(SleepRecordViewHolder viewHolder, int position) {
            viewHolder.update(sleepRecords[position]);
        }
    }

    class SleepRecordViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        String recId;
        @BindView(R.id.titleView) TextView titleView;
        @BindView(R.id.delButton) ImageButton delButton;

        public SleepRecordViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }

        public void update(final SleepRecord sleepRecord) {
            String title =
                    DateUtils.dateToStr(sleepRecord.getRecordSleepTime(), displayDateTimeFormat)
                    + " - "
                    + DateUtils.dateToStr(sleepRecord.getRecordWakeupTime(), displayDateTimeFormat);
            titleView.setText(title);
            recId = sleepRecord.getRecordId();
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deleteSleepRecordTask != null) {
                        return;
                    }

                    deleteSleepRecordTask = new DeleteSleepRecordTask(recId);
                    deleteSleepRecordTask.execute();
                }
            });
        }
    }
}
