package im.hch.sleeprecord.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    public static String downloadImage(final Context context, final String downloadUrl) {
        if (downloadUrl == null) {
            return null;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return null;
            }

            File imageFile = createImageFile(context);
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(response.body().bytes());
            fos.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to download image.", e);
        }

        return null;
    }
}
