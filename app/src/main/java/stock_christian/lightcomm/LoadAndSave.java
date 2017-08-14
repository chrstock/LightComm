package stock_christian.lightcomm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

public class LoadAndSave {

    public Bitmap getThumbnail(String filenamePath,Context mContext) {

        Bitmap thumbnail = null;

        try {
            if (isSdReadable()) {
                thumbnail = BitmapFactory.decodeFile(filenamePath);
            }
        } catch (Exception e) {
        }

        if (thumbnail == null) {
            try {
                File filePath = mContext.getFileStreamPath(filenamePath);
                FileInputStream fi = new FileInputStream(filePath);
                thumbnail = BitmapFactory.decodeStream(fi);
            } catch (Exception ex) {

            }
        }
        return thumbnail;
    }

    public boolean isSdReadable() {

        boolean mExternalStorageAvailable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            mExternalStorageAvailable = true;
            Log.i("isSdReadable", "External storage card is readable.");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.i("isSdReadable", "External storage card is readable.");
            mExternalStorageAvailable = true;
        } else {

            mExternalStorageAvailable = false;
        }

        return mExternalStorageAvailable;
    }
}
