package im.hch.sleeprecord.activities.training;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sleepaiden.androidcommonutils.exceptions.AccountNotExistException;
import com.sleepaiden.androidcommonutils.exceptions.AuthFailureException;
import com.sleepaiden.androidcommonutils.exceptions.ConnectionFailureException;
import com.sleepaiden.androidcommonutils.exceptions.InternalServerException;

import java.util.Date;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.MyAppConfig;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;
import im.hch.sleeprecord.models.SleepTrainingPlan;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

public class PlanningFragment extends BaseFragment {
    public static final int MIN_PROGRESS = 1;

    @BindView(R.id.seekBar11) SeekBar seekBar11;
    @BindView(R.id.seekBar12) SeekBar seekBar12;
    @BindView(R.id.seekBar13) SeekBar seekBar13;
    @BindView(R.id.seekBar14) SeekBar seekBar14;
    @BindView(R.id.seekBar21) SeekBar seekBar21;
    @BindView(R.id.seekBar22) SeekBar seekBar22;
    @BindView(R.id.seekBar23) SeekBar seekBar23;
    @BindView(R.id.seekBar24) SeekBar seekBar24;
    @BindView(R.id.seekBar31) SeekBar seekBar31;
    @BindView(R.id.seekBar32) SeekBar seekBar32;
    @BindView(R.id.seekBar33) SeekBar seekBar33;
    @BindView(R.id.seekBar34) SeekBar seekBar34;

    @BindView(R.id.timeLabel11) TextView timeLabel11;
    @BindView(R.id.timeLabel12) TextView timeLabel12;
    @BindView(R.id.timeLabel13) TextView timeLabel13;
    @BindView(R.id.timeLabel14) TextView timeLabel14;
    @BindView(R.id.timeLabel21) TextView timeLabel21;
    @BindView(R.id.timeLabel22) TextView timeLabel22;
    @BindView(R.id.timeLabel23) TextView timeLabel23;
    @BindView(R.id.timeLabel24) TextView timeLabel24;
    @BindView(R.id.timeLabel31) TextView timeLabel31;
    @BindView(R.id.timeLabel32) TextView timeLabel32;
    @BindView(R.id.timeLabel33) TextView timeLabel33;
    @BindView(R.id.timeLabel34) TextView timeLabel34;

    @BindView(R.id.fab) FloatingActionButton floatingActionButton;

    @BindString(R.string.minute_short) String minuteShort;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.sleep_training_plan_title) String title;
    @BindString(R.string.error_auth_failure) String authError;

    private SharedPreferenceUtil sharedPreferenceUtil;
    private SessionManager sessionManager;
    private SleepServiceClient sleepServiceClient;
    SaveTrainingPlanTask saveTrainingPlanTask;
    private SeekBar[] seekBars;
    private TextView[] timeLabels;

    public static PlanningFragment newInstance() {
        return new PlanningFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_planning, container, false);
        ButterKnife.bind(this, view);

        mainActivity.setTitle(title);

        sharedPreferenceUtil = new SharedPreferenceUtil(getActivity());
        sleepServiceClient = new SleepServiceClient(MyAppConfig.getAppConfig());
        sessionManager = new SessionManager(getActivity());
        sleepServiceClient.setAccessToken(sessionManager.getAccessToken());

        seekBars = new SeekBar[] {
                        seekBar11, seekBar12, seekBar13, seekBar14,
                        seekBar21, seekBar22, seekBar23, seekBar24,
                        seekBar31, seekBar32, seekBar33, seekBar34
                };
        timeLabels = new TextView[] {
                timeLabel11, timeLabel12, timeLabel13, timeLabel14,
                timeLabel21, timeLabel22, timeLabel23, timeLabel24,
                timeLabel31, timeLabel32, timeLabel33, timeLabel34
        };

        for (int i = 0; i < seekBars.length; i++) {
            setOnSeekBarChangeListener(seekBars[i], timeLabels[i]);
            timeLabels[i].setText(seekBars[i].getProgress() + minuteShort);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptStartTraining();
            }
        });
        return view;
    }

    private void setOnSeekBarChangeListener(SeekBar seekBar, final TextView timeLabel) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int minProgress = getMinProgress(seekBar);
                int maxProgress = getMaxProgress(seekBar);
                if (progress < minProgress) {
                    progress = minProgress;
                } else if (progress > maxProgress) {
                    progress = maxProgress;
                }

                seekBar.setProgress(progress);
                timeLabel.setText(progress + minuteShort);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private int getMinProgress(SeekBar seekBar) {
        if (seekBar == seekBar12) {
            return seekBar11.getProgress();
        } else if (seekBar == seekBar13) {
            return seekBar12.getProgress();
        } else if (seekBar == seekBar22) {
            return seekBar21.getProgress();
        } else if (seekBar == seekBar23) {
            return seekBar22.getProgress();
        } else if (seekBar == seekBar32) {
            return seekBar31.getProgress();
        } else if (seekBar == seekBar33) {
            return seekBar32.getProgress();
        }

        return MIN_PROGRESS;
    }

    private int getMaxProgress(SeekBar seekBar) {
        if (seekBar == seekBar11) {
            return seekBar12.getProgress();
        } else if (seekBar == seekBar12) {
            return seekBar13.getProgress();
        } else if (seekBar == seekBar21) {
            return seekBar22.getProgress();
        } else if (seekBar == seekBar22) {
            return seekBar23.getProgress();
        } else if (seekBar == seekBar31) {
            return seekBar32.getProgress();
        } else if (seekBar == seekBar32) {
            return seekBar33.getProgress();
        }

        return seekBar.getMax();
    }

    private SleepTrainingPlan getSleepTrainingPlan() {
        SleepTrainingPlan plan = new SleepTrainingPlan(new Date(System.currentTimeMillis()));
        plan.setFirstWeekTime(
                seekBar14.getProgress(), seekBar11.getProgress(),
                seekBar12.getProgress(), seekBar13.getProgress());
        plan.setSecondWeekTime(
                seekBar24.getProgress(), seekBar21.getProgress(),
                seekBar22.getProgress(), seekBar23.getProgress());
        plan.setFollowingWeekTime(
                seekBar34.getProgress(), seekBar31.getProgress(),
                seekBar32.getProgress(), seekBar33.getProgress());
        return plan;
    }

    private void attemptStartTraining() {
        if (saveTrainingPlanTask != null) {
            return;
        }

        saveTrainingPlanTask = new SaveTrainingPlanTask(getSleepTrainingPlan());
        saveTrainingPlanTask.execute();
    }

    private class SaveTrainingPlanTask extends AsyncTask<Void, Void, Boolean> {
        SleepTrainingPlan plan;
        String errorMessage;

        public SaveTrainingPlanTask(SleepTrainingPlan plan) {
            this.plan = plan;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                sleepServiceClient.saveSleepTrainingPlan(plan);
                sharedPreferenceUtil.storeSleepTrainingPlan(plan);
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
            } catch (AuthFailureException | AccountNotExistException e) {
                errorMessage = authError;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                mainActivity.loadFragment(SleepTrainingFragment.newInstance(), null);
            } else {
                if (errorMessage == authError) {
                    ActivityUtils.navigateToLoginActivity(mainActivity);
                } else {
                    Snackbar.make(seekBar11, errorMessage, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
            saveTrainingPlanTask = null;
        }
    }
}
