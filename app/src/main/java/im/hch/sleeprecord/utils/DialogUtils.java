package im.hch.sleeprecord.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.view.View;

import java.util.Calendar;

import im.hch.sleeprecord.activities.login.ResetPasswordDialogFragment;
import im.hch.sleeprecord.activities.main.AddRecordDialogFragment;
import im.hch.sleeprecord.activities.main.BabyInfoDialogFragment;
import im.hch.sleeprecord.activities.home.VerifyEmailDialogFragment;
import im.hch.sleeprecord.activities.main.SuggestionDialogFragment;
import im.hch.sleeprecord.activities.settings.UpdatePasswordDialogFragment;
import im.hch.sleeprecord.models.BabyInfo;

public class DialogUtils {
    public static final String DIALOG_TAG = "dialog";

    /**
     * Show dateTime picker dialog.
     * @param v
     * @param listener
     */
    public static DatePickerDialog showDatePickerDialog(View v, DatePickerDialog.OnDateSetListener listener) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), listener, year, month, day);
        datePickerDialog.show();
        return datePickerDialog;
    }

    /**
     * Show the baby information edit dialog.
     * @param fragmentManager
     * @param babyInfo
     */
    public static BabyInfoDialogFragment showEditBabyInfoDialog(FragmentManager fragmentManager, BabyInfo babyInfo) {
        BabyInfoDialogFragment babyInfoFragment = BabyInfoDialogFragment.newInstance(babyInfo);
        babyInfoFragment.show(fragmentManager, DIALOG_TAG);
        return babyInfoFragment;
    }

    /**
     * Show the add record dialog.
     * @param fragmentManager
     * @return
     */
    public static AddRecordDialogFragment showAddRecordDialog(FragmentManager fragmentManager,
                                                              AddRecordDialogFragment.AddRecordDialogListener callback) {
        AddRecordDialogFragment addRecordDialogFragment = AddRecordDialogFragment.newInstance();
        addRecordDialogFragment.setMListener(callback);
        addRecordDialogFragment.show(fragmentManager, DIALOG_TAG);
        return addRecordDialogFragment;
    }

    /**
     * Show the progress dialog. Used for network content or local storage content loading.
     * @param activity
     * @param message
     * @return
     */
    public static ProgressDialog showProgressDialog(Activity activity, String message) {
        ProgressDialog progressBar = new ProgressDialog(activity);
        progressBar.setMessage(message);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setCancelable(false);
        progressBar.show();
        return progressBar;
    }

    /**
     * Show the update password fragment.
     * @param fragmentManager
     * @return
     */
    public static UpdatePasswordDialogFragment showUpdatePasswordDialog(FragmentManager fragmentManager) {
        UpdatePasswordDialogFragment fragment = UpdatePasswordDialogFragment.newInstance();
        fragment.show(fragmentManager, DIALOG_TAG);
        return fragment;
    }

    /**
     * Show the verify email dialog.
     * @param fragmentManager
     * @return
     */
    public static VerifyEmailDialogFragment showVerifyEmailDialog(FragmentManager fragmentManager) {
        VerifyEmailDialogFragment fragment = VerifyEmailDialogFragment.newInstance();
        fragment.show(fragmentManager, DIALOG_TAG);
        return fragment;
    }

    /**
     * Show the reset password dialog.
     * @param fragmentManager
     * @return
     */
    public static ResetPasswordDialogFragment showResetPasswordDialog(FragmentManager fragmentManager) {
        ResetPasswordDialogFragment fragment = ResetPasswordDialogFragment.newInstance();
        fragment.show(fragmentManager, DIALOG_TAG);
        return fragment;
    }

    /**
     * Show add suggestion dialog.
     * @param fragmentManager
     * @return
     */
    public static SuggestionDialogFragment showAddSuggestionDialog(FragmentManager fragmentManager) {
        SuggestionDialogFragment fragment = SuggestionDialogFragment.newInstance();
        fragment.show(fragmentManager, DIALOG_TAG);
        return fragment;
    }
}
