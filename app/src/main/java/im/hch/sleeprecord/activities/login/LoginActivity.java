package im.hch.sleeprecord.activities.login;

import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.Metrics;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.loader.EmailLoaderHelper;
import im.hch.sleeprecord.models.UserProfile;
import im.hch.sleeprecord.serviceclients.IdentityServiceClient;
import im.hch.sleeprecord.serviceclients.exceptions.AccountNotExistException;
import im.hch.sleeprecord.serviceclients.exceptions.ConnectionFailureException;
import im.hch.sleeprecord.serviceclients.exceptions.InternalServerException;
import im.hch.sleeprecord.serviceclients.exceptions.InvalidIDTokenException;
import im.hch.sleeprecord.serviceclients.exceptions.WrongPasswordException;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.FieldValidator;
import im.hch.sleeprecord.utils.MetricHelper;
import im.hch.sleeprecord.utils.PermissionUtils;
import im.hch.sleeprecord.utils.SessionManager;
import im.hch.sleeprecord.wxapi.WXEntryActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>
    , GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "LoginActivity";

    private static final int GOOGLE_SIGN_IN_RESULT_CODE = 9001;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    @BindView(R.id.email) AutoCompleteTextView mEmailView;
    @BindView(R.id.password) EditText mPasswordView;
    @BindView(R.id.email_sign_in_button) Button mEmailSigninButton;
    @BindView(R.id.forgetPswdTextView) TextView forgetPswdTextView;
    @BindView(R.id.registerAccountTextView) TextView registerAccountTextView;
    @BindView(R.id.googleSigninBtn) Button googleSigninButton;
    @BindView(R.id.facebookSigninBtn) Button facebookSigninButton;
    @BindView(R.id.wechatSigninBtn) Button wechatSigninButton;

    @BindString(R.string.error_incorrect_password) String incorrectPasswordError;
    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.error_invalid_email) String invalidEmailError;
    @BindString(R.string.progress_message_sign_in) String signInProgressMessage;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.sign_in_google_failed) String signinGoogleFailed;
    @BindString(R.string.sign_in_facebook_failed) String signinFacebookFailed;
    @BindString(R.string.sign_in_wechat_failed) String signinWechatFailed;
    @BindString(R.string.wechat_app_id) String wechatAppId;

    private SessionManager mSessionManager;
    private IdentityServiceClient identityServiceClient;
    private EmailLoaderHelper emailLoaderHelper;
    private ProgressDialog progressDialog;
    private UserProfile userProfile;
    private MetricHelper metricHelper;

    private GoogleSignInOptions googleSignInOptions;
    private GoogleApiClient googleApiClient;
    private VerifyGoogleSignResultTask verifyGoogleSignResultTask;

    private CallbackManager fabCallbackManager;
    private VerifyFacebookSignResultTask verifyFacebookSignResultTask;

    public static final String WECHAT_LOGIN_STATE = "wechat_sa_login";
    private static final String WECHAT_USERINFO_SCOPE = "snsapi_userinfo";
    private IWXAPI wechatAPI; // The interface for things that communicate with WeChat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mSessionManager = new SessionManager(this);
        identityServiceClient = new IdentityServiceClient();
        emailLoaderHelper = new EmailLoaderHelper(this);
        metricHelper = new MetricHelper(this);

        // Set up the login form.
        populateAutoComplete();

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
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

        registerAccountTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.navigateToRegisterActivity(LoginActivity.this);
            }
        });

        forgetPswdTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showResetPasswordDialog(LoginActivity.this.getFragmentManager());
            }
        });

        initGoogleSignIn();
        initFacebookSignIn();
        initWechatSignIn();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(wechatBroadcastReceiver);
        super.onDestroy();
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Login with Wechat
    //////////////////////////////////////////////////////////////////////////////////

    private void initWechatSignIn() {
        wechatAPI = WXAPIFactory.createWXAPI(this, wechatAppId, true);
        wechatAPI.registerApp(wechatAppId);

        IntentFilter intentFilter = new IntentFilter(WXEntryActivity.WECHAT_LOGIN_RES_ACTION);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(wechatBroadcastReceiver, intentFilter);

        wechatSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SendAuth.Req req = new SendAuth.Req();
                req.scope = WECHAT_USERINFO_SCOPE;
                req.state = WECHAT_LOGIN_STATE;
                wechatAPI.sendReq(req);
            }
        });
    }

    private BroadcastReceiver wechatBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int errorCode = intent.getIntExtra(WXEntryActivity.EXTRA_ERR_CODE, BaseResp.ErrCode.ERR_USER_CANCEL);
            if (errorCode == BaseResp.ErrCode.ERR_OK) {
                String code = intent.getStringExtra(WXEntryActivity.EXTRA_CODE);

            } else {
                Snackbar.make(mEmailSigninButton, signinWechatFailed, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    };
    //////////////////////////////////////////////////////////////////////////////////
    // Login with google
    //////////////////////////////////////////////////////////////////////////////////
    private void initGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        String googleClientId = getString(R.string.default_web_client_id);
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleClientId)
                .requestEmail().build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        googleSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_RESULT_CODE);
            }
        });
    }

    // When failed to signin with google
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Handle google login result.
     * @param result
     */
    private void handleGoogleLogin(GoogleSignInResult result) {
        if (result.isSuccess()) {
            if (verifyGoogleSignResultTask == null) {
                verifyGoogleSignResultTask = new VerifyGoogleSignResultTask(result.getSignInAccount());
                verifyGoogleSignResultTask.execute();
            }
        } else {
            Snackbar.make(mEmailSigninButton, signinGoogleFailed, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private class VerifyGoogleSignResultTask extends AsyncTask<Void, Void, Boolean> {
        private GoogleSignInAccount account;
        private String errorMessage;

        public VerifyGoogleSignResultTask(GoogleSignInAccount acct) {
            this.account = acct;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(LoginActivity.this, signInProgressMessage);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                UserProfile userProfile = identityServiceClient.verifyGoogleToken(
                        account.getEmail(), account.getDisplayName(), account.getIdToken());
                mSessionManager.createSession(userProfile);
                return Boolean.TRUE;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
                metricHelper.errorMetric(Metrics.GOOGLE_LOGIN_ERROR_METRIC, e);
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InvalidIDTokenException e) {
                errorMessage = signinGoogleFailed;
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            verifyGoogleSignResultTask = null;
            progressDialog.dismiss();

            if (success) {
                ActivityUtils.navigateToMainActivity(LoginActivity.this);
            } else {
                Snackbar.make(mEmailSigninButton, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            verifyGoogleSignResultTask = null;
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN_RESULT_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleLogin(result);
        }

        fabCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    //////////////////////////////////////////////////////////////////////////////////
    // Login with Facebook
    //////////////////////////////////////////////////////////////////////////////////
    /**
     * Init facebook signin.
     */
    private void initFacebookSignIn() {
        facebookSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(
                        LoginActivity.this,
                        Arrays.asList("user_photos", "email", "public_profile")
                );
            }
        });

        fabCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(fabCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        final AccessToken accessToken = loginResult.getAccessToken();
                        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    String name = object.getString("name");
                                    String email = object.getString("email");
                                    if (verifyFacebookSignResultTask == null) {
                                        verifyFacebookSignResultTask = new VerifyFacebookSignResultTask(accessToken, email, name);
                                        verifyFacebookSignResultTask.execute();
                                    }
                                } catch (JSONException e) {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.d(TAG, "Failed to login with facebook.", e);
                        Snackbar.make(mEmailSigninButton, signinFacebookFailed, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        metricHelper.errorMetric(Metrics.FACEBOOK_LOGIN_ERROR_METRIC, e);
                    }
                }
        );
    }

    private class VerifyFacebookSignResultTask extends AsyncTask<Void, Void, Boolean> {
        private String email;
        private String name;
        private String errorMessage;
        private AccessToken accessToken;

        public VerifyFacebookSignResultTask(AccessToken accessToken, String email, String name) {
            this.accessToken = accessToken;
            this.email = email;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(LoginActivity.this, signInProgressMessage);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                UserProfile userProfile = identityServiceClient.verifyFacebookToken(
                        email, name, accessToken.getToken());
                mSessionManager.createSession(userProfile);
                return Boolean.TRUE;
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
                metricHelper.errorMetric(Metrics.FACEBOOK_LOGIN_ERROR_METRIC, e);
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            } catch (InvalidIDTokenException e) {
                errorMessage = signinGoogleFailed;
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            verifyFacebookSignResultTask = null;
            progressDialog.dismiss();

            if (success) {
                ActivityUtils.navigateToMainActivity(LoginActivity.this);
            } else {
                Snackbar.make(mEmailSigninButton, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            verifyFacebookSignResultTask = null;
            progressDialog.dismiss();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Load email list
    //////////////////////////////////////////////////////////////////////////////////
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

    //////////////////////////////////////////////////////////////////////////////////
    // Login with Email
    //////////////////////////////////////////////////////////////////////////////////
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        ActivityUtils.hideKeyboard(this);
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
            mEmailView.setError(invalidEmailError);
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String errorMessage;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            errorMessage = incorrectPasswordError;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(LoginActivity.this, signInProgressMessage);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                userProfile = identityServiceClient.login(mEmail, mPassword);
                if (userProfile != null) {
                    mSessionManager.createSession(userProfile);
                    return true;
                } else {
                    return false;
                }
            } catch (AccountNotExistException e) {
                //use the default error message
            } catch (WrongPasswordException e) {
                //use the default error message
            } catch (InternalServerException e) {
                errorMessage = internalServerError;
                metricHelper.errorMetric(Metrics.LOGIN_ERROR_METRIC, e);
            } catch (ConnectionFailureException e) {
                errorMessage = failedToConnectError;
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progressDialog.dismiss();

            if (success) {
                ActivityUtils.navigateToMainActivity(LoginActivity.this);
            } else {
                mEmailView.requestFocus();
                Snackbar.make(mEmailSigninButton, errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressDialog.dismiss();
        }
    }
}