package org.opencv.javacv.facerecognition;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.internal.telephony.ITelephony;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.JsonElement;

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
import java.util.Arrays;
import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import bluethooth.remote.control.Controller;
import testingairesponse.ListeningActivity;
import testingairesponse.VoiceRecognitionListener;
import texttospeach.TextToSpeechHelper;
import utilities.CommandExecution;
import utilities.DatabaseHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import com.facebook.FacebookSdk;

//import java.io.FileNotFoundException;
//import org.opencv.contrib.FaceRecognizer;


public class FdActivity extends ListeningActivity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;

    public static final int TRAINING = 0;
    public static final int SEARCHING = 1;
    public static final int IDLE = 2;

    private static final int frontCam = 1;
    private static final int backCam = 2;


    private int faceState = SEARCHING;
    private MenuItem nBackCam;
    private MenuItem mFrontCam;
    private MenuItem connectSAR;
    private MenuItem addUserItem;
//    private MenuItem

    int x = 0;
    private Mat mRgba;
    private Mat mGray;

    private CascadeClassifier mJavaDetector;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private int mLikely = 999;

    String mPath = "";

    private Tutorial3View mOpenCvCameraView;
    private int mChooseCamera = backCam;


    private ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;

    PersonRecognizer fr;
    ToggleButton toggleButtonGrabar;
    //    Button addUser;
    Button popUp, endCall;
    ImageView ivGreen, ivYellow, ivRed;
    String listenState = "";
    ImageView listenIndicator;
    TextView bubbleName, aiResponseText;


    boolean callFromApp = false; // To control the call has been made from the application
    boolean callFromOffHook = false; // To control the change to idle state is from the app call

    private static String userName = "", userPassword = "", userEmail = "";

    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


    static final long MAXIMG = 10;

    ArrayList<Mat> alimgs = new ArrayList<Mat>();

    int[] labels = new int[(int) MAXIMG];
    int countImages = 0;

    labels labelsFile;

    private TextView textVoice, textResultVoice;
    CommandExecution commandExecuter;


    private Controller controller;

    private BaseLoaderCallback mLoaderCallback;

    // facebook
    private CallbackManager callbackManager;
    private LoginResult facebookLoginResult;

    public FdActivity() {
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

//        ContentResolver cr = getContentResolver();
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
//                null, null, null, null);
//        if (cur.getCount() > 0) {
//            while (cur.moveToNext()) {
//                String id = cur.getString(
//                        cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(
//                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                    //Query phone here.  Covered next
//                }
//            }
//        }

        Log.d("Loading", "OnCreate");


        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);


        setContentView(R.layout.face_detect_surface_view);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        mLoaderCallback = new BaseLoaderCallback(this) {
//            @Override
//            public void onManagerConnected(int status) {
//                switch (status) {
//                    case LoaderCallbackInterface.SUCCESS: {
//
//                        Log.d("Loading", "enter");
////                        fr = new PersonRecognizer(mPath);
//                          fr  = StartUpActivity.fr;
//
//                        String s = getResources().getString(R.string.Straininig);
//                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
////                        fr.load();
//
//                        try {
//                            // load cascade file from application resources
//                            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
//                            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
//                            File mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
//                            FileOutputStream os = new FileOutputStream(mCascadeFile);
//
//                            byte[] buffer = new byte[4096];
//                            int bytesRead;
//                            while ((bytesRead = is.read(buffer)) != -1) {
//                                os.write(buffer, 0, bytesRead);
//                            }
//                            is.close();
//                            os.close();
//
//                            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
//                            if (mJavaDetector.empty()) {
//                                Log.e(TAG, "Failed to load cascade classifier");
//                                mJavaDetector = null;
//                            } else
//                                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
//
//                            //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
//
//                            cascadeDir.delete();
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
//                        }
//
//                        mOpenCvCameraView.enableView();
//                        Log.d("Loading", "exit");
//                    }
//                    break;
//                    default: {
//                        super.onManagerConnected(status);
//                    }
//                    break;
//                }
//            }
//        };

        new LoadOpenCV().execute();


        // facebook
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        callbackManager = CallbackManager.Factory.create();
//        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//            facebookLoginResult = loginResult;
//            }
//
//            @Override
//            public void onCancel() {
//            Toast.makeText(context, "Couldn't log into facebook!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//            Toast.makeText(context, "Couldn't log into facebook!", Toast.LENGTH_SHORT).show();
//            }
//            });
//
//        // generate the KeyHash
//        // Add code to print out the key hash
////        try {
////            PackageInfo info = getPackageManager().getPackageInfo(
////                    "org.opencv.javacv.facerecognition",
////                    PackageManager.GET_SIGNATURES);
////            for (Signature signature : info.signatures) {
////                MessageDigest md = MessageDigest.getInstance("SHA");
////                md.update(signature.toByteArray());
////                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
////            }
////        } catch (PackageManager.NameNotFoundException e) {
////
////        } catch (NoSuchAlgorithmException e) {
////
////        }
//
//         log into facebook.com
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));


