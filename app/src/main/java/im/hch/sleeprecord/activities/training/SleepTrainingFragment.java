package im.hch.sleeprecord.activities.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindString;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.activities.BaseFragment;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;

public class SleepTrainingFragment extends BaseFragment {

    @BindString(R.string.sleep_training_title) String title;

    private SleepServiceClient sleepServiceClient;

    public SleepTrainingFragment() {
        sleepServiceClient = new SleepServiceClient();
    }

    public static SleepTrainingFragment newInstance() {
        SleepTrainingFragment fragment = new SleepTrainingFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_sleep_training, container, false);
        ButterKnife.bind(this, view);

        mainActivity.setTitle(title);

        return view;
    }

}
