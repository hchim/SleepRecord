package im.hch.sleeprecord.activities.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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
import im.hch.sleeprecord.serviceclients.exceptions.WrongVerifyCodeException;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;

public class VerifyEmailDialogFragment extends DialogFragment {

    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private VerifyEmailTask mVerifyEmailTask = null;
    private IdentityServiceClient identityServiceClient;
    private OnEmailVerifiedListener listener;

    @BindView(R.id.verifyCode) EditText verifyCodeTextView;

    @BindString(R.string.progress_message_verify) String progressMessageVerify;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_account_does_not_exist) String accountDoesNotExist;
    @BindString(R.string.error_wrong_verify_code) String wrongVerifyCode;

    public static VerifyEmailDialogFragment newInstance() {
        VerifyEmailDialogFragment fragment = new VerifyEmailDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_verify_email, null, false);
        ButterKnife.bind(this, view);
        init(getActivity());

        builder.setView(view)
                .setTitle(R.string.email_verify_dialog_title)
                // Add action buttons
                .setPositiveButton(R.string.button_Save, null)
                .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        VerifyEmailDialogFragment.this.getDialog().cancel();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attempVerifyEmail();
                    }
                });
            }
        });
        return dialog;
    }

    private void init(Activity activity) {
        identityServiceClient = new IdentityServiceClient();
        sessionManager = new SessionManager(activity);
        if (activity instanceof OnEmailVerifiedListener) {
            listener = (OnEmailVerifiedListener) activity;
        }

        verifyCodeTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attempVerifyEmail();
                    return true;
                }
                return false;
            }
        });
    }

    private void attempVerifyEmail() {
        if (mVerifyEmailTask != null) {
            return;
        }

        ActivityUtils.hideKeyboard(this.getActivity());
        verifyCodeTextView.setError(null);

        String verifyCode = verifyCodeTextView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (verifyCode == null || verifyCode.toString().equals("")) {
            verifyCodeTextView.setError(requiredFieldError);
            focusView = verifyCodeTextView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mVerifyEmailTask = new VerifyEmailTask(verifyCode);
            mVerifyEmailTask.execute();
        }
    }

    /**
     * This task saves user info to the remote service.
     */
    private class VerifyEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String verifyCode;
        private String errorMessage;

        public VerifyEmailTask(String verifyCode) {
            this.verifyCode = verifyCode;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String userid = sessionManager.getUserId();
            try {
                identityServiceClient.verifyEmail(verifyCode, userid);
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
            } catch (AccountNotExistException e) {
                errorMessage = accountDoesNotExist;
            } catch (WrongVerifyCodeException e) {
                errorMessage = wrongVerifyCode;
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(
                    VerifyEmailDialogFragment.this.getActivity(), progressMessageVerify);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mVerifyEmailTask = null;
            progressDialog.dismiss();

            if (success) {
                VerifyEmailDialogFragment.this.dismiss();
                listener.onEmailVerified();
            } else {
                verifyCodeTextView.requestFocus();
                Snackbar.make(verifyCodeTextView, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        }

        @Override
        protected void onCancelled() {
            mVerifyEmailTask = null;
            progressDialog.dismiss();
        }
    }

    public interface OnEmailVerifiedListener {
        public void onEmailVerified();
    }
}
