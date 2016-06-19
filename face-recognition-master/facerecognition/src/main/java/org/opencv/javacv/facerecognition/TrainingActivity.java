package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ai.api.model.Result;
import bluethooth.remote.control.Controller;
import texttospeach.TextToSpeechHelper;
import utilities.CommandExecution;

//import java.io.FileNotFoundException;
//import org.opencv.contrib.FaceRecognizer;


public class TrainingActivity extends Activity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;
    public static final int TRAINING = 0;
    public static final int SEARCHING = 1;
    public static final int IDLE = 2;
    private static final int frontCam = 1;
    private static final int backCam = 2;
    private int faceState = IDLE;
    private MenuItem nBackCam;
    private MenuItem mFrontCam;
    private MenuItem mEigen;
    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;
    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private int mLikely = 999;
    String mPath = "";
    private Tutorial3View mTutorial3View;
    private int mChooseCamera = backCam;

    Bitmap mBitmap;
    PersonRecognizer fr;
    ToggleButton toggleButtonGrabar;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;

    int faceOrder;

    static final long MAXIMG = 10;

    ArrayList<Mat> alimgs = new ArrayList<Mat>();

    int[] labels = new int[(int) MAXIMG];
    int countImages = 0;
    ImageView facePreview;

    labels labelsFile;

    private TextView textVoice, textResultVoice;
    CommandExecution commandExecuter;

    String username;

    TypedArray facesResource;


    // facebook

    private Controller controller;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //   System.loadLibrary("detection_based_tracker");


                    fr = new PersonRecognizer(mPath);
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mTutorial3View.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;


            }
        }
    };

    public TrainingActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        boolean traingFlag = false;

        faceOrder = 0;
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);


        setContentView(R.layout.train_layout);
        facesResource = getResources().obtainTypedArray(R.array.faces2);

        username = getIntent().getExtras().getString("usrname");

        Toast.makeText(getApplicationContext(), username, Toast.LENGTH_SHORT).show();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textVoice = (TextView) findViewById(R.id.text);
        textResultVoice = (TextView) findViewById(R.id.result);
        facePreview = (ImageView) findViewById(R.id.face_position);


        final TextToSpeechHelper textToSpeechHelper = new TextToSpeechHelper(getApplicationContext());
        commandExecuter = new CommandExecution(textToSpeechHelper, getApplicationContext());

        ///////////////////////
        mTutorial3View = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        mTutorial3View.setCvCameraViewListener(this);

        Log.i("widoooooooooooo", "ttttttttttt");


        mPath = getFilesDir() + "/facerecogOCV/";

        labelsFile = new labels(mPath);


        // Gawaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaad
        controller = new Controller();


        toggleButtonGrabar = (ToggleButton) findViewById(R.id.toggleButtonGrabar);
//        textState= (TextView)findViewById(R.id.textViewState);


        toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                grabarOnclick();
            }
        });


        boolean success = (new File(mPath)).mkdirs();
        if (!success) {
            Log.e("Error", "Error creating directory");
        }
    }

    void grabarOnclick() {
        if (toggleButtonGrabar.isChecked())
            faceState = TRAINING;
        else {
            if (faceState == TRAINING) ;
            // train();
            //fr.train();
            countImages = 0;
            faceState = IDLE;
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTutorial3View != null)
            mTutorial3View.disableView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkResult(Result result) {
        commandExecuter.setResult(result, this);
        commandExecuter.executeCommand();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mTutorial3View.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length == 1) && (faceState == TRAINING) && (countImages < MAXIMG)) {


            Mat m = new Mat();
            Rect r = facesArray[0];


            m = mRgba.submat(r);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");

            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            if (countImages >= MAXIMG - 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleButtonGrabar.setChecked(false);
                        grabarOnclick();
                        Log.d("Add", "HERE");
                        faceOrder++;
                        if(faceOrder<8) {
                            facePreview.setImageDrawable(facesResource.getDrawable(faceOrder));
                        }else{
                            Toast.makeText(getApplicationContext(),"Training Succes",Toast.LENGTH_LONG);
                            finish();
                        }
                    }
                });
            }
            if (countImages < MAXIMG) {
                    fr.add(m, username.toString());
                    countImages++;
            }
        }

        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }

        if (facesArray.length > 0) {
            moveSAR(facesArray[0]);
        }
        Log.i("detected faces", facesArray.length + "");

