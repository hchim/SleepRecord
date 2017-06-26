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
import android.util.Log;
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
import im.hch.sleeprecord.MyAppConfig;
import im.hch.sleeprecord.R;
import im.hch.sleeprecord.serviceclients.AppInfoServiceClient;
import im.hch.sleeprecord.utils.ActivityUtils;
import im.hch.sleeprecord.utils.DialogUtils;
import im.hch.sleeprecord.utils.SessionManager;

public class SuggestionDialogFragment extends DialogFragment {

    private ProgressDialog progressDialog;
    private AddSuggestionAsyncTask addSuggestionAsyncTask = null;
    private AppInfoServiceClient appInfoServiceClient;
    private SessionManager sessionManager;

    @BindView(R.id.suggestionTextView) EditText suggestionTextView;

    @BindString(R.string.progress_message_save) String progressMessageSave;
    @BindString(R.string.error_failed_to_connect) String failedToConnectError;
    @BindString(R.string.error_internal_server) String internalServerError;
    @BindString(R.string.error_field_required) String requiredFieldError;
    @BindString(R.string.suggestion_thanks_message) String thanksMessage;

    public static SuggestionDialogFragment newInstance() {
        SuggestionDialogFragment fragment = new SuggestionDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_suggestion, null, false);
        ButterKnife.bind(this, view);
        init(getActivity());

        builder.setView(view)
                .setTitle(R.string.pref_title_suggestion)
                // Add action buttons
                .setPositiveButton(R.string.button_Save, null)
                .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SuggestionDialogFragment.this.getDialog().cancel();
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
                        attemptSubmitSuggestion();
                    }
                });
            }
        });
        return dialog;
    }

    private void init(Activity activity) {
        appInfoServiceClient = new AppInfoServiceClient(MyAppConfig.getAppConfig());
        sessionManager = new SessionManager(activity);

        suggestionTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptSubmitSuggestion();
                    return true;
                }
                return false;
            }
        });
    }

    private void attemptSubmitSuggestion() {
        if (addSuggestionAsyncTask != null) {
            return;
        }

        ActivityUtils.hideKeyboard(this.getActivity());
        suggestionTextView.setError(null);

        String suggestion = (String) suggestionTextView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (suggestion == null) {
            suggestionTextView.setError(requiredFieldError);
            focusView = suggestionTextView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            addSuggestionAsyncTask = new AddSuggestionAsyncTask(suggestion);
            addSuggestionAsyncTask.execute();
        }
    }

    private class AddSuggestionAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private String suggestion;

        public AddSuggestionAsyncTask(String suggestion) {
            this.suggestion = suggestion;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtils.showProgressDialog(SuggestionDialogFragment.this.getActivity(), progressMessageSave);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                appInfoServiceClient.addSuggestion(sessionManager.getUserId(), suggestion);
                return Boolean.TRUE;
            } catch (Exception e) {
                Log.e(MainActivity.TAG, e.getMessage(), e);
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            addSuggestionAsyncTask = null;
            progressDialog.dismiss();

            if (aBoolean) {
                Snackbar.make(suggestionTextView, thanksMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                SuggestionDialogFragment.this.dismiss();
            }
        }

        @Override
        protected void onCancelled() {
            addSuggestionAsyncTask = null;
            progressDialog.dismiss();
        }
    }
}
