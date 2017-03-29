package im.hch.sleeprecord.utils;

import android.app.Activity;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import lombok.Setter;

/**
 * Created by huiche on 3/29/17.
 */

public class LinkClickHandler extends LinkMovementMethod {
    private static LinkClickHandler mInstance;
    @Setter
    private Activity activity;

    public static LinkClickHandler getInstance() {
        if (mInstance == null)
            mInstance = new LinkClickHandler();
        return mInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            String link = getLink(widget, buffer, event);
            if (link != null) {
                if (activity != null) {
                    ActivityUtils.navigateToWebActivity(activity, link);
                } else {
                    return super.onTouchEvent(widget, buffer, event);
                }
            }
        }
        return true;
    }

    /**
     * Get the clicked link.
     * @param widget
     * @param buffer
     * @param event
     * @return
     */
    private String getLink(TextView widget, Spannable buffer, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();
        x += widget.getScrollX();
        y += widget.getScrollY();

        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);
        URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

        return link.length > 0 ? link[0].getURL() : null;
    }
}