//        final ToggleButton bluetoothOnOff = (ToggleButton) findViewById(R.id.toggleButton2);
//
//        bluetoothOnOff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (bluetoothOnOff.isChecked()) {
//
////                    Intent trainingIntent = new Intent(org.opencv.javacv.facerecognition.FdActivity.this, org.opencv.javacv.facerecognition.TrainingActivity.class);
////                    startActivity(trainingIntent);
//
//                    //  controller.connectToSAR();
//
//
//                } else {
//                    //    controller.disconnectToSAR();
//
//
//                }
//            }
//        });


        textVoice =         (TextView)  findViewById(R.id.text);
        textResultVoice =   (TextView)  findViewById(R.id.result);
        listenIndicator =   (ImageView) findViewById(R.id.listen_indicator);
        bubbleName =        (TextView)  findViewById(R.id.bubble_name);
        aiResponseText =    (TextView)  findViewById(R.id.ai_response);

        final TextToSpeechHelper textToSpeechHelper = new TextToSpeechHelper(getApplicationContext());
        commandExecuter = new CommandExecution(textToSpeechHelper, FdActivity.this);


        context = getApplicationContext(); // Needs to be set
//        VoiceRecognitionListener.getInstance().setListener(this); // Here we set the current listener
        Log.d("State", "Ready to listen");
        listenState = "Ready to listen";




        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mPath = getFilesDir() + "/facerecogOCV/";

        labelsFile = new labels(mPath);
        controller = new Controller();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj == "IMG") {
                } else {
                    ivGreen.setVisibility(View.INVISIBLE);
                    ivYellow.setVisibility(View.INVISIBLE);
                    ivRed.setVisibility(View.INVISIBLE);
                    if (mLikely < 0) ;
                    else if (mLikely < 50) {
                        ivGreen.setVisibility(View.VISIBLE);
                    } else if (mLikely < 80) {
                        ivYellow.setVisibility(View.VISIBLE);
                    } else {
                        ivRed.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
        ivGreen = (ImageView) findViewById(R.id.imageView3);
        ivYellow = (ImageView) findViewById(R.id.imageView4);
        ivRed = (ImageView) findViewById(R.id.imageView2);

        ivGreen.setVisibility(View.INVISIBLE);
        ivYellow.setVisibility(View.INVISIBLE);
        ivRed.setVisibility(View.INVISIBLE);


        boolean success = (new File(mPath)).mkdirs();
        if (!success) {
            Log.e("Error", "Error creating directory");
        }


//        startListening(); // starts listening

    }


    @Override
    public void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this); // facebook tracker
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void checkResult(Result result) {
        commandExecuter.setResult(result, this);
        commandExecuter.executeCommand();
    }

    @Override
    public void onResume() {
        super.onResume();
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        AppEventsLogger.activateApp(this); // facebook tracker
    }

    public void onDestroy() {
        super.onDestroy();
//        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
//        mGray.release();
//        mRgba.release();
    }

    MatOfRect faces;

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        commandExecuter.setCurrentFrame(inputFrame);

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        faces = new MatOfRect();

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


        Core.flip(mRgba, mRgba, 1);

        if ((facesArray.length > 0) && (faceState == SEARCHING) && fr.canPredict()) {

//            for (int i = 0; i < facesArray.length; i++) {

            Mat m = new Mat();
            m = mGray.submat(facesArray[0]);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            String textTochange = "IMG";


            textTochange = fr.predict(m);
            mLikely = fr.getProb();

            msg = new Message();
            msg.obj = textTochange;
            final String textTochangeTemp = textTochange;

            mHandler.sendMessage(msg);
            final Point pl = facesArray[0].tl();
            final Point pr = facesArray[0].br();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bubbleName.setVisibility(View.VISIBLE);
                    bubbleName.setX((mRgba.width() - (int) pl.x) - (int) (pr.x - pl.x));
                    bubbleName.setY((int) pl.y);
                    bubbleName.setText(" " + textTochangeTemp);
                }
            });

