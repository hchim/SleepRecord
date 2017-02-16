package im.hch.sleeprecord.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.IOException;

import im.hch.sleeprecord.activities.RegisterActivity;
import im.hch.sleeprecord.activities.login.LoginActivity;
import im.hch.sleeprecord.activities.main.MainActivity;
import im.hch.sleeprecord.activities.training.ChecklistActivity;
import im.hch.sleeprecord.activities.training.PlanningActivity;

public class ActivityUtils {

    public static void navigateToMainActivity(Activity currentActivity) {
        navigateTo(currentActivity, MainActivity.class, true, null);
    }

    public static void navigateToRegisterActivity(Activity currentActivity) {
        navigateTo(currentActivity, RegisterActivity.class, true, null);
    }

    public static void navigateToLoginActivity(Activity currentActivity) {
        navigateTo(currentActivity, LoginActivity.class, true, null);
    }

    public static void navigateToChecklistActivity(Activity currentActivity) {
        navigateTo(currentActivity, ChecklistActivity.class, false, null);
    }

    public static void navigateToPlanningActivity(Activity currentActivity) {
        navigateTo(currentActivity, PlanningActivity.class, false, null);
    }

    /**
     * Navigate the the new activity from the current activity.
     * @param current
     * @param newActivityClass
     * @param clearTop
     */
    private static void navigateTo(Activity current, Class newActivityClass, boolean clearTop, Bundle bundle) {
        Intent intent = new Intent(current, newActivityClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }

        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        current.startActivity(intent);

        if (clearTop) {
            current.finish();
        }
    }

    /**
     * Hide keyboard.
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        View v = activity.getWindow().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static final int PICKUP_IMAGE_REQUEST_CODE = 214;
    public static final int TAKE_PHOTO_REQUEST_CODE = 215;

    /**
     * Show the select image view.
     * @param activity
     */
    public static void selectImageFromGallery(Activity activity) {
        Intent intentGallery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intentGallery.setType("image/*");
        activity.startActivityForResult(intentGallery, PICKUP_IMAGE_REQUEST_CODE);
    }

    /**
     * Take a photo and select the image.
     *
     * If fullSizeImage is true, activity must implement ImageCaptureListener.
     * If fullSizeImage is false, get the bitmap as follows:
     *
     * Bundle extras = data.getExtras();
     * Bitmap imageBitmap = (Bitmap) extras.get("data")
     *
     * @param activity
     * @param fullSizeImage
     */
    public static void selectImageFromCamera(Activity activity, boolean fullSizeImage) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentCamera.resolveActivity(activity.getPackageManager()) != null) {
            if (fullSizeImage) {
                try {
                    File photoFile = ImageUtils.createImageFile(activity);
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(activity,
                                activity.getPackageName() + ".fileprovider",
                                photoFile);
                        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        if (activity instanceof ImageCaptureListener) {
                            ((ImageCaptureListener) activity).onImageFileCreated(photoURI);
                        }
                    }
                } catch (IOException e) {
                    Log.e(MainActivity.TAG, "Failed to create image file.", e);
                }
            }
            activity.startActivityForResult(intentCamera, TAKE_PHOTO_REQUEST_CODE);
        }
    }
}
