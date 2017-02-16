package im.hch.sleeprecord.activities;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.hch.sleeprecord.utils.MetricHelper;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {
    protected SessionManager sessionManager;
    protected SharedPreferenceUtil sharedPreferenceUtil;
    protected MetricHelper metricHelper;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sessionManager = new SessionManager(getActivity());
        sharedPreferenceUtil = new SharedPreferenceUtil(getActivity());
        metricHelper = new MetricHelper(getActivity());

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
