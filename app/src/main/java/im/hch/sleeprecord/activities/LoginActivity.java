package im.hch.sleeprecord.activities;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.loader.EmailLoaderHelper;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.UserProfileServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.FieldValidator;
import im.hch.sleeprecord.utils.PermissionUtils;
import im.hch.sleeprecord.utils.ProgressBarHelper;
import im.hch.sleeprecord.utils.SessionManager;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final String TAG = "LoginActivity";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    @BindView(R.id.email) AutoCompleteTextView mEmailView;
    @BindView(R.id.password) EditText mPasswordView;
    @BindView(R.id.login_progress) ProgressBar mProgressView;
    @BindView(R.id.login_form) RelativeLayout mLoginForm;
    @BindView(R.id.email_sign_in_button) Button mEmailSigninButton;
    @BindView(R.id.email_register_button) Button mEmailRegisterButton;

    @BindString(R.string.error_incorrect_password) String incorrectPasswordError;
    @BindString(R.string.error_invalid_password) String invalidPassordError;
    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_invalid_email) String invalidPasswordError;

    private SessionManager mSessionManager;
    private UserProfileServiceClient userProfileServiceClient;
    private EmailLoaderHelper emailLoaderHelper;
    private ProgressBarHelper progressBarHelper;

    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mSessionManager = new SessionManager(this);
        userProfileServiceClient = new UserProfileServiceClient();
        emailLoaderHelper = new EmailLoaderHelper(this);
        progressBarHelper = new ProgressBarHelper(mProgressView, mLoginForm, null);

        if (mSessionManager.isLoggedIn()) {
            ActivityUtils.navigateToMainActivity(this);
            return;
        }

        // Set up the login form.
        populateAutoComplete();

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.navigateToRegisterActivity(LoginActivity.this);
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
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        // set the email lists
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emails);
        mEmailView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(requiredFieldError);
            focusView = mEmailView;
            cancel = true;
        } else if (!FieldValidator.isEmailValid(email)) {
            mEmailView.setError(invalidPasswordError);
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            progressBarHelper.show();
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            userProfile = userProfileServiceClient.login(mEmail, mPassword);
            return userProfile != null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progressBarHelper.hide();

            if (success) {
                mSessionManager.createSession(userProfile);
                ActivityUtils.navigateToMainActivity(LoginActivity.this);
            } else {
                mPasswordView.setError(incorrectPasswordError);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressBarHelper.hide();
        }
    }
}

