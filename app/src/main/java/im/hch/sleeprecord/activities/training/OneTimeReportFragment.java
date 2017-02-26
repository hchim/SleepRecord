package im.hch.sleeprecord.activities.training;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;

public class OneTimeReportFragment extends DialogFragment {
    private static final String ARG_TOTAL_TIME = "TotalTime";
    private static final String ARG_CRIED_OUT_TIMES = "CriedOutTimes";
    private static final String ARG_SOOTHE_TIMES = "SootheTimes";

    @BindView(R.id.elapsedTimeTextView) TextView elapsedTimeTextView;
    @BindView(R.id.criedOutTimesTextView) TextView criedOutTimesTextView;
    @BindView(R.id.sootheTimesTextView) TextView sootheTimesTextView;

    private long totalTimeInMillis;
    private int criedOutTimes;
    private int sootheTimes;

    public OneTimeReportFragment() {}

    public static OneTimeReportFragment newInstance(long totalTimeInMillis, int criedTimes, int sootheTimes) {
        OneTimeReportFragment fragment = new OneTimeReportFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TOTAL_TIME, totalTimeInMillis);
        args.putInt(ARG_CRIED_OUT_TIMES, criedTimes);
        args.putInt(ARG_SOOTHE_TIMES, sootheTimes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            totalTimeInMillis = bundle.getLong(ARG_TOTAL_TIME);
            criedOutTimes = bundle.getInt(ARG_CRIED_OUT_TIMES);
            sootheTimes = bundle.getInt(ARG_SOOTHE_TIMES);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_one_time_report, null, false);
        ButterKnife.bind(this, view);

        long time = totalTimeInMillis / 1000;
        elapsedTimeTextView.setText(String.format("%02d:%02d", time/60, time%60));
        criedOutTimesTextView.setText(String.format("%d", criedOutTimes));
        sootheTimesTextView.setText(String.format("%d", sootheTimes));

        builder.setView(view)
                .setTitle(R.string.training_result_title)
                // Add action buttons
                .setPositiveButton(R.string.alert_btn_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        OneTimeReportFragment.this.getDialog().cancel();
                    }
                });
        final AlertDialog dialog = builder.create();
        return dialog;
    }

}
