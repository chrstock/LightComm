package stock_christian.lightcomm;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Calculations {

    private ArrayList<Point> Coordinates = new ArrayList<>();
    private ArrayList<Double> distanceList = new ArrayList<>();

    private static final String fullPath = "/storage/emulated/0/Streams/MoreStreams/PIC";

    public ArrayList<Point> calculateBoundingBoxCenter(ArrayList<Mat> Bitmaps)
    {
        List<MatOfPoint> MoP_contours = new ArrayList<>();
        Mat Mat_hierarchy = new Mat();
        ArrayList<Integer> x = new ArrayList<>();
        ArrayList<Integer> y = new ArrayList<>();

        Imgproc.findContours(Bitmaps.get(1), MoP_contours,Mat_hierarchy,Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat_hierarchy.release();

        List<Moments> mu = new ArrayList<>(MoP_contours.size());

        for(int i = 0; i<MoP_contours.size();i++){
            mu.add(i, Imgproc.moments(MoP_contours.get(i)));
            Moments p = mu.get(i);
            x.add((int) (p.get_m10() / p.get_m00()));
            y.add((int) (p.get_m01() / p.get_m00()));
            Coordinates.add(i,new Point(x.get(i),y.get(i)));
        }
        return Coordinates;
    }
    public ArrayList<Double> calculateAllDistances(ArrayList<Point> P){

        double x1,x0,y1,y0;

        for(int i = 0; i< Coordinates.size(); i++) {
            x1= Coordinates.get(i).x;
            y1= Coordinates.get(i).y;
            for (int j = 0; j < Coordinates.size(); j++) {
                x0= Coordinates.get(j).x;
                y0= Coordinates.get(j).y;
                distanceList.add(Math.sqrt(((x1-x0)*(x1-x0))+((y1-y0)*(y1-y0))));
            }
        }
        return distanceList;
    }

    public ArrayList<Double> calculateFourBiggestDistances(ArrayList<Double> distances){

        double longestDistance = 0.0;

        ArrayList<Double> longestDistanceList = new ArrayList<>();
        int index=0;
        for(int i=0;i<4;i++) {
            for (int j = 0; j < distanceList.size(); j++) {
                if (distanceList.get(j) > longestDistance) {
                    longestDistance = distanceList.get(j);
                    index = j;
                }
            }
            longestDistanceList.add(longestDistance);
            distanceList.set(index, 0.0);
            longestDistance = 0.0;
        }
        return longestDistanceList;
    }

    public ArrayList<Integer> determiningTwoEdgePairs(ArrayList<Point> P, ArrayList<Double> dist){
        ArrayList<Integer> index = new ArrayList<>();
        double dou_root,a0,a1,b0,b1;
        int token1=0;
        int token2=0;

        for(int i=0;i<P.size();i++) {
            a0=P.get(i).x;
            b0=P.get(i).y;
            for(int j=0;j<P.size();j++) {
                a1=P.get(j).x;
                b1=P.get(j).y;
                dou_root=Math.sqrt(((a0-a1)*(a0-a1))+((b0-b1)*(b0-b1)));

                if(dou_root==dist.get(0)&&token1==0) {
                    index.add(i);
                    index.add(j);
                    token1++;
                }
                if(dou_root==dist.get(2)&&token2==0) {
                    index.add(i);
                    index.add(j);
                    token2++;
                }
            }
        }
        return index;
    }

    public ArrayList<Integer> determingAllEdgePositions(ArrayList<Point> P, ArrayList<Integer> iV){

        ArrayList<Integer> detIndex = new ArrayList<>(Arrays.asList(0,0,0,0));

        double xHigh=0.0;
        double xLow=2000.0;
        double Sum,x,y;
        int del1=0;
        int del2=0;

        for(int i=0;i<iV.size();i++) {
            x = P.get(iV.get(i)).x;
            y = P.get(iV.get(i)).y;
            Sum=Math.sqrt((x*x)+(y*y));

            if(Sum>xHigh) {
                xHigh=Sum;
                detIndex.set(3,iV.get(i));
                del1=i;
            }
            if(Sum<xLow) {
                xLow=Sum;
                detIndex.set(0,iV.get(i));
                del2=i;
            }
        }
        //0,33,1,34
        //
        if(del1>del2) {
            iV.remove(del1);
            iV.remove(del2);
        }else{
            iV.remove(del2);
            iV.remove(del1);
        }


        double x1 = P.get(iV.get(0)).x;
        double x2 = P.get(iV.get(1)).x;

        if(x1>x2){
            detIndex.set(1, iV.get(0));
            detIndex.set(2, iV.get(1));
        }else{
            detIndex.set(2, iV.get(0));
            detIndex.set(1, iV.get(1));
        }
        return detIndex;
    }

    public ArrayList<Double> calculateColumnX (ArrayList<Point> P, ArrayList<Integer> dI, int columnCount) {
        ArrayList<Double> column = new ArrayList<>();

        double Difference;
        double Ax = P.get(dI.get(0)).x;
        double Cx = P.get(dI.get(2)).x;

        double Bx = P.get(dI.get(1)).x;
        double Dx = P.get(dI.get(3)).x;
        //Spalte 0 berechnen mit Abstand durch cleveres Runden

        for(int i=0;i<10;i++) {
            if(columnCount==0){
                Difference = Math.abs(Ax-Cx);
                Difference = Difference/9*i;

                if(Ax>Cx) {column.add((Ax)-Difference);}
                else {column.add((Ax)+Difference);}
            }

            if(columnCount==11) {
                Difference = Math.abs(Bx-Dx);
                Difference=Difference/9*i;

                if(Bx>Dx) {column.add(Bx-Difference);}
                else {column.add(Bx+Difference);}
            }
        }
        return column;
    }

    public ArrayList<Double> calculateColumnY (ArrayList<Point> P, ArrayList<Integer> dI, int columnCount) {

        ArrayList<Double> column = new ArrayList<>();

        double Difference;
        double Ay = P.get(dI.get(0)).y;
        double By = P.get(dI.get(1)).y;
        double Cy = P.get(dI.get(2)).y;
        double Dy = P.get(dI.get(3)).y;

        for(int i=0;i<10;i++) {
            if(columnCount==0){
                Difference = Math.abs(Cy-Ay);
                Difference = Difference/9*i;

                column.add(Ay+Difference);
            }

            if(columnCount==11) {
                Difference = Math.abs(By-Dy);
                Difference = Difference/9*i;

                column.add(By+Difference);
            }

        }

        return column;
    }

    public ArrayList<Point> calculateAllPoints(ArrayList<Double> C0x,ArrayList<Double> C0y,ArrayList<Double> C11x,ArrayList<Double> C11y){

        ArrayList<Point> AllPoints = new ArrayList<>();

        double Diffx,Diffy;
        int x,y;

        for(int row=0;row<10;row++) {
            for(int column=0;column<12;column++) {
                Diffx= C11x.get(row)-C0x.get(row);
                Diffy= C11y.get(row)-C0y.get(row);
                Diffx= (Diffx/11*column);
                Diffy= (Diffy/11*column);
                //xvalue
                x=(int)Math.abs(C0x.get(row)+Diffx);
                //yvalue
                if(C0y.get(row)>C11y.get(row)) {
                    y=(int)Math.abs(C0y.get(row)-Diffy);
                }
                else {
                    y=(int)Math.abs(C0y.get(row)+Diffy);
                }
                AllPoints.add(new Point(x,y));
            }
        }
        return AllPoints;
    }

    public String calculateLightToBitSequence(ArrayList<Point> AllPoints,ArrayList<Mat> AllMats,ArrayList<Bitmap> AllBitmaps){

        String bits = "";
        int x,y;
        int pixel;
        int token_synch=0;

        for(int j=0;j<AllBitmaps.size();j++) {
            Utils.matToBitmap(AllMats.get(j),AllBitmaps.get(j));

            for(int i=14;i<119;i++)
            {
                x=(int)AllPoints.get(i).x;
                y=(int)AllPoints.get(i).y;

                pixel = AllBitmaps.get(j).getPixel(x,y);
                if(pixel!=Color.BLACK) {
                    bits+="1";
                }
                else {
                    bits+="0";
                }
                if((bits.length()-token_synch)%56==0&&bits.length()!=0) {
                    bits+="-";
                    token_synch++;
                }

                switch(i) {
                    //After Lamp 21 (element 20) jump to Lamp 26 (element 25) etc.
                    case 20:i=25;break;
                    case 32:i=37;break;
                    case 44:i=49;break;
                    case 56:i=61;break;
                    case 68:i=73;break;
                    case 80:i=85;break;
                    case 92:i=97;break;
                    case 104:i=120;break;
                }
            }

        }


        return bits;
    }

    public String calcualteBitsequenceToASCIISymbols(String Bits){
        String PSK="";

        ArrayList<String> Anzeige = new ArrayList<>(Arrays.asList(Bits.split("-")));
        char help_char;

        for(int j=0;j<Anzeige.size();j++) {
            for (int i = 0; i <= Anzeige.get(j).length() - 7; i += 7) {
                help_char = (char) Integer.parseInt(Anzeige.get(j).substring(i, i + 7), 2);
                PSK += help_char;
            }
        }

        return PSK;
    }
}
