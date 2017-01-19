package im.hch.sleeprecord.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ImageUtils {
    public static final String TAG = "ImageUtils";

    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String FILE_SUFIX = ".jpg";

    public static File createImageFile(final Context context) throws IOException {
        String fileName = String.format("JPEG_%s", DateUtils.dateToStr(new Date(), DATE_FORMAT));
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, FILE_SUFIX, storageDir);
        return image;
    }

    public static String saveImageFile(final Context context, final Bitmap bitmap) {
        try {
            File imageFile = createImageFile(context);
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save image file.", e);
            return null;
        }
    }
}
