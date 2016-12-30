package im.hch.sleeprecord.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.loader.EmailLoaderHelper;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.IdentityServiceClient;
import im.hch.sleeprecord.serviceclients.exceptions.EmailUsedException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.FieldValidator;
import im.hch.sleeprecord.utils.PermissionUtils;
import im.hch.sleeprecord.utils.SessionManager;

/**
 * A Register screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final String TAG = "RegisterActivity";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mRegisterTask = null;

    // UI references.
    @BindView(R.id.email) AutoCompleteTextView mEmailView;
    @BindView(R.id.password) EditText mPasswordView;
    @BindView(R.id.re_password) EditText mRepeatPasswordView;
    @BindView(R.id.register_form) RelativeLayout mRegisterForm;
    @BindView(R.id.username) EditText mUsernameView;
    @BindView(R.id.email_register_button) Button mEmailRegisterButton;

    @BindString(R.string.error_incorrect_password) String incorrectPasswordError;
    @BindString(R.string.error_invalid_password) String invalidPassordError;
    @BindString(R.string.error_unmatch_password) String unmatchPasswordError;
    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_invalid_email) String invalidEmailError;
    @BindString(R.string.progress_message_register) String progressMessageRegister;
    @BindString(R.string.error_failed_to_register) String failedToRegisterError;
    @BindString(R.string.error_email_used) String emailUsedError;

    private SessionManager mSessionManager;
    private IdentityServiceClient identityServiceClient;
    private EmailLoaderHelper emailLoaderHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        this.mSessionManager = new SessionManager(this);
        this.identityServiceClient = new IdentityServiceClient();
        this.emailLoaderHelper = new EmailLoaderHelper(this);

        populateAutoComplete();

        mRepeatPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }

    private void populateAutoComplete() {
        if (!PermissionUtils.mayRequestContacts(this, mEmailView)) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return emailLoaderHelper.createLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = emailLoaderHelper.getEmails(cursor);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emails);
        mEmailView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private void attemptRegister() {
        if (mRegisterTask != null) {
            return;
        }

        ActivityUtils.hideKeyboard(this);
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString();
        String rePassword = mRepeatPasswordView.getText().toString();
        String nickName = mUsernameView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!FieldValidator.isPasswordValid(password)) {
            mPasswordView.setError(invalidPassordError);
            focusView = mPasswordView;
            cancel = true;
        } else if (!password.equals(rePassword)) {
            mPasswordView.setError(unmatchPasswordError);
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(requiredFieldError);
            focusView = mEmailView;
            cancel = true;
        } else if (!FieldValidator.isEmailValid(email)) {
            mEmailView.setError(invalidEmailError);
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(nickName)) {
            mUsernameView.setError(requiredFieldError);
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mRegisterTask = new UserRegisterTask(email, password, nickName);
            mRegisterTask.execute((Void) null);
        }
    }

    /**
     * This task registers an account.
     */
    private class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mNickName;
        private UserProfile userProfile;
        private String errorMessage;

        UserRegisterTask(String email, String password, String nickName) {
            mEmail = email;
            mPassword = password;
            mNickName = nickName;
            errorMessage = failedToRegisterError;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(RegisterActivity.this, progressMessageRegister);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                userProfile = identityServiceClient.register(mEmail, mPassword, mNickName);
                return userProfile != null;
            } catch (EmailUsedException e) {
                errorMessage = emailUsedError;
            } catch (InternalServerException e) {
                //Use the default error message
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegisterTask = null;
            progressDialog.dismiss();

            if (success) {
                userProfile.setEmail(mEmail);
                mSessionManager.createSession(userProfile);
                ActivityUtils.navigateToMainActivity(RegisterActivity.this);
            } else {
                Snackbar.make(mEmailRegisterButton, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            mRegisterTask = null;
            progressDialog.dismiss();
        }
    }
}

