package stock_christian.lightcomm;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Calculations {

    public class DistancesWithPoints {

        public DistancesWithPoints (Point sp,Point ep,Double di){
            StartPoint = sp;
            EndPoint   = ep;
            Distance   = di;
            Position   = "";
        }

        public Point StartPoint = new Point();
        public Point EndPoint = new Point();
        public Double Distance;
        public String Position;
    }

    private ArrayList<Point> Coordinates;
    private ArrayList<Double> distanceList;

    private HashMap<Integer, DistancesWithPoints> DistanceWithPointsList = new HashMap<>();
    private HashMap<String, Point> SquarePoints = new HashMap<>();

    ArrayList<Double> C0x;
    ArrayList<Double> C0y;
    ArrayList<Double> C11x;
    ArrayList<Double> C11y;

    ArrayList<Point> AllPoints;

    public String calculateSignal(ArrayList<Mat> AllMats, ArrayList<Bitmap> AllBitmaps){
        int count = 0;

        String PSK = "";
        String Bits;



        for(Mat SingleMat: AllMats){

            AllPoints = new ArrayList<Point>();
            Coordinates = new ArrayList<Point>();
            distanceList = new ArrayList<Double>();
            DistanceWithPointsList = new HashMap<>();

            Bits = "";


            calculateBoundingBoxCenter(SingleMat);

            calculateAllDistances();

            determingPointsInSquare();

            C0x  = calculateColumnX(0);
            C0y  = calculateColumnY(0);
            C11x = calculateColumnX(11);
            C11y = calculateColumnY(11);

            calculateAllPoints();

            Bits = calculateLightToBitSequence(SingleMat,AllBitmaps.get(count));

            PSK = PSK + calculateBitsequenceToASCIISymbols(Bits);

            count++;
        }
        return PSK;
    }

    public void calculateBoundingBoxCenter(Mat SiMat){
        ArrayList<Integer> x = new ArrayList<>();
        ArrayList<Integer> y = new ArrayList<>();

        Mat Mat_hierarchy = new Mat();

        List<MatOfPoint> MoP_contours = new ArrayList<>();
        Imgproc.findContours(SiMat, MoP_contours, Mat_hierarchy, 0, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        Imgproc.drawContours(SiMat, MoP_contours, -1, new Scalar(255, 255, 255), -1);

        List<Moments> mu = new ArrayList<>(MoP_contours.size());
        for (int i = 0; i < MoP_contours.size(); i++) {
            mu.add(i, Imgproc.contourMoments(MoP_contours.get(i)));
            Moments p = mu.get(i);
            x.add((int) (p.get_m10() / p.get_m00()));
            y.add((int) (p.get_m01() / p.get_m00()));
            Coordinates.add(new Point(x.get(i), y.get(i)));
        }

        Mat_hierarchy.release();
    }

    public void calculateAllDistances(){

        double x1,x0,y1,y0;
        int Anzahl = Coordinates.size();
        Double Distance;
        Double FirstBiggestDistance = 0.0;
        Double SecondBiggestDistance= 0.0;

        for(int i = 0;i<(Anzahl-1); i++ ){
            x1= Coordinates.get(i).x;
            y1= Coordinates.get(i).y;
            for (int j = (i+1);j<Anzahl;j++) {
                x0= Coordinates.get(j).x;
                y0= Coordinates.get(j).y;
                distanceList.add(Math.sqrt(((x1-x0)*(x1-x0))+((y1-y0)*(y1-y0))));
                Distance = Math.sqrt(((x1-x0)*(x1-x0))+((y1-y0)*(y1-y0)));
                if ( Distance > FirstBiggestDistance){

                    if(FirstBiggestDistance != 0.0)
                        DistanceWithPointsList.put(1,DistanceWithPointsList.get(0));

                    DistanceWithPointsList.put(0,new DistancesWithPoints(new Point(x1,y1),new Point(x0,y0),Distance));
                    SecondBiggestDistance = FirstBiggestDistance;
                    FirstBiggestDistance = Distance;
                }else{
                    if ( Distance > SecondBiggestDistance){
                        DistanceWithPointsList.put(1,new DistancesWithPoints(new Point(x1,y1),new Point(x0,y0),Distance));
                        SecondBiggestDistance = Distance;
                    }
                }
            }
        }
    }

    public void determingPointsInSquare(){

        double NearDistanceOrigin = 2000.0;
        double FarDistanceOrigin = 0.0;
        double Distance;


        //Calculate Highest Distance to Origin = D and Nearest = A
        for (int i=0;i<2;i++){
            Distance = Math.sqrt(Math.pow(DistanceWithPointsList.get(i).StartPoint.x,2)+Math.pow(DistanceWithPointsList.get(i).StartPoint.y,2));
            if (Distance < NearDistanceOrigin) {
                NearDistanceOrigin = Distance;
                SquarePoints.put("A",DistanceWithPointsList.get(i).StartPoint);
            }

            if (Distance > FarDistanceOrigin) {
                FarDistanceOrigin = Distance;
                SquarePoints.put("D",DistanceWithPointsList.get(i).StartPoint);
            }

            Distance =  Math.sqrt(Math.pow(DistanceWithPointsList.get(i).EndPoint.x,2)+Math.pow(DistanceWithPointsList.get(i).EndPoint.y,2));
            if (Distance < NearDistanceOrigin) {
                NearDistanceOrigin = Distance;
                SquarePoints.put("A", DistanceWithPointsList.get(i).EndPoint);
            }

            if (Distance > FarDistanceOrigin) {
                FarDistanceOrigin = Distance;
                SquarePoints.put("D",DistanceWithPointsList.get(i).EndPoint);
            }

        }

        if (SquarePoints.containsValue(DistanceWithPointsList.get(0).StartPoint) || (SquarePoints.containsValue(DistanceWithPointsList.get(0).EndPoint))){
            if (DistanceWithPointsList.get(1).StartPoint.y < DistanceWithPointsList.get(1).EndPoint.y){
                SquarePoints.put("B",DistanceWithPointsList.get(1).StartPoint);
                SquarePoints.put("C",DistanceWithPointsList.get(1).EndPoint);
            } else {
                SquarePoints.put("C",DistanceWithPointsList.get(1).StartPoint);
                SquarePoints.put("B",DistanceWithPointsList.get(1).EndPoint);
            }
        }
        else {
            if (DistanceWithPointsList.get(0).StartPoint.y < DistanceWithPointsList.get(0).EndPoint.y){
                SquarePoints.put("B",DistanceWithPointsList.get(0).StartPoint);
                SquarePoints.put("C",DistanceWithPointsList.get(0).EndPoint);
            } else {
                SquarePoints.put("C",DistanceWithPointsList.get(0).StartPoint);
                SquarePoints.put("B",DistanceWithPointsList.get(0).EndPoint);
            }
        }
    }

    public ArrayList<Double> calculateColumnX (int columnCount) {
        ArrayList<Double> column = new ArrayList<>();

        double Difference;
        double Ax = SquarePoints.get("A").x;
        double Bx = SquarePoints.get("B").x;
        double Cx = SquarePoints.get("C").x;
        double Dx = SquarePoints.get("D").x;
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

    public ArrayList<Double> calculateColumnY (int columnCount) {

        ArrayList<Double> column = new ArrayList<>();

        double Difference;
        double Ay = SquarePoints.get("A").y;
        double By = SquarePoints.get("B").y;
        double Cy = SquarePoints.get("C").y;
        double Dy = SquarePoints.get("D").y;

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

    public void calculateAllPoints(){

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
        String xml = "";
    }

//    public ArrayList<Bitmap> Morph(ArrayList<Mat> AllMats, ArrayList<Bitmap> Bit){
//
//        for (int i = 0; i<AllMats.size(); i++){
//            Utils.matToBitmap(AllMats.get(i),Bit.get(i));
//        }
//
//        return Bit;
//    }

    public String calculateLightToBitSequence(Mat simat,Bitmap bmp){

        int x, y;
        int pixel;
        String Bits = "";
        String CountBit = "";

        Utils.matToBitmap(simat, bmp);

        for (int i = 15; i < 119; i++) {

            x = (int) AllPoints.get(i).x;
            y = (int) AllPoints.get(i).y;

            pixel = bmp.getPixel(x, y);

            if (pixel != Color.BLACK) {
                Bits += "1";
            } else {
                Bits += "0";
            }
//
//            if(i==24 || i==36 || i==48 || i==60 || i==72 || i==84)
//            {
//                if(pixel != Color.BLACK){
//                    CountBit+="1";
//                }
//                else{
//                    CountBit+="0";
//                }
//            }else{
////                if(Bits.length()%7==0 )
////                {
////                    Bits+= "0";
////                }
//
//                if (pixel != Color.BLACK) {
//                    Bits += "1";
//                } else {
//                    Bits += "0";
//                }
//            }
//
//            switch(i)
//            {
//                case 20: i = 23; break;// CountBit
//
//                case 24: i = 26; break;// Next UseByte
//                case 32: i = 35; break;// CountBit Again
//
//                case 36: i = 38; break;// Next UseByte Again
//                case 44: i = 47; break;
//
//                case 48: i = 50; break;
//                case 56: i = 59; break;
//
//                case 60: i = 62; break;
//                case 68: i = 71; break;
//
//                case 72: i = 74; break;
//                case 78: i = 83; break;
//
//                case 84: i = 86; break;
//                case 92: i = 97; break;
//
//                case 104: i = 120; break;

//            }
        }
        return Bits;
    }

    public String calculateBitsequenceToASCIISymbols(String Bits){

        String PSK = "";
        char singleLetter;

        for(int i = 0; i<= Bits.length()-7; i+=7){
            singleLetter = (char) Integer.parseInt(Bits.substring(i, i+7),2);
            PSK += singleLetter;
        }
        return PSK;
    }
}