//        Core.flip(mRgba.t(), mRgba.t(), -1);

        //  if(facesArray.length>1){
        //     controller.goUp();
        // }else if(facesArray.length == 1){
        //    controller.goRight();
        // }


        Core.flip(mRgba, mRgba, 1);

        return mRgba;
    }

    private void moveSAR(Rect rect) {
        Point topLeft = rect.tl();
        Point bottomRight = rect.br();
        Log.e("Top Leftttttttttttttt", "" + topLeft.toString());
        Log.e("Bottom Rightttttttttt", "" + bottomRight.toString());

        Point center = new Point((topLeft.x + bottomRight.x) / 2, (topLeft.y + bottomRight.y) / 2);

        Tutorial3View opencvDis = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        int opencvDisHeight = opencvDis.getHeight();
        int opencvDisWidth = opencvDis.getWidth();

        Log.e("Screen Widthhhhhh", "" + opencvDisWidth + "");
        Log.e("Screen Heightttttt", "" + opencvDisHeight + "");

        int horizontalBlock = checkHorizontalGrid(center.x, opencvDisWidth);

        Log.e("Horizontal blockkkkk", horizontalBlock + "");

        int verticalBlock = checkVerticalGrid(center.y, opencvDisHeight);

        Log.e("Vertical blockkkkkk", verticalBlock + "");

        if (horizontalBlock == 2 && verticalBlock == 1) {
            return; // almost in center
        }

        // horizontal
        switch (horizontalBlock) {
            case 0: // far left
                Log.e("Go", "Lefttttttttttttttttttttttttttttttttttttttt");
                controller.goLeft(10);
                break;
            case 1: // left
                Log.e("Go", "Leftttttttttttttttttttttttttttttttttttttt");
                controller.goLeft(5);
                break;
            case 3: // rgiht
                Log.e("Go", "Rightttttttttttttttttttttttttttttttttttttt");
                controller.goRight(5);
                break;
            case 4: // far right
                Log.e("Go", "Rightttttttttttttttttttttttttttttttttttttt");
                controller.goRight(10);
                break;
        }

        switch (verticalBlock) {
            case 0:
                controller.goUp(5);
                break;
            case 2:
                controller.goDown(5);
                break;
        }
       /*
//        Display display = getWindowManager().getDefaultDisplay();
//        android.graphics.Point size = new android.graphics.Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;

//        Log.e("Widthhhhhhhhhhhhhhhhhhh", ""+width);
//        Log.e("Heightttttttttttttttttt", ""+height);
//
//        int displayCenterX = width/2;
//        int displayCenterY = height/2;

        Log.e("center x",center.x+"");
        Log.e("center y",center.y+"");

//        Log.e("displaycenter x",displayCenterX+"");

//        displayCenterX/=2;

        Tutorial3View opencvDis=(Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        int opencvDisHeight =  opencvDis.getHeight();
        int opencvDisWidth = opencvDis.getWidth();
        Log.e("Heightttttttttttttttttt", "" + opencvDisHeight);
        Log.e("Widthhhhhhhhhhhhhhhhhhh", "" + opencvDisWidth);

        int opencvDisCenterX = opencvDisWidth / 2;
        int opencvDisCenterY = opencvDisHeight / 2;


        // we calculate the distance between the person and the screen
        int sign = center.x - opencvDisCenterX > 0 ? 1 : 0;

        double x = Math.abs(center.x - opencvDisCenterX);
        double y = Math.abs(topLeft.x - bottomRight.x);
        double depthFactor = 29600;

        int theta = -1;
//        if(y != 0){
            theta = (int) Math.toDegrees(Math.atan( (y * x) / (depthFactor+10*y)));
            Log.i("Thetaaaaaaaaaaaaaaa",theta+" "+sign);
//        }

        if(theta != -1) {
            if (sign == 1) {
                controller.goRight(theta);
            } else {
                controller.goLeft(theta);
            }
        }


//        if(opencvDisCenterX - 100 > center.x){
//            controller.goRight();
//
//            Log.e("leffffffft","going to left");
//
//        }else if(opencvDisCenterX + 100 < center.x){
//            controller.goLeft();
//
//            Log.e("rightttttt","going right");
//        }
//
//        if(opencvDisCenterY - 10 > center.y){
//            controller.goUp();
//
//            Log.e("leffffffft","going to left");
//
//        }else if(opencvDisCenterY + 10 < center.y) {
//            controller.goDown();
//
//            Log.e("rightttttt","going right");
//        }

*/
    }

    private int checkVerticalGrid(double y, int opencvDisHeight) {

        int tempGrid = (int) y / (opencvDisHeight / 10);

        if (tempGrid <= 2) {
            return 0;
        }
        if (tempGrid >= 7) {
            return 2;
        } else {
            return 1;
        }
    }

    private int checkHorizontalGrid(double x, int opencvDisWidth) {


        return (int) x / (opencvDisWidth / 5);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        if (mTutorial3View.numberCameras() > 1) {
            nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
            mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));

//        mEigen = menu.add("EigenFaces");
//        mLBPH.setChecked(true);
        }

        //mOpenCvCameraView.setAutofocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
//        if (item == mItemFace50)
//            setMinFaceSize(0.5f);
//        else if (item == mItemFace40)
//            setMinFaceSize(0.4f);
//        else if (item == mItemFace30)
//            setMinFaceSize(0.3f);
//        else if (item == mItemFace20)
//            setMinFaceSize(0.2f);
//        else if (item == mItemType) {
//            mDetectorType = (mDetectorType + 1) % mDetectorName.length;
//            item.setTitle(mDetectorName[mDetectorType]);
//            setDetectorType(mDetectorType);
//        
//        }
        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        //  mEigen.setChecked(false);
        if (item == nBackCam) {
            mTutorial3View.setCamFront();
            mChooseCamera = frontCam;
        }
        //fr.changeRecognizer(0);
        else if (item == mFrontCam) {
            mChooseCamera = backCam;
            mTutorial3View.setCamBack();

        }

        item.setChecked(true);

        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }


}
