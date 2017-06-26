package im.hch.sleeprecord.activities.settings;

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

import com.sleepaiden.androidcommonutils.exceptions.AccountNotExistException;
import com.sleepaiden.androidcommonutils.exceptions.AuthFailureException;
import com.sleepaiden.androidcommonutils.exceptions.ConnectionFailureException;
import com.sleepaiden.androidcommonutils.exceptions.InternalServerException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.MyAppConfig;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.serviceclients.IdentityServiceClient;
import im.hch.sleeprecord.serviceclients.exceptions.WrongPasswordException;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.FieldValidator;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.utils.SharedPreferenceUtil;

public class UpdatePasswordDialogFragment extends DialogFragment {

    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private SharedPreferenceUtil sharedPreferenceUtil;
    private SavePasswordTask mSavePasswordTask = null;
    private IdentityServiceClient identityServiceClient;

    @BindView(R.id.currentPassword) EditText currentPasswordEditText;
    @BindView(R.id.newPassword) EditText newPasswordEditText;
    @BindView(R.id.repeatPassword) EditText repeatPasswordEditText;

    @BindString(R.string.progress_message_save) String progressMessageSave;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.error_invalid_password) String invalidPassordError;
    @BindString(R.string.error_unmatch_password) String unmatchPasswordError;
    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_wrong_password) String wrongPasswordError;
    @BindString(R.string.error_account_does_not_exist) String accountDoesNotExist;
    @BindString(R.string.error_auth_failure) String authError;

    public static UpdatePasswordDialogFragment newInstance() {
        UpdatePasswordDialogFragment fragment = new UpdatePasswordDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_update_password, null, false);
        ButterKnife.bind(this, view);
        init(getActivity());

        builder.setView(view)
                .setTitle(R.string.pref_title_update_password)
                // Add action buttons
                .setPositiveButton(R.string.button_Save, null)
                .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        UpdatePasswordDialogFragment.this.getDialog().cancel();
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
                        attempSavePassword();
                    }
                });
            }
        });
        return dialog;
    }

    private void init(Activity activity) {
        sessionManager = new SessionManager(activity);
        sharedPreferenceUtil = new SharedPreferenceUtil(activity);

        identityServiceClient = new IdentityServiceClient(MyAppConfig.getAppConfig());
        identityServiceClient.setAccessToken(sessionManager.getAccessToken());

        repeatPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attempSavePassword();
                    return true;
                }
                return false;
            }
        });
    }

    private void attempSavePassword() {
        if (mSavePasswordTask != null) {
            return;
        }

        ActivityUtils.hideKeyboard(this.getActivity());
        currentPasswordEditText.setError(null);
        newPasswordEditText.setError(null);
        repeatPasswordEditText.setError(null);

        String current = currentPasswordEditText.getText().toString();
        String newPsw = newPasswordEditText.getText().toString();
        String repeatPsw = repeatPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (current == null || current.toString().equals("")) {
            currentPasswordEditText.setError(requiredFieldError);
            focusView = currentPasswordEditText;
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
            mSavePasswordTask = new SavePasswordTask(current, newPsw);
            mSavePasswordTask.execute();
        }
    }

    /**
     * This task saves user info to the remote service.
     */
    private class SavePasswordTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage;
        private String oldPassword;
        private String newPassword;

        public SavePasswordTask(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                identityServiceClient.updateUserPassword(oldPassword, newPassword);
                return true;
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
            } catch (AccountNotExistException e) {
                errorMessage = accountDoesNotExist;
            } catch (WrongPasswordException e) {
                errorMessage = wrongPasswordError;
            } catch (AuthFailureException e) {
                errorMessage = authError;
            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(
                    UpdatePasswordDialogFragment.this.getActivity(), progressMessageSave);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSavePasswordTask = null;
            progressDialog.dismiss();

            if (success) {
                UpdatePasswordDialogFragment.this.dismiss();
            } else {
                if (errorMessage == authError) {
                    ActivityUtils.navigateToLoginActivity(UpdatePasswordDialogFragment.this.getActivity());
                } else {
                    currentPasswordEditText.requestFocus();
                    Snackbar.make(currentPasswordEditText, errorMessage, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mSavePasswordTask = null;
            progressDialog.dismiss();
        }
    }
}
