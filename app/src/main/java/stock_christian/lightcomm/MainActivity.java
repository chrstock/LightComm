package stock_christian.lightcomm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private final static String APP_PATH_SD_CARD = "/Streams/";
    private final static String APP_THUMBNAIL_PATH_SD_CARD = "MoreStreams";
    private final static String APP_FILENAME = "PIC";
    public final static String EXTRA_MESSAGE = "stock.christian";
    private static final String TAG = "MainActivity";

    private static final String fullPath = "/storage/emulated/0/Streams/MoreStreams/PIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    @Override
    protected void onStart (){
        Log.i(TAG, "Called onStart");
        super.onStart();

    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Called onResume");
        super.onResume();

        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mOpenCVCallBack)) {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
    }

    public void onClickRecord (View view) {
        final Intent callVideoAppIntent = new Intent();
        callVideoAppIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        callVideoAppIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 6);
        startActivityForResult(callVideoAppIntent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Button btn_translate = (Button) findViewById(R.id.btn_translate);
        Uri videoUri;
        int FileCounter=0;

        if(requestCode==0) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.tst_recordSuccess) + data.getData(), Toast.LENGTH_LONG);

                videoUri = data.getData();

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                try {
                    Bitmap btm_records;

                    mmr.setDataSource(getBaseContext(), videoUri);

                    for (int i = 0; i < 6000000; i += 1000000) {
                        btm_records = mmr.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST);
                        saveImageToExternalStorage(btm_records,FileCounter++);
                    }

                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (RuntimeException ex){
                    ex.printStackTrace();
                }finally {
                    try {
                        mmr.release();
                    } catch (RuntimeException ex) {
                    }
                }

                btn_translate.setVisibility(View.VISIBLE);

            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, getString(R.string.tst_recordFailed),Toast.LENGTH_LONG);
            }else {
                Toast.makeText(this, getString(R.string.tst_recordAborted),Toast.LENGTH_LONG);
            }
        }
    }

    public boolean saveImageToExternalStorage(Bitmap image, int count) {
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SD_CARD + APP_THUMBNAIL_PATH_SD_CARD;

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            File file = new File(fullPath, APP_FILENAME+count+++".png");
            file.createNewFile();
            fOut = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.PNG, 50, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            return true;

        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
            return false;
        }
    }

    public void onClickTranslate (View view){
        final Intent translateVideoRecordIntent = new Intent(this, LastActivity.class);
        startActivity(translateVideoRecordIntent);
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
}
