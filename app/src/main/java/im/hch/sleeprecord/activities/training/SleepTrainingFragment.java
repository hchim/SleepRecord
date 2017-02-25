package im.hch.sleeprecord.activities.training;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;
import im.hch.sleeprecord.models.SleepTrainingPlan;
import im.hch.sleeprecord.views.CountDownTextView;
import im.hch.sleeprecord.views.CountUpTextView;

public class SleepTrainingFragment extends BaseFragment {
    public static final String TAG = "SleepTrainingFragment";
    public static final long VIBRATE_TIME = 1000;

    @BindString(R.string.sleep_training_title) String title;
    @BindString(R.string.day_x) String dayX;
    @BindString(R.string.training_begin_desc) String trainingStageStartDesc;
    @BindString(R.string.training_crying_desc) String traingStageCryingDesc;
    @BindString(R.string.training_soothe_desc) String traingStageSootheDesc;
    @BindString(R.string.default_count_time) String defaultCountTime;

    @BindView(R.id.dayXTextView) TextView dayXTextView;
    @BindView(R.id.totalTimeTextView) CountUpTextView countUpTextView;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.stageInfoTextView) TextView stageInfoTextView;
    @BindView(R.id.stageCheckbox) CheckBox stageCheckBox;
    @BindView(R.id.countDownTextView) CountDownTextView countDownTextView;
    @BindView(R.id.finishCheckbox) CheckBox finishCheckBox;

    private SleepTrainingPlan sleepTrainingPlan;
    private SleepTrainingPlan.TrainingPlanTime currentSleepTrainingTime;
    private TrainingStage currentStage = TrainingStage.START;
    private int criedOutTimes = 0;
    private int sootheTimes = 0;
    private Vibrator vibrator;

    public static SleepTrainingFragment newInstance() {
        SleepTrainingFragment fragment = new SleepTrainingFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_sleep_training, container, false);
        vibrator = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

        ButterKnife.bind(this, view);

        sleepTrainingPlan = sharedPreferenceUtil.retrieveSleepTrainingPlan();
        if (sleepTrainingPlan == null) {
            Log.wtf(TAG, "Sleep Training plan is null.");
        }
        currentSleepTrainingTime = sleepTrainingPlan.currentTrainingPlanTime();

        mainActivity.setTitle(title);
        dayXTextView.setText(String.format(dayX, sleepTrainingPlan.trainingStartedDays()));
        countDownTextView.setVisibility(View.GONE);//hide count down text view

        stageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    return;
                }
                onStageCheckboxChecked();
            }
        });
        finishCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    return;
                }
                switchToStage(TrainingStage.FINISHED);
                //TODO show report
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sleep_training, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_training_reset:
                switchToStage(TrainingStage.START);
                break;
//            case R.id.action_training_history:
//                break;
//            case R.id.action_training_report:
//                break;
            case R.id.action_training_reset_plan:
                resetSleepTrainingPlan();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetSleepTrainingPlan() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_message_reset_training_plan)
                .setPositiveButton(R.string.alert_btn_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sharedPreferenceUtil.removeSleepTrainingPlan();
                        //TODO reset remotely
                        mainActivity.loadFragment(ChecklistFragment.newInstance(), null);
                    }
                })
                .setNegativeButton(R.string.alert_btn_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    /**
     * When the stage checkbox checked.
     */
    private void onStageCheckboxChecked() {
        switch (currentStage) {
            case START:
                //start count up timer
                countUpTextView.start(1000);
                //change two stage two
                switchToStage(TrainingStage.CRYING);
                break;
            case CRYING:
                //increase cried out times
                criedOutTimes += 1;
                //start the count down timer
                countDownTextView.start(
                        currentSleepTrainingTime.getCriedOutTime(criedOutTimes),
                        new CountDownTextView.OnFinishCallback() {
                            @Override
                            public void onFinish() {
                                vibrator.vibrate(VIBRATE_TIME);
                                switchToStage(TrainingStage.SOOTHE);
                            }
                        });
                //disable checkbox
                stageCheckBox.setEnabled(false);
                break;
            case SOOTHE:
                //increase soothe times
                sootheTimes += 1;
                //start count down timer
                countDownTextView.start(
                        currentSleepTrainingTime.getSootheTime(),
                        new CountDownTextView.OnFinishCallback() {
                            @Override
                            public void onFinish() {
                                vibrator.vibrate(VIBRATE_TIME);
                                switchToStage(TrainingStage.CRYING);
                            }
                        }
                );
                //disable checkbox
                stageCheckBox.setEnabled(false);
                break;
        }
    }

    /**
     * Switch to a stage.
     * @param stage
     */
    private void switchToStage(TrainingStage stage) {
        switch (stage) {
            case START:
                countUpTextView.stop();
                countUpTextView.setText(defaultCountTime);
                stageInfoTextView.setText(trainingStageStartDesc);
                imageView.setImageResource(R.mipmap.put_baby_to_crib);
                stageCheckBox.setChecked(false);
                stageCheckBox.setEnabled(true);
                countDownTextView.stop();
                countDownTextView.setText(defaultCountTime);
                countDownTextView.setVisibility(View.GONE);
                finishCheckBox.setChecked(false);
                finishCheckBox.setEnabled(true);
                criedOutTimes = 0;
                sootheTimes = 0;
                break;
            case CRYING:
                stageInfoTextView.setText(traingStageCryingDesc);
                countDownTextView.setVisibility(View.VISIBLE);
                countDownTextView.stop();
                countDownTextView.setText(defaultCountTime);
                stageCheckBox.setChecked(false);
                stageCheckBox.setEnabled(true);
                imageView.setImageResource(R.mipmap.crying_in_crib);
                break;
            case SOOTHE:
                stageInfoTextView.setText(traingStageSootheDesc);
                countDownTextView.setVisibility(View.VISIBLE);
                countDownTextView.stop();
                countDownTextView.setText(defaultCountTime);
                stageCheckBox.setChecked(false);
                stageCheckBox.setEnabled(true);
                imageView.setImageResource(R.mipmap.soothe_in_crib);
                break;
            case FINISHED:
                countUpTextView.stop();
                countDownTextView.stop();
                finishCheckBox.setEnabled(false);
                stageCheckBox.setEnabled(false);
                break;
        }
        currentStage = stage;
    }

    private enum TrainingStage {
        START,   //put the baby in the crib and left the room
        CRYING,  //The baby started to cry or is crying
        SOOTHE,  //Soothe the baby without picking up
        FINISHED
    }
}