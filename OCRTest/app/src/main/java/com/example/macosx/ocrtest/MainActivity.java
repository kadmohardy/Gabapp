package com.example.macosx.ocrtest;

public class MainActivity
{
}
/*
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Scalar;
import org.opencv.android.LoaderCallbackInterface;

import static org.opencv.core.Core.circle;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.Core.line;
import static org.opencv.core.Core.rectangle;
import static org.opencv.imgproc.Imgproc.BORDER_TRANSPARENT;
import static org.opencv.imgproc.Imgproc.CV_HOUGH_GRADIENT;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.HoughCircles;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpAffine;
import static org.opencv.imgproc.Imgproc.warpPerspective;


public class MainActivity extends AppCompatActivity {

    private TextView mResult;
    private ProgressDialog mProgressDialog;
    private ImageView mImage;
    private Button mButtonGallery, mButtonCamera;
    private String mCurrentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    //private ImageScanner mScanner;
    private Mat mOriginalImageMat;
    private Bitmap mBitmap;

    private static final int NUMBER_OF_QUESTIONS = 20;
    private static final int NUMBER_OF_OPTIONS = 4;
    private int count;
    private int countMarked;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCVINIT", "OpenCV loaded sucessfully");
                }
                break;

                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("OnCreate METODO ", "ESTA AQUI");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //mScanner = new ImageScanner();
        mResult = findViewById(R.id.tv_result);
        mImage = findViewById(R.id.image);
        mButtonGallery = findViewById(R.id.bt_gallery);
        mButtonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });


        mButtonCamera = findViewById(R.id.bt_camera);
        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

    }

    private void uriOCR(Uri uri) {

        if (uri != null) {
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                mImage.setImageBitmap(bitmap);

                //doOMR(bitmap);
                getPontuacao(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            boolean success = OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
            if (!success)
                Log.e("OpenCV", "Asynchronous initialization failed!");
            else
                Log.d("OpenCV", "Asynchronous initialization succeeded!");
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
        }


        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri =  intent
                    .getParcelableExtra(Intent.EXTRA_STREAM);
            uriOCR(uri);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String storageDir = Environment.getExternalStorageDirectory()
                + "/TessOCR";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        if (requestCode == REQUEST_TAKE_PHOTO
                && resultCode == Activity.RESULT_OK) {

            setPic();
        }
        else if (requestCode == REQUEST_PICK_PHOTO
                && resultCode == Activity.RESULT_OK) {

            Uri uri = data.getData();
            if (uri != null) {
                uriOCR(uri);
            }
        }
    }

    private void setPic() {

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor << 1;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImage.setImageBitmap(bitmap);

        //doOCR(bitmap);

    }


    private void setPic(Bitmap bitmap) {

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        // Get the dimensions of the bitmap

        int photoW = bitmap.getWidth();
        int photoH = bitmap.getHeight();

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        mImage.setImageBitmap(bitmap);

        //doOCR(bitmap);

    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    private void takePhoto()
    {
        dispatchTakePictureIntent();
    }

    private void doOCR(final Bitmap bitmap)
    {
        Log.d("DOOCR", "ESTA AQUI");

        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Processing",
                    "Doing OCR...", true);
        }
        else {
            mProgressDialog.show();
        }

        new Thread(new Runnable() {
            public void run() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        mProgressDialog.dismiss();
                    }

                });

            }
        }).start();
    }
    /////////////////////

    public Rect MakeRect(int x1, int y1, int x2, int y2)
    {
        Rect touchedRect = new Rect();

        touchedRect.x = x1;
        touchedRect.y = y1;
        touchedRect.width = x2 - x1;
        touchedRect.height = y1 - y2;

        return touchedRect;
    }

    public Point RotPoint(Point p, Point o, double rad)
    {
        Point p1 = new Point(p.x - o.x, p.y - o.y);
        float CoordX =  (float) (p1.x * Math.cos(rad) - p1.y * Math.sin(rad) + o.x);
        float CoordY = (float) (p1.x * Math.sin(rad) + p1.y * Math.cos(rad) + o.y);
        return new Point(CoordX, CoordY);
    }

    public void DrawRects(Mat img, Point rtr, Point rbl)
    {
        Mat imgRotated = new Mat();

        List<Rect> rects = new ArrayList<Rect>();

        Point pageMakerSuperiorRight = new Point(1084, 76);
        Point pageMakerInferiorLeft = new Point(77, 1436);

        //NameBox

        rects.add(MakeRect(223, 105, 603, 152));
        //PayRollBox
        rects.add(MakeRect(223, 152, 603, 198));
        //SIN
        rects.add(MakeRect(223, 198, 603, 244));
        //Address
        rects.add(MakeRect(223, 244, 603, 290));
        //Postal
        rects.add(MakeRect(223, 291, 603, 336));
        //Picture
        rects.add(MakeRect(129, 491, 765, 806));

        //Fix rotation angle
        double angle = Math.atan2(pageMakerSuperiorRight.y - pageMakerInferiorLeft.y, pageMakerSuperiorRight.x - pageMakerInferiorLeft.x);
        double realAngle = Math.atan2(rtr.y - rbl.y, rtr.x - rbl.x);
        double angleShift = -(angle - realAngle);

        //Rotate image
        int s1 = img.cols();
        int s2 = img.rows();
        Size size = new Size(s1, s2);

        Point rc = new Point((rtr.x + rbl.x) / 2, (rbl.y + rtr.y) / 2);
        Mat rotMat = Imgproc.getRotationMatrix2D(rc, angleShift / 3.14159265359 * 180.0, 1.0);
        warpAffine(img, imgRotated, rotMat, img.size(), INTER_CUBIC, BORDER_TRANSPARENT);


        rtr = RotPoint(rtr, rc, -angleShift);
        rbl = RotPoint(rbl, rc, -angleShift);

        //Calculate ratio between template and real image
        double realWidth = rtr.x - rbl.x;
        double realHeight = rbl.y - rtr.y;
        double width = pageMakerSuperiorRight.x - pageMakerInferiorLeft.x;
        double height = pageMakerInferiorLeft.y - pageMakerSuperiorRight.y;
        double wr = realWidth / width;
        double hr = realHeight / height;

        circle(img, rbl, 3, new Scalar(0, 255, 0), 2);
        circle(img, rtr, 3, new Scalar(0, 255, 0), 2);

        for (int i = 0; i < rects.size(); i++)
        {
            Rect r = rects.get(i);
            double x1 = (r.x - pageMakerSuperiorRight.x) * wr + rtr.x;
            double y1 = (r.y - pageMakerSuperiorRight.y) * hr + rtr.y;
            double x2 = (r.x + r.width - pageMakerSuperiorRight.x) * wr + rtr.x;
            double y2 = (r.y + r.height - pageMakerSuperiorRight.y) * hr + rtr.y;

            rectangle(img, new Point((float) x1, (float) y1), new Point((float)x2, (float)y2), new Scalar(0, 0, 255), 3);
        }
    }

    public void doOMR(Bitmap bitmap)
    {

        Mat imageToProcess = new Mat();
        Utils.bitmapToMat(bitmap, imageToProcess);

        Mat grey = new Mat();

        Imgproc.cvtColor(imageToProcess, grey, Imgproc.COLOR_BGR2GRAY);
        int width = imageToProcess.cols();
        int height = imageToProcess.rows();
        //long raw =  grey.dataAddr();

        Image imageAux = new Image() ;
        imageAux = new Image(width, height, "Y800");


        // scan the image for barcodes
        int result = mScanner.scanImage(imageAux);

        //Top right point
        Point tr = new Point(0,0);
        Point bl = new Point(0,0);

        Iterator<Symbol> symbols = imageAux.getSymbols().iterator();
        while (symbols.hasNext()){
            Symbol currSymbol = symbols.next();
            int b1[] = currSymbol.getBounds();
            int b2[] = currSymbol.getLocationPoint(3);
        }

    }


    public void sheetsPreProcessing(Mat imageGabarito)
    {

        Size size = new Size(3,3);
        GaussianBlur(imageGabarito, imageGabarito, size, 0);
        adaptiveThreshold(imageGabarito,
                            imageGabarito,
                            255,
                            Imgproc.ADAPTIVE_THRESH_MEAN_C,
                            Imgproc.THRESH_BINARY,
                            75,
                            10);
        Core.bitwise_not(imageGabarito, imageGabarito);
    }

    public void getPontuacao(Bitmap bitmap)
    {
        mOriginalImageMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mOriginalImageMat);

        //Gray scale
        Imgproc.cvtColor(mOriginalImageMat, mOriginalImageMat, Imgproc.COLOR_BGR2GRAY);

        //Pre processing
        sheetsPreProcessing(mOriginalImageMat);

        Mat img2 = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Imgproc.cvtColor(mOriginalImageMat, img2, Imgproc.COLOR_GRAY2RGB);

        Mat img3 = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Imgproc.cvtColor(mOriginalImageMat, img3, Imgproc.COLOR_GRAY2RGB);


        // Detect page markers
        // Create new rgb image

        //Converting the image to grayscale
        detectPageMarkers(mOriginalImageMat, img2, img3);

        //Utils.matToBitmap(img3, bitmap);

        //mImage.setImageBitmap(bitmap);

    }

    public void detectPageMarkers(Mat image, Mat imageResult, Mat imageResult2)
    {
        Mat lines = new Mat();

        lines.create(image.rows(), image.cols(), CvType.CV_8UC1);

        // Detect Lines
        Imgproc.HoughLinesP(image, lines, 1, Math.PI/180, 80, 400, 10);

        //Drawing lines on the image
        for (int i = 0; i < lines.cols(); i++) {
            double[] points = lines.get(0, i);
            double x1, y1, x2, y2;

            x1 = points[0];
            y1 = points[1];
            x2 = points[2];
            y2 = points[3];

            Point pt1 = new Point(x1, y1);
            Point pt2 = new Point(x2, y2);

            //Drawing lines on an image
            line(imageResult, pt1, pt2, new Scalar(0, 0, 255), 3, Core.LINE_AA);

        }

        ArrayList<Point> corners = new ArrayList<Point>();
        for (int i = 0; i < lines.cols(); i++)
        {
            for (int j = i+1; j < lines.cols(); j++)
            {
                double[] points1 = lines.get(0, i);
                double[] points2 = lines.get(0, j);

                int[] p1 = new int[]{(int) points1[0], (int) points1[1], (int) points1[2], (int) points1[3]};
                int[] p2 = new int[]{(int) points2[0], (int) points2[1], (int) points2[2], (int) points2[3]};

                Point pt = computeIntersect(p1, p2);

                if (pt.x >= 0 && pt.y >= 0 && pt.x < imageResult.cols() && pt.y < imageResult.rows())
                    corners.add(pt);
            }
        }

        // Get mass center
        int xMassa = 0;
        int yMassa = 0;

        for (int i = 0; i < corners.size(); i++){
            xMassa += corners.get(i).x;
            yMassa += corners.get(i).y;
        }

        xMassa = (xMassa/corners.size());
        yMassa = (yMassa/corners.size());
        Point center = new Point(xMassa, yMassa);

        sortCorners(corners, center);

        // Draw corner points
        for (int i = 0; i < corners.size(); i++) {
            circle(imageResult, corners.get(i), 20,
                    new Scalar(0, 255, 0), 2);
        }


        if (corners.size() == 0){

        }


        MatOfPoint cornersMat = new MatOfPoint();
        cornersMat.fromList(corners);



        //########################################################################################//
        //########################################################################################//
        //PERSPECTIVA DA IMAGEM
        Point bottomLeft = corners.get(2);
        Point bottomRight = corners.get(3);
        Point topRight = corners.get(1);
        Point topLeft = corners.get(0);

        MatOfPoint2f cornersMat2f = new MatOfPoint2f(
                bottomLeft,
                bottomRight,
                topRight,
                topLeft);

        Rect r = boundingRect(cornersMat);
        Mat quadrado = Mat.zeros(r.height, r.width, CvType.CV_8UC3);

        Mat matOfQuad2f = new MatOfPoint2f(
                new Point(0, 0),
                new Point(quadrado.cols(),0),
                new Point(quadrado.cols(), quadrado.rows()),
                new Point(0, quadrado.rows())
        );

        //Drawing lines on the image
        rectangle(quadrado, new Point(r.x,r.y), new Point(r.width, r.height),
                new Scalar(0,255,0));

        Mat transMatrix = new Mat();
        transMatrix = getPerspectiveTransform(cornersMat2f, matOfQuad2f);
        warpPerspective(imageResult2, quadrado, transMatrix, quadrado.size());


        double kx1 = 0.122;
        double kx2 = 0.673;
        double ky = 0.157;

        double [] arrayKX = {0.122, kx1*1.75, kx1*2.5, kx1*3.25, kx2, kx2*1.135, kx2*1.273, kx2*1.408};
        double [] arrayKY = {ky, ky*1.55, ky*2.1, ky*2.66, ky*3.21,
                ky*3.75, ky*4.3, ky*4.85, ky*5.4, ky*5.95};

        circle(quadrado, new Point(r.width*0.122, r.height*ky*3.21), 3, new Scalar(0, 255, 0), 2, 8 ,0);
        circle(quadrado, new Point(r.width*kx1*1.75, r.height*ky*3.21), 3, new Scalar(0, 255, 0), 2, 8 ,0);
        circle(quadrado, new Point(r.width*kx1*2.5, r.height*ky*3.21), 3, new Scalar(0, 255, 0), 2, 8 , 0);
        circle(quadrado, new Point(r.width*kx1*3.25, r.height*ky*3.21), 3, new Scalar(0, 255, 0), 2, 8 ,0);



        //########################################################################################//
        //########################################################################################//
        // DETECTA AS RESPOSTAS
        int sheetCircleRadius = r.width/40;

        int[][] answersMat = detectSheetsCoodinates(quadrado, r.width, r.height, sheetCircleRadius);
        //mResult.setText(buildAnswersSrt(answersMat));

        setImage(quadrado);

    }

    public String buildAnswersSrt(int[][] answersMat)
    {
        String answersStr = "";
        countMarked = 0;
        for (int i = 0; i < NUMBER_OF_QUESTIONS; i++)
        {
            int questionSum = 0;
            String strAux = "";

            if (answersMat[i][0] == 1){
                questionSum += 1;
                strAux = Integer.toString(i+1) + "-A ";
                countMarked++;
            }
            if (answersMat[i][1] == 1){
                questionSum += 1;
                strAux = Integer.toString(i+1) + "-B ";
                countMarked++;
            }
            if (answersMat[i][2] == 1){
                questionSum += 1;
                strAux = Integer.toString(i+1) + "-C ";
                countMarked++;
            }
            if (answersMat[i][3] == 1){
                questionSum += 1;
                strAux = Integer.toString(i+1) + "-D ";
                countMarked++;
            }
            if (questionSum > 1){
                strAux = Integer.toString(i+1) + "-MM ";
            }
            if (questionSum == 0){
                strAux = Integer.toString(i+1) + "-NR ";
            }

            answersStr += strAux;

        }

        return answersStr;
    }

    public int verifyIfSheetIsMarked(Mat imageSheet, Point optionCircle, int radius, int count)
    {
        int isMarked = 0;
        int contador = count;
        Rect rectSheet = new Rect((int) optionCircle.x, (int)optionCircle.y, 2*radius, 2*radius);
        Mat submatSheet = new Mat(imageSheet, rectSheet);

        Imgproc.cvtColor(submatSheet, submatSheet, Imgproc.COLOR_BGR2GRAY);

        double p = (double) countNonZero(submatSheet)/(submatSheet.size().width*submatSheet.size().height);

        if(p >= 0.25){
            isMarked = 1;
        }
        //1 - 6 - 11 - 14 - 19 - 24
        return isMarked;
    }

    public int [][] detectSheetsCoodinates(Mat imageSheet, int imageWidth, int imageHeight, int radius)
    {
        ArrayList<Point> uusheetsCenterArray = new ArrayList<Point>();

        double kx1 = 0.122;
        double kx2 = 0.673;
        double ky = 0.157;

        double [] arrayKX = {0.122, kx1*1.75, kx1*2.5, kx1*3.25, kx2, kx2*1.135, kx2*1.273, kx2*1.408};
        double [] arrayKY = {ky, ky*1.55, ky*2.1, ky*2.66, ky*3.21,
                             ky*3.75, ky*4.3, ky*4.85, ky*5.4, ky*5.95};

        // 20 linhas (vinte questoes e 4 opcoes)
        // A = 1, B = 2, C = 3, D = 4

        int [][] sheets = new int[NUMBER_OF_QUESTIONS][NUMBER_OF_OPTIONS];
        count = 1;
        for (int i = 0 ; i < NUMBER_OF_QUESTIONS; i++)
        {
            // Detecta a questao i
            ArrayList<Point> pointsQuestion = new ArrayList<Point>();

            for (int j = 0; j < 4; j++)
            {
                Point pAux;
                //questoes (1 a 10)
                if (i < 10)
                {
                    pAux = new Point((int) imageWidth*arrayKX[j], (int) imageHeight*arrayKY[i]);
                    //sheetsCenterArray.add(pAux);
                    //pointsQuestion.add(pAux);
                } else
                {
                    pAux = new Point((int) imageWidth*arrayKX[j+4], (int) imageHeight*arrayKY[i-10]);
                    //sheetsCenterArray.add(pAux);
                    //pointsQuestion.add(pAux);
                }

                sheets[i][j] = verifyIfSheetIsMarked(imageSheet, pAux, radius, count);
                count ++;
            }


        }

        return sheets;
    }

    public void setImage(Mat mat)
    {
        Bitmap bit = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bit);

        mImage.setImageBitmap(bit);
    }

    public MatOfPoint getMatOfPointFromVector4Points(ArrayList<Point> points)
    {
        MatOfPoint mat = new MatOfPoint();
        ArrayList<Point> pointsToBeAdd = new ArrayList<Point>();

        for (int i = 0; i < points.size(); i++) {
            Point aux = points.get(i);
            int [] pointsAux = {(int ) aux.x, (int) aux.y};
            mat.put(i,0, pointsAux);
        }
        return mat;
    }

    public void sortCorners(ArrayList<Point> corners, Point center)
    {
        ArrayList<Point> topLeft = new ArrayList<>();
        ArrayList<Point> botLeft = new ArrayList<>();
        ArrayList<Point> topRight = new ArrayList<>();
        ArrayList<Point> botRight = new ArrayList<>();


        for (int i = 0; i < corners.size(); i++)
        {
            if (corners.get(i).y > center.y && corners.get(i).x > center.x){
                topRight.add(corners.get(i));
            }
            else if (corners.get(i).y > center.y && corners.get(i).x < center.x)
            {
                topLeft.add(corners.get(i));
            }
            else if (corners.get(i).y < center.y && corners.get(i).x < center.x){
                botLeft.add(corners.get(i));
            } else {
                botRight.add(corners.get(i));
            }
        }

        corners.clear();

        corners.add(getMean(topLeft));
        corners.add(getMean(topRight));
        corners.add(getMean(botLeft));
        corners.add(getMean(botRight));

    }

    public Point getMean(ArrayList<Point> points)
    {
        int xMean = 0;
        int yMean = 0;

        for (int i = 0; i < points.size(); i++)
        {
            xMean += points.get(i).x;
            yMean += points.get(i).y;
        }
        xMean = xMean/points.size();
        yMean = yMean/points.size();

        return new Point(xMean, yMean);
    }

    public Point computeIntersect(int[] a,
                                  int[] b)
    {
        int x1 = a[0], y1 = a[1], x2 = a[2], y2 = a[3], x3 = b[0], y3 = b[1], x4 = b[2], y4 = b[3];

        try{
            Point pt = new Point();
            float d = (float)((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));
                if (d == 0)
                {
                    return new Point(-1,-1);
                }
            pt.x = ((x1*y2 - y1*x2) * (x3-x4) - (x1-x2) * (x3*y4 - y3*x4)) / d;
            pt.y = ((x1*y2 - y1*x2) * (y3-y4) - (y1-y2) * (x3*y4 - y3*x4)) / d;

            if(pt.x < Math.min(x1,x2)  || pt.x > Math.max(x1,x2)  ||
                    pt.y < Math.min(y1,y2)  || pt.y > Math.max(y1,y2) ){
                return new Point(-1,-1);
            }
            if(pt.x < Math.min(x3,x4) || pt.x > Math.max(x3,x4) ||
                    pt.y < Math.min(y3,y4) || pt.y > Math.max(y3,y4) ){
                return new Point(-1,-1);
            }
            return pt;
        } catch (Exception ex){
            return new Point(-1, -1);
        }
    }

    public static Mat adaptativeProcess(Mat img, Mat corners)
    {

        Mat mat = Mat.zeros(4,2,CvType.CV_32F);
        mat.put(0,0,0); mat.put(0,1,512);
        mat.put(1,0,0); mat.put(1,1,0);
        mat.put(2,0,512); mat.put(2,1,0);
        mat.put(3,0,512); mat.put(3,1,512);

        mat = Imgproc.getPerspectiveTransform(corners, mat);

        Mat M = new Mat();

        Imgproc.warpPerspective(img, M, mat, new Size(512,512));

        return M;
    }
}

*/