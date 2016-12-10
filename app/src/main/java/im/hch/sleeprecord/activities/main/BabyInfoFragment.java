package im.hch.sleeprecord.activities.main;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import java.util.Calendar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.models.BabyInfo;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.ProgressBarHelper;
import im.hch.sleeprecord.utils.SessionManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BabyInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BabyInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BabyInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private SleepServiceClient sleepServiceClient;
    private SaveUserInfoTask mSaveUserInfoTask;
    private ProgressBarHelper progressBarHelper;
    private SessionManager sessionManager;

    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.babyinfo_form) View mBabyInfoView;
    @BindView(R.id.babyinfo_save_btn) Button mBabyInfoSaveButton;
    @BindView(R.id.babyname) EditText mBabyNameView;
    @BindView(R.id.baby_birthday) EditText mBabyBirthdayView;
    @BindView(R.id.boyRadioButton) RadioButton boyRadioButton;
    @BindView(R.id.girlRadioButton) RadioButton girlRadioButton;

    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_gender_required) String invalidGenderError;

    public BabyInfoFragment() {
        sleepServiceClient = new SleepServiceClient();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BabyInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BabyInfoFragment newInstance(String param1, String param2) {
        BabyInfoFragment fragment = new BabyInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_baby_info, container, false);
        ButterKnife.bind(view);

        progressBarHelper = new ProgressBarHelper(mProgressBar, mBabyInfoView, null);
        mBabyInfoSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSaveInfo();
            }
        });

        mBabyBirthdayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showDatePickerDialog(mBabyBirthdayView, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        final Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        mBabyBirthdayView.setText(DateUtils.dateToStr(c.getTime()));
                    }
                });
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sessionManager = new SessionManager(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        sessionManager = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private int getBabyGender() {
        if (boyRadioButton.isChecked()) {
            return 1;
        } else if (girlRadioButton.isChecked()) {
            return 2;
        } else {
            return 0;
        }
    }

    private void attemptSaveInfo() {
        if (mSaveUserInfoTask != null) {
            return;
        }

        // Reset errors.
        mBabyNameView.setError(null);
        mBabyBirthdayView.setError(null);
        girlRadioButton.setError(null);

        String babyName = mBabyNameView.getText().toString();
        String babyBirthday = mBabyBirthdayView.getText().toString();
        int gender = getBabyGender();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(babyName)) {
            mBabyNameView.setError(requiredFieldError);
            focusView = mBabyNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(babyBirthday)) {
            mBabyBirthdayView.setError(requiredFieldError);
            focusView = mBabyBirthdayView;
            cancel = true;
        } else if (gender == 0) {
            girlRadioButton.setError(invalidGenderError);
            focusView = boyRadioButton;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            progressBarHelper.show();

            BabyInfo babyInfo = new BabyInfo();
            babyInfo.setBabyName(babyName);
            babyInfo.setBabyGender(BabyInfo.Gender.create(gender));
            babyInfo.setBabyBirthday(DateUtils.strToDate(babyBirthday));

            mSaveUserInfoTask = new SaveUserInfoTask(babyInfo);
            mSaveUserInfoTask.execute((Void) null);
        }
    }

    /**
     * This task saves user info to the remote service.
     */
    public class SaveUserInfoTask extends AsyncTask<Void, Void, Boolean> {

        private BabyInfo babyInfo;

        public SaveUserInfoTask(BabyInfo babyInfo) {
            this.babyInfo = babyInfo;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (sessionManager != null) {
                String loginid = sessionManager.getLoginId();
                if (loginid != null) {
                    sleepServiceClient.saveBabyInfo(this.babyInfo, loginid);
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveUserInfoTask = null;
            progressBarHelper.hide();

            if (success) {
                Activity parent = BabyInfoFragment.this.getActivity();
                if (parent != null) {
                    ActivityUtils.navigateToMainActivity(parent);
                }
            } else {
                //TODO show error message
                mBabyNameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mSaveUserInfoTask = null;
            progressBarHelper.hide();
        }
    }
}
