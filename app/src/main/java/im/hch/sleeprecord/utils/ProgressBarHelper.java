package im.hch.sleeprecord.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;

public class ProgressBarHelper {

    private View mView;
    private ProgressBar mProgressView;
    private int mShortAnimTime;
    private ProgressBarHelperCallback mCallback;

    public ProgressBarHelper(ProgressBar progressBar, View view, ProgressBarHelperCallback callback) {
        this.mView = view;
        this.mProgressView = progressBar;
        this.mCallback = callback;
        mShortAnimTime = progressBar
                .getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public void hide() {
        showProgress(false);
    }

    public void show() {
        showProgress(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (mCallback != null) {
            if (show) {
                mCallback.onShowProgress();
            } else {
                mCallback.onHideProgress();
            }
        }

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            if (mView != null) {
                mView.setVisibility(show ? View.GONE : View.VISIBLE);
                mView.animate().setDuration(mShortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            }

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(mShortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (mView != null) {
                mView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }
    }

    public interface ProgressBarHelperCallback {
        public void onShowProgress();
        public void onHideProgress();
    }
}
