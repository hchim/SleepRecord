package im.hch.sleeprecord.activities.main;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import im.hch.sleeprecord.serviceclients.SleepServiceClient;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DateUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BabyInfoDialogFragmentListener} interface
 * to handle interaction events.
 * Use the {@link BabyInfoDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BabyInfoDialogFragment extends DialogFragment {
    private static final String ARG_BABY_NAME = "BabyName";
    private static final String ARG_BABY_BIRTHDAY = "BabyBirthday";
    private static final String ARG_BABY_GENDER = "BabyGender";

    private String babyName = "";
    private String babyBirthday = "";
    private BabyInfo.Gender babyGender = BabyInfo.Gender.Boy;

    private BabyInfoDialogFragmentListener mListener;
    private SleepServiceClient sleepServiceClient;
    private SaveUserInfoTask mSaveUserInfoTask;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private SharedPreferenceUtil sharedPreferenceUtil;

    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.babyinfo_form) View mBabyInfoView;
    @BindView(R.id.babyinfo_save_btn) Button mBabyInfoSaveButton;
    @BindView(R.id.babyname) EditText mBabyNameView;
    @BindView(R.id.baby_birthday) EditText mBabyBirthdayView;
    @BindView(R.id.boyRadioButton) RadioButton boyRadioButton;
    @BindView(R.id.girlRadioButton) RadioButton girlRadioButton;

    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_gender_required) String invalidGenderError;
    @BindString(R.string.babyinfo_fragment_title) String title;
    @BindString(R.string.progress_message_save) String progressMessageSave;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param babyInfo Baby information
     * @return A new instance of fragment BabyInfoDialogFragment.
     */
    public static BabyInfoDialogFragment newInstance(BabyInfo babyInfo) {
        BabyInfoDialogFragment fragment = new BabyInfoDialogFragment();
        if (babyInfo != null) {
            Bundle args = new Bundle();
            args.putString(ARG_BABY_NAME, babyInfo.getBabyName());
            args.putString(ARG_BABY_BIRTHDAY, DateUtils.dateToStr(babyInfo.getBabyBirthday()));
            args.putInt(ARG_BABY_GENDER, babyInfo.getBabyGender().getValue());
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        if (bundle != null) {
            babyName = bundle.getString(ARG_BABY_NAME);
            babyBirthday = bundle.getString(ARG_BABY_BIRTHDAY);
            babyGender = BabyInfo.Gender.create(bundle.getInt(ARG_BABY_GENDER));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_baby_info, container, false);
        ButterKnife.bind(this, view);

        Context context = this.getActivity();
        sessionManager = new SessionManager(context);
        sharedPreferenceUtil = new SharedPreferenceUtil(context);
        sleepServiceClient = new SleepServiceClient();

        if (context instanceof BabyInfoDialogFragmentListener) {
            mListener = (BabyInfoDialogFragmentListener) context;
        }

        getDialog().setTitle(title);
        mBabyNameView.setText(babyName);
        mBabyBirthdayView.setText(babyBirthday);
        if (babyGender == BabyInfo.Gender.Boy) {
            boyRadioButton.setChecked(true);
            girlRadioButton.setChecked(false);
        } else if (babyGender == BabyInfo.Gender.Girl){
            boyRadioButton.setChecked(false);
            girlRadioButton.setChecked(true);
        } else {
            boyRadioButton.setChecked(false);
            girlRadioButton.setChecked(false);
        }

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
    private class SaveUserInfoTask extends AsyncTask<Void, Void, Boolean> {

        private BabyInfo babyInfo;
        private String errorMessage;

        public SaveUserInfoTask(BabyInfo babyInfo) {
            this.babyInfo = babyInfo;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String userid = sessionManager.getUserId();
            if (userid != null) {
                try {
                    sleepServiceClient.saveBabyInfo(this.babyInfo, userid);
                    return true;
                } catch (InternalServerException e) {
                    errorMessage = internalServerError;
                } catch (ConnectionFailureException e) {
                    errorMessage = failedToConnectError;
                }
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(BabyInfoDialogFragment.this.getActivity(), progressMessageSave);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveUserInfoTask = null;
            progressDialog.dismiss();

            if (success) {
                sharedPreferenceUtil.storeBabyInfo(babyInfo);
                if (mListener != null) {
                    mListener.onBabyInfoUpdated(babyInfo);
                }
                BabyInfoDialogFragment.this.dismiss();
            } else {
                mBabyNameView.requestFocus();
                Snackbar.make(mBabyInfoSaveButton, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        }

        @Override
        protected void onCancelled() {
            mSaveUserInfoTask = null;
            progressDialog.dismiss();
        }
    }

    public interface BabyInfoDialogFragmentListener {
        /**
         * Invoked when baby info is updated. The Activity should implement this method and
         * update the UI.
         * @param babyInfo
         */
        void onBabyInfoUpdated(BabyInfo babyInfo);
    }
}