//            Core.rectangle(mRgba, new Point(mRgba.width() - facesArray[0].br().x, facesArray[0].tl().y), new Point(mRgba.width() - facesArray[0].tl().x, facesArray[0].br().y), FACE_RECT_COLOR, 3);
//                    if(mLikely < 70)
//            Core.putText(mRgba, textTochange, new Point(mRgba.width() - facesArray[0].br().x, facesArray[0].tl().y), 3, 4, new Scalar(255, 0, 0, 255));
//            }


            DatabaseHelper db = new DatabaseHelper(getApplicationContext());

            try {
                db.open();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String testMail = db.getEmail(textTochange);
            String testPassword = db.getPassword(textTochange);

            if (!testMail.equals("not found")) {
                userEmail = testMail;
            }
            if (!testPassword.equals("not found")) {
                userPassword = testPassword;
            }

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bubbleName.setVisibility(View.INVISIBLE);
                }
            });
        }

//        for (int i = 0; i < facesArray.length; i++){
//            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//            Core.putText();
//        }

        if (facesArray.length > 0) {
            moveSAR(facesArray[0]);
        }
//        Log.i("detected faces", facesArray.length + "");

//        Core.flip(mRgba.t(), mRgba.t(), -1);

        //  if(facesArray.length>1){
        //     controller.goUp();
        // }else if(facesArray.length == 1){
        //    controller.goRight();
        // }

