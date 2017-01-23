package im.hch.sleeprecord.activities.login;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.serviceclients.IdentityServiceClient;
import im.hch.sleeprecord.serviceclients.exceptions.AccountNotExistException;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.serviceclients.exceptions.WrongSecurityCodeException;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.FieldValidator;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;
import im.hch.sleeprecord.utils.StringUtils;

public class ResetPasswordDialogFragment extends DialogFragment {

    private SessionManager sessionManager;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private ResetPasswordTask mResetPasswordTask = null;
    private SendEmailTask mSendEmailTask = null;
    private IdentityServiceClient identityServiceClient;

    @BindView(R.id.emailEditText) EditText emailEditText;
    @BindView(R.id.securityCode) EditText securityCodeEditText;
    @BindView(R.id.newPassword) EditText newPasswordEditText;
    @BindView(R.id.repeatPassword) EditText repeatPasswordEditText;
    @BindView(R.id.send_email_btn) Button sendEmailButton;
    @BindView(R.id.password_reset_btn) Button resetPasswordButton;
    @BindView(R.id.securityCodeView) View securityCodeView;
    @BindView(R.id.newPswdView) View newPswdView;
    @BindView(R.id.repeatPSWDView) View repeatPswdView;

    @BindString(R.string.reset_password_title) String title;
    @BindString(R.string.progress_message_send) String progressMessageSend;
    @BindString(R.string.progress_message_reset) String progressMessageReset;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.error_invalid_security_code) String invalidSecurityCode;
    @BindString(R.string.error_invalid_email) String invalidEmailError;
    @BindString(R.string.error_unmatch_password) String unmatchPasswordError;
    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_account_does_not_exist) String accountDoesNotExist;
    @BindString(R.string.error_invalid_password) String invalidPassordError;
    @BindString(R.string.error_wrong_security_code) String wrongSecurityCodeError;

    public static ResetPasswordDialogFragment newInstance() {
        ResetPasswordDialogFragment fragment = new ResetPasswordDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        ButterKnife.bind(this, view);

        Context context = this.getActivity();
        identityServiceClient = new IdentityServiceClient();
        sessionManager = new SessionManager(context);
        sharedPreferenceUtil = new SharedPreferenceUtil(context);

        getDialog().setTitle(title);

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempSendEmail();
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempResetPassword();
            }
        });

        repeatPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attempResetPassword();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void attempSendEmail() {
        if (mSendEmailTask != null) {
            return;
        }

        ActivityUtils.hideKeyboard(this.getActivity());
        emailEditText.setError(null);

        String email = emailEditText.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (StringUtils.isEmpty(email)) {
            emailEditText.setError(requiredFieldError);
            focusView = emailEditText;
            cancel = true;
        } else if (!FieldValidator.isEmailValid(email)) {
            emailEditText.setError(invalidEmailError);
            focusView = emailEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mSendEmailTask = new SendEmailTask(email);
            mSendEmailTask.execute();
        }
    }

    private void attempResetPassword() {
        if (mResetPasswordTask != null) {
            return;
        }

        ActivityUtils.hideKeyboard(this.getActivity());
        emailEditText.setError(null);
        securityCodeEditText.setError(null);
        newPasswordEditText.setError(null);
        repeatPasswordEditText.setError(null);

        String email = emailEditText.getText().toString();
        String securityCode = securityCodeEditText.getText().toString();
        String newPsw = newPasswordEditText.getText().toString();
        String repeatPsw = repeatPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (StringUtils.isEmpty(email)) {
            emailEditText.setError(requiredFieldError);
            focusView = emailEditText;
            cancel = true;
        } else if (!FieldValidator.isEmailValid(email)) {
            emailEditText.setError(invalidEmailError);
            focusView = emailEditText;
            cancel = true;
        } else if (StringUtils.isEmpty(securityCode)) {
            securityCodeEditText.setError(requiredFieldError);
            focusView = securityCodeEditText;
            cancel = true;
        } else if (!FieldValidator.isPasswordValid(newPsw)) {
            newPasswordEditText.setError(invalidPassordError);
            focusView = newPasswordEditText;
            cancel = true;
        } else if (!newPsw.equals(repeatPsw)) {
            repeatPasswordEditText.setError(unmatchPasswordError);
            focusView = repeatPasswordEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mResetPasswordTask = new ResetPasswordTask(email, securityCode, newPsw);
            mResetPasswordTask.execute();
        }
    }

    private void showPasswordResetView() {
        newPswdView.setVisibility(View.VISIBLE);
        repeatPswdView.setVisibility(View.VISIBLE);
        securityCodeView.setVisibility(View.VISIBLE);
        resetPasswordButton.setVisibility(View.VISIBLE);
        sendEmailButton.setVisibility(View.GONE);
    }

    /**
     * This task saves user info to the remote service.
     */
    private class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progressDialog;
        private String errorMessage;
        private String email;
        private String securityCode;
        private String newPassword;

        public ResetPasswordTask(String email, String securityCode, String newPassword) {
            this.email = email;
            this.securityCode = securityCode;
            this.newPassword = newPassword;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                identityServiceClient.resetPassword(email, securityCode, newPassword);
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
            } catch (AccountNotExistException e) {
                errorMessage = accountDoesNotExist;
            } catch (WrongSecurityCodeException e) {
                errorMessage = wrongSecurityCodeError;
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(
                    ResetPasswordDialogFragment.this.getActivity(), progressMessageReset);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mResetPasswordTask = null;
            progressDialog.dismiss();

            if (success) {
                ResetPasswordDialogFragment.this.dismiss();
            } else {
                securityCodeEditText.requestFocus();
                Snackbar.make(securityCodeEditText, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        }

        @Override
        protected void onCancelled() {
            mResetPasswordTask = null;
            progressDialog.dismiss();
        }
    }

    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progressDialog;
        private String errorMessage;
        private String email;

        public SendEmailTask(String email) {
            this.email = email;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                identityServiceClient.sendPasswordResetEmail(email);
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
            } catch (AccountNotExistException e) {
                errorMessage = accountDoesNotExist;
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(
                    ResetPasswordDialogFragment.this.getActivity(), progressMessageSend);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSendEmailTask = null;
            progressDialog.dismiss();

            if (success) {
                showPasswordResetView();
            } else {
                emailEditText.requestFocus();
                Snackbar.make(emailEditText, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        }

        @Override
        protected void onCancelled() {
            mSendEmailTask = null;
            progressDialog.dismiss();
        }
    }
}
