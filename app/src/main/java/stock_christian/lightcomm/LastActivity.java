package stock_christian.lightcomm;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class LastActivity extends AppCompatActivity {

    private Scalar LOWER_RED1 = new Scalar(220);
    private Scalar LOWER_RED2 = new Scalar(255);

    private ImageView mImage;
    private TextView mText;

    private ArrayList<Mat> Mat_Pictures;
    private String PSK;
    private String fullPath = "/storage/emulated/0/Streams/MoreStreams/PIC";
    private ArrayList<Bitmap> Bmp_NewPictures = new ArrayList<>();

    Calculations C = new Calculations();
    LoadAndSave L = new LoadAndSave();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last);

        mImage = (ImageView) findViewById(R.id.imV_preview);
        mText = (TextView) findViewById(R.id.PSKcode);

        for(int i=0;i<6;i++)
        {
            Bmp_NewPictures.add(L.getThumbnail(fullPath+i+".png",getApplicationContext()));
        }

        //Calculate Points
        Mat_Pictures = bitmapToMat (Bmp_NewPictures.get(0));

        PSK = C.calculateSignal(Mat_Pictures,Bmp_NewPictures);

        //Connect Router
        mText.setText(PSK);
    }

    @Override
    protected void onStart(){
        super.onStart();

    }



    public ArrayList<Mat> bitmapToMat(Bitmap bmp){
        ArrayList<Mat> Mat_All = new ArrayList<>();
        String filename;

        Mat_All.add(new Mat((bmp.getHeight()),bmp.getWidth(), CvType.CV_8UC3));

        for(int i=0;i<6;i++){

            filename=fullPath+i+".png";

            if(i==0){
                Utils.bitmapToMat(bmp,Mat_All.get(0));
            }
            else {
                Mat_All.add(i,Imgcodecs.imread(filename));
            }

//            Mat_All.add(Imgcodecs.imread(filename));

            Imgproc.GaussianBlur(Mat_All.get(i), Mat_All.get(i), new Size(3,3),10);
            Imgproc.cvtColor(Mat_All.get(i), Mat_All.get(i), Imgproc.COLOR_RGB2GRAY);
            Core.inRange(Mat_All.get(i),LOWER_RED1,LOWER_RED2,Mat_All.get(i));

            Imgproc.erode(Mat_All.get(i),Mat_All.get(i),Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(10,10)));
            Imgproc.dilate(Mat_All.get(i),Mat_All.get(i),Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(30,30)));
        }
        return Mat_All;
    }

    public void onClickPic1(View view){
        mImage.setImageBitmap(Bmp_NewPictures.get(0));
    }
    public void onClickPic2(View view){
        mImage.setImageBitmap(Bmp_NewPictures.get(1));
    }
    public void onClickPic3(View view){
        mImage.setImageBitmap(Bmp_NewPictures.get(2));
    }
    public void onClickPic4(View view){
        mImage.setImageBitmap(Bmp_NewPictures.get(3));
    }
    public void onClickPic5(View view){
        mImage.setImageBitmap(Bmp_NewPictures.get(4));
    }
    public void onClickPic6(View view){
        mImage.setImageBitmap(Bmp_NewPictures.get(5));
    }
}

