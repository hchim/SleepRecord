package im.hch.sleeprecord.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;

public class AddRecordActivity extends AppCompatActivity {
    public static final String TAG = AddRecordActivity.class.getSimpleName();
    private static final long MAX_SLEEP_TIME = 24 * 60 * 60;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.add_record_ProgressBar) ProgressBar addRecordProgressBar;
    @BindView(R.id.fall_asleep_date_editText) EditText fallAsleepDateEditText;
    @BindView(R.id.fall_asleep_time_editText) EditText fallAsleepTimeEditText;
    @BindView(R.id.wakeup_date_editText) EditText wakeupDateEditText;
    @BindView(R.id.wakeup_time_editText) EditText wakeupTimeEditText;
    @BindView(R.id.save_record_button) Button saveRecordButton;

    @BindString(R.string.failed_to_parse_fallasleep_time) String failureParseFallAsleepTime;
    @BindString(R.string.failed_to_parse_wakeup_time) String failureParseWakeupTime;
    @BindString(R.string.wakeup_time_before_fallasleep_time) String wakeupTimeBeforeFallAsleepTime;
    @BindString(R.string.time_between_too_long) String timeBetweenTooLong;

    private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        addRecordProgressBar.setVisibility(View.GONE);
    }

    private void saveSleepRecord() {
        addRecordProgressBar.setVisibility(View.VISIBLE);
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

        //TODO invoke service api
        addRecordProgressBar.setVisibility(View.GONE);
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
}
