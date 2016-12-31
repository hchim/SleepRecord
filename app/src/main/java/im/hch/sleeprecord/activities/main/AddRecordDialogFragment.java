package im.hch.sleeprecord.activities.main;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;

public class AddRecordDialogFragment extends DialogFragment {
    public static final String TAG = AddRecordDialogFragment.class.getSimpleName();
    private static final long MAX_SLEEP_TIME = 24 * 60 * 60;

    @BindView(R.id.fall_asleep_date_editText) EditText fallAsleepDateEditText;
    @BindView(R.id.fall_asleep_time_editText) EditText fallAsleepTimeEditText;
    @BindView(R.id.wakeup_date_editText) EditText wakeupDateEditText;
    @BindView(R.id.wakeup_time_editText) EditText wakeupTimeEditText;
    @BindView(R.id.save_record_button) Button saveRecordButton;

    @BindString(R.string.failed_to_parse_fallasleep_time) String failureParseFallAsleepTime;
    @BindString(R.string.failed_to_parse_wakeup_time) String failureParseWakeupTime;
    @BindString(R.string.wakeup_time_before_fallasleep_time) String wakeupTimeBeforeFallAsleepTime;
    @BindString(R.string.time_between_too_long) String timeBetweenTooLong;
    @BindString(R.string.title_activity_add_record) String title;
    @BindString(R.string.progress_message_save) String progressMessageSave;

    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    private static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    private ProgressDialog progressDialog;
    private SaveSleepRecordTask saveSleepRecordTask;
    private SleepServiceClient sleepServiceClient;
    private SessionManager sessionManager;
    private AddRecordDialogListener mListener;

    public static AddRecordDialogFragment newInstance() {
        AddRecordDialogFragment fragment = new AddRecordDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_add_record, container, false);
        ButterKnife.bind(this, view);

        Context context = getActivity();
        sleepServiceClient = new SleepServiceClient();
        sessionManager = new SessionManager(context);

        if (context instanceof AddRecordDialogListener) {
            mListener = (AddRecordDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AddRecordDialogListener");
        }

        getDialog().setTitle(title);
        resetDatetime();

        fallAsleepDateEditText.setOnClickListener(new DatePickOnClickListener());
        fallAsleepTimeEditText.setOnClickListener(new TimePickOnClickListener());
        wakeupDateEditText.setOnClickListener(new DatePickOnClickListener());
        wakeupTimeEditText.setOnClickListener(new TimePickOnClickListener());
        saveRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSleepRecord();
            }
        });

        return view;
    }

    private void resetDatetime() {
        Calendar now = Calendar.getInstance();
        wakeupDateEditText.setText(dateFormat.format(now.getTime()));
        wakeupTimeEditText.setText(timeFormat.format(now.getTime()));

        // set default fall asleep time
        now.add(Calendar.HOUR, -1);
        fallAsleepDateEditText.setText(dateFormat.format(now.getTime()));
        fallAsleepTimeEditText.setText(timeFormat.format(now.getTime()));
    }

    private void handleSaveFailure(String message) {
        Snackbar.make(saveRecordButton, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void saveSleepRecord() {
        Calendar from = getDatetime(fallAsleepDateEditText, fallAsleepTimeEditText);
        if (from == null) {
            handleSaveFailure(failureParseFallAsleepTime);
            return;
        }

        //TODO from time should after last fallasleep time

        Calendar to = getDatetime(wakeupDateEditText, wakeupTimeEditText);
        if (to == null) {
            handleSaveFailure(failureParseWakeupTime);
            return;
        }

        if (from.after(to)) {
            handleSaveFailure(wakeupTimeBeforeFallAsleepTime);
            return;
        }

        long diff = (to.getTimeInMillis() - from.getTimeInMillis()) / 1000;
        if (diff > MAX_SLEEP_TIME) { //larger than 24 hours
            handleSaveFailure(timeBetweenTooLong);
            return;
        }

        saveSleepRecordTask = new AddRecordDialogFragment.SaveSleepRecordTask(from.getTime(), to.getTime());
        saveSleepRecordTask.execute((Void) null);
    }

    private Calendar getDatetime(EditText dateEdit, EditText timeEdit) {
        Calendar date = getDate(dateEdit);
        Calendar time = getTime(timeEdit);
        if (date == null || time == null) {
            return null;
        }

        date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        return date;
    }

    private Calendar getDate(EditText editText) {
        String dateStr = editText.getText().toString();
        try {
            Date date = dateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse date str: " + dateStr);
            return null;
        }
    }

    private Calendar getTime(EditText editText) {
        String timeStr = editText.getText().toString();
        try {
            Date date = timeFormat.parse(timeStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse time str: " + timeStr);
            return null;
        }
    }

    final class DatePickOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText editText = (EditText) v;
            DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    editText.setText(dateFormat.format(calendar.getTime()));
                }
            };

            Calendar calendar = getDate(editText);
            if (calendar != null) {
                DatePickerDialog datePickerDialog =
                        new DatePickerDialog(v.getContext(), R.style.AppTheme, listener,
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        }
    }

    final class TimePickOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText editText = (EditText) v;
            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    editText.setText(timeFormat.format(calendar.getTime()));
                }
            };

            Calendar calendar = getTime(editText);
            if (calendar != null) {
                TimePickerDialog timePickerDialog =
                        new TimePickerDialog(v.getContext(), R.style.AppTheme, listener,
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true);
                timePickerDialog.show();
            }
        }
    }

    private class SaveSleepRecordTask extends AsyncTask<Void, Void, Boolean> {

        Date from;
        Date to;

        public SaveSleepRecordTask(Date from, Date to) {
            this.from = from;
            this.to = to;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(AddRecordDialogFragment.this.getActivity(), progressMessageSave);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();

            if (result) {
                if (mListener != null) {
                    mListener.onSleepRecordSaved(from, to);
                }
                AddRecordDialogFragment.this.dismiss();
            } else {
                //TODO show error message
            }
        }

        @Override
        protected void onCancelled() {
            progressDialog.dismiss();
            saveSleepRecordTask = null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            sleepServiceClient.addSleepRecord(from, to, sessionManager.getUserId());
            return true;
        }
    }

    public interface AddRecordDialogListener {
        public void onSleepRecordSaved(Date from, Date to);
    }
}