package im.hch.sleeprecord.utils;

import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.view.View;

import java.util.Calendar;

import im.hch.sleeprecord.activities.main.BabyInfoDialogFragment;
import im.hch.sleeprecord.models.BabyInfo;

public class DialogUtils {

    /**
     * Show date picker dialog.
     * @param v
     * @param listener
     */
    public static void showDatePickerDialog(View v, DatePickerDialog.OnDateSetListener listener) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), listener, year, month, day);
        datePickerDialog.show();
    }

    public static void showEditBabyInfoDialog(FragmentManager fragmentManager, BabyInfo babyInfo) {
        BabyInfoDialogFragment babyInfoFragment = BabyInfoDialogFragment.newInstance(babyInfo);
        babyInfoFragment.show(fragmentManager, "dialog");
    }
}