//        Core.putText(mRgba, listenState, new Point(30, 90), 3, 4, new Scalar(255, 0, 0, 255));
        return mRgba;
    }


    public static String getUserPassword() {
        return userPassword;
    }

    public static void setUserPassword(String userPassword) {
        FdActivity.userPassword = userPassword;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        FdActivity.userEmail = userEmail;
    }

    private void moveSAR(Rect rect) {
            Point topLeft = rect.tl();
            Point bottomRight = rect.br();
            Log.e("Top Leftttttttttttttt", "" + topLeft.toString());
            Log.e("Bottom Rightttttttttt", "" + bottomRight.toString());

            Point center = new Point((topLeft.x + bottomRight.x) / 2, (topLeft.y + bottomRight.y) / 2);

            controller.move((int) center.x, (int) center.y);

//        Tutorial3View opencvDis = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
//
//        int opencvDisHeight = opencvDis.getHeight();
//        int opencvDisWidth = opencvDis.getWidth();
//
//        Log.e("Screen Widthh", "" + opencvDisWidth + "");
//        Log.e("Screen Heighttt", "" + opencvDisHeight + "");
//
//        int horizontalBlock = checkHorizontalGrid(center.x, opencvDisWidth);
//
//        Log.e("Horizontal blockkkk", horizontalBlock + "");
//
//        int verticalBlock = checkVerticalGrid(center.y, opencvDisHeight);
//
//        Log.e("Vertical blockkk", verticalBlock + "");
//
//        if (horizontalBlock == 2 && verticalBlock == 1) {
//            return; // almost in center
//        }
//
//        // horizontal
//        switch (horizontalBlock) {
//            case 0: // far left
//                Log.e("Go", "Lefttttttttttttttttttttttttttttttttttttttt");
//                controller.goLeft(10);
//                break;
//            case 1: // left
//                Log.e("Go", "Leftttttttttttttttttttttttttttttttttttttt");
//                controller.goLeft(5);
//                break;
//            case 3: // rgiht
//                Log.e("Go", "Rightttttttttttttttttttttttttttttttttttttt");
//                controller.goRight(5);
//                break;
//            case 4: // far right
//                Log.e("Go", "Rightttttttttttttttttttttttttttttttttttttt");
//                controller.goRight(10);
//                break;
//        }
//
//        switch (verticalBlock) {
//            case 0:
//                controller.goUp(5);
//                break;
//            case 2:
//                controller.goDown(5);
//                break;
//        }
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
        int tempGrid =  (int) x / (opencvDisWidth / 20);

        if(tempGrid < 4){
            return 0;
        }else if(tempGrid < 8){
            return 1;
        }else if(tempGrid < 12){
            return 2;
        }
        else if(tempGrid < 16){
            return 3;
        }else{
            return 4;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
//        if (mOpenCvCameraView.numberCameras() > 1) {
//            nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
//            mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));
//
//    }
        connectSAR = menu.add("Connect");
        addUserItem = menu.add("Add User");
        addUserItem.setCheckable(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
//        nBackCam.setChecked(false);
//        mFrontCam.setChecked(false);
//        if (item == nBackCam) {
//            mOpenCvCameraView.setCamFront();
//            mChooseCamera = frontCam;
//        }
//        //fr.changeRecognizer(0);
//        else if (item == mFrontCam) {
//            mChooseCamera = backCam;
//            mOpenCvCameraView.setCamBack();
//
//        }else
        if (item == addUserItem) {
            Intent intent = new Intent(getApplicationContext(), org.opencv.javacv.facerecognition.signup_activity.class);
            startActivity(intent);
            finish();
        } else if (item == connectSAR) {
            if (!connectSAR.isChecked()) {

//                    Intent trainingIntent = new Intent(org.opencv.javacv.facerecognition.FdActivity.this, org.opencv.javacv.facerecognition.TrainingActivity.class);
//                    startActivity(trainingIntent);

                controller.connectToSAR(mOpenCvCameraView.getWidth(), mOpenCvCameraView.getHeight());
                Log.d("SAR", "Connect");
                connectSAR.setChecked(true);
            } else {
                    controller.disconnectToSAR();
                Log.d("SAR", "Disconnect");
                connectSAR.setChecked(false);
            }
        }

        item.setChecked(true);

        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }


    @Override
    public void processVoiceCommands(String... voiceCommands) {
        Log.d("State", "Processing");
        listenState = "Processing";

        listenIndicator.setImageResource(R.drawable.circle_red);
        final AIConfiguration config = new AIConfiguration("a7ee7ac49bac4559b295d1c38a18812f",
                "9a44c559-6daa-45b7-adc4-375c71de82d7", AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        final AIDataService aiDataService = new AIDataService(context, config);

        final AIRequest aiRequest = new AIRequest();


        aiRequest.setQuery(voiceCommands[0]);
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(final AIResponse aiResponse) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (aiResponse != null) {
                            // process aiResponse here
                            final Result result = aiResponse.getResult();

                            // Get parameters
                            String parameterString = "";
                            if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                                for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                                    parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                                }
                            }

                            final String finalParameterString = parameterString;

                            Log.d("MYPARA", result.getFulfillment().getSpeech());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textResultVoice.setText("Query:" + result.getResolvedQuery() +
                                            "\nAction: " + result.getAction() +
                                            "\nParameters: " + finalParameterString);
                                    try {
                                        aiResponseText.setText(result.getFulfillment().getSpeech().substring(0, 50));
                                    } catch (Exception e) {
                                        aiResponseText.setText(result.getFulfillment().getSpeech());
                                    }
                                }
                            });
                            checkResult(result);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                restartListeningService();
                                listenIndicator.setImageResource(R.drawable.circle_green);
                                Log.d("State", "Ready to listen");
                                listenState = "Ready to listen";
                            }
                        });
                    }
                }).start();
            }

        }.execute(aiRequest);

        if (voiceCommands[0].matches(".*end.*")) {
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            Class clazz = null;
            try {
                clazz = Class.forName(telephonyManager.getClass().getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Method method = null;
            try {
                method = clazz.getDeclaredMethod("getITelephony");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            method.setAccessible(true);
            ITelephony telephonyService = null;
            try {
                telephonyService = (ITelephony) method.invoke(telephonyManager);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            telephonyService.endCall();
        }

    }


    private void showPopup(Activity context, Point p, String name, String phone) {
        int popupWidth = 600;
        int popupHeight = 480;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup);

        Log.d("Layout", (layout == null) + "");

        // Creating the PopupWindow
        PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        int OFFSET_X = 30;
        int OFFSET_Y = 30;

        TextView nameText = (TextView) layout.findViewById(R.id.sender_name);
        TextView phoneText = (TextView) layout.findViewById(R.id.sender_phone);

        nameText.setTextColor(Color.WHITE);
        phoneText.setTextColor(Color.WHITE);

        nameText.setText(name);
        phoneText.setText(phone);


        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, (int) (p.x + OFFSET_X), (int) (p.y + OFFSET_Y));
    }

//    public class StatePhoneReceiver extends PhoneStateListener {
//        Context context;
//
//        public StatePhoneReceiver(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        public void onCallStateChanged(int state, String incomingNumber) {
//            super.onCallStateChanged(state, incomingNumber);
//
//            switch (state) {
//
//                case TelephonyManager.CALL_STATE_OFFHOOK: //Call is established
//                    if (callFromApp) {
//                        callFromApp = false;
//                        callFromOffHook = true;
//
//                        try {
//                            Thread.sleep(500); // Delay 0,5 seconds to handle better turning on loudspeaker
//                        } catch (InterruptedException e) {
//                        }
//
//                        //Activate loudspeaker
//                        AudioManager audioManager = (AudioManager)
//                                getSystemService(Context.AUDIO_SERVICE);
//                        audioManager.setMode(AudioManager.MODE_IN_CALL);
//                        audioManager.setSpeakerphoneOn(true);
//                    }
//                    break;
//
//                case TelephonyManager.CALL_STATE_IDLE: //Call is finished
//                    if (callFromOffHook) {
//                        callFromOffHook = false;
//                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                        audioManager.setMode(AudioManager.MODE_NORMAL); //Deactivate loudspeaker
//                        manager.listen(myPhoneStateListener, // Remove listener
//                                PhoneStateListener.LISTEN_NONE);
//                    }
//                    break;
//            }
//        }
//    }
private class LoadOpenCV extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // before making http calls

    }

    @Override
    protected Void doInBackground(Void... arg0) {

        VoiceRecognitionListener.getInstance().setListener(FdActivity.this); // Here we set the current listener
        mLoaderCallback = new BaseLoaderCallback(FdActivity.this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {

                        Log.d("Loading", "enter");
//                        fr = new PersonRecognizer(mPath);
                        fr  = StartUpActivity.fr;

                        String s = getResources().getString(R.string.Straininig);
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
//                        fr.load();

                        try {
                            // load cascade file from application resources
                            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            File mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
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

                        mOpenCvCameraView.enableView();
                        Log.d("Loading", "exit");
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, FdActivity.this, mLoaderCallback);
        startListening(); // starts listening
    }

}

}