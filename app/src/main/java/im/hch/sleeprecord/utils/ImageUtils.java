package im.hch.sleeprecord.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ImageUtils {

    public static final String FILE_FORMAT = "JPEG_yyyyMMdd_HHmmss_";
    public static final String FILE_SUFIX = ".jpg";

    public static File createImageFile(final Context context) throws IOException {
        String fileName = DateUtils.dateToStr(new Date(), FILE_FORMAT);
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, FILE_SUFIX, storageDir);
        return image;
    }
}
