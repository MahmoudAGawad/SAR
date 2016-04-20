package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

//import java.io.FileNotFoundException;
//import org.opencv.contrib.FaceRecognizer;

import com.facebook.FacebookSdk;




public class FdActivity extends ListeningActivity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int TRAINING= 0;
    public static final int SEARCHING= 1;
    public static final int IDLE= 2;

    private static final int frontCam =1;
    private static final int backCam =2;


    private int faceState=IDLE;
//    private int countTrain=0;

//    private MenuItem               mItemFace50;
//    private MenuItem               mItemFace40;
//    private MenuItem               mItemFace30;
//    private MenuItem               mItemFace20;
//    private MenuItem               mItemType;
//    
    private MenuItem               nBackCam;
    private MenuItem               mFrontCam;
    private MenuItem               mEigen;


    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
 //   private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely=999;

    String mPath="";

    private Tutorial3View   mOpenCvCameraView;
    private int mChooseCamera = backCam;

    EditText text;
    TextView textresult;
    private  ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;

    PersonRecognizer fr;
    ToggleButton toggleButtonGrabar,toggleButtonTrain,buttonSearch;
    Button buttonCatalog;
    ImageView ivGreen,ivYellow,ivRed;
    ImageButton imCamera;

    TextView textState;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


    static final long MAXIMG = 10;

    ArrayList<Mat> alimgs = new ArrayList<Mat>();

    int[] labels = new int[(int)MAXIMG];
    int countImages=0;

    labels labelsFile;

    private TextView textVoice , textResultVoice;
    CommandExecution commandExecuter;

    // facebook
    private CallbackManager callbackManager;
    private LoginResult facebookLoginResult;

    private Controller controller;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                 //   System.loadLibrary("detection_based_tracker");



                    fr=new PersonRecognizer(mPath);

                 //   fr.changeRecognizer(1);

                    String s = getResources().getString(R.string.Straininig);
                    Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
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

                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookLoginResult = loginResult;
            }

            @Override
            public void onCancel() {
                Toast.makeText(context, "Couldn't log into facebook!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(context, "Couldn't log into facebook!", Toast.LENGTH_SHORT).show();
            }
        });

        // generate the KeyHash
        // Add code to print out the key hash
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "org.opencv.javacv.facerecognition",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

        // log into facebook.com
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));



        setContentView(R.layout.face_detect_surface_view);

        textVoice = (TextView)findViewById(R.id.text);
        textResultVoice = (TextView) findViewById(R.id.result);
        ////
        final TextToSpeechHelper textToSpeechHelper = new TextToSpeechHelper(getApplicationContext());
        commandExecuter = new CommandExecution(textToSpeechHelper , getApplicationContext());

        context = getApplicationContext(); // Needs to be set
        VoiceRecognitionListener.getInstance().setListener(this); // Here we set the current listener
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                startListening(); // starts listening
//            }
//        }).start();


        ///////////////////////
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setCvCameraViewListener(this);

        Log.i("widoooooooooooo","ttttttttttt");


        mPath=getFilesDir()+"/facerecogOCV/";

        labelsFile= new labels(mPath);

        Iv=(ImageView)findViewById(R.id.imageView1);
        textresult = (TextView) findViewById(R.id.textView1);

        // Gawaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaad
        controller = new Controller();
        final ToggleButton bluetoothOnOff = (ToggleButton) findViewById(R.id.toggleButton2);

        bluetoothOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothOnOff.isChecked()){
                    controller.connectToSAR();


                }else{
                    controller.disconnectToSAR();
                }
            }
        });




        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	if (msg.obj=="IMG")
            	{
            	 Canvas canvas = new Canvas();
                 canvas.setBitmap(mBitmap);
                 Iv.setImageBitmap(mBitmap);
                 if (countImages>=MAXIMG-1)
                 {
                	 toggleButtonGrabar.setChecked(false);
                 	 grabarOnclick();
                 }
            	}
            	else
            	{
            		textresult.setText(msg.obj.toString());

            		 ivGreen.setVisibility(View.INVISIBLE);
            	     ivYellow.setVisibility(View.INVISIBLE);
            	     ivRed.setVisibility(View.INVISIBLE);

            	     if (mLikely<0);

            	     else if (mLikely<50) {
                         ivGreen.setVisibility(View.VISIBLE);
                         textresult.setText("HELLO " + msg.obj.toString());

//                         if(msg.obj.toString().equals("mostafa")){
//                             controller.goUp();
//                         }else if (msg.obj.toString().equals("diaa")){
//                             controller.goRight();
//                         }
//                         else if (msg.obj.toString().equals("waleed")){
//                             controller.goLeft();
//                         }
//                         else if (msg.obj.toString().equals("gawad")){
//                             controller.goDown();
//                         }
//
//                         Intent i = new Intent(org.opencv.javacv.facerecognition.FdActivity.this,
//                                 testingairesponse.MainActivity.class);
//                         finish();
//                         startActivity(i);

//                         textToSpeechHelper.speak("HELLO " + msg.obj.toString());
//                         controller.goLeft();

                     }
            		else if (mLikely<80) {
                         ivYellow.setVisibility(View.VISIBLE);
                         textresult.setText("HELLO " + msg.obj.toString());
//                         if(msg.obj.toString().equals("mostafa")){
//                            controller.goUp();
//                         }else if (msg.obj.toString().equals("diaa")){
//                             controller.goRight();
//                         }
//                         else if (msg.obj.toString().equals("waleed")){
//                             controller.goLeft();
//                         }
//                         else if (msg.obj.toString().equals("gawad")){
//                             controller.goDown();
//                         }
//                         Intent i = new Intent(org.opencv.javacv.facerecognition.FdActivity.this,
//                                 testingairesponse.MainActivity.class);
//                         finish();
//                         startActivity(i);
//                         textToSpeechHelper.speak("HELLO " + msg.obj.toString());
//                         controller.goRight();
                     }
            		else {
                         ivRed.setVisibility(View.VISIBLE);
                     }
            	}
            }
        };

        text=(EditText)findViewById(R.id.editText1);
        buttonCatalog=(Button)findViewById(R.id.buttonCat);
        toggleButtonGrabar=(ToggleButton)findViewById(R.id.toggleButtonGrabar);
        buttonSearch=(ToggleButton)findViewById(R.id.buttonBuscar);
        toggleButtonTrain=(ToggleButton)findViewById(R.id.toggleButton1);
        textState= (TextView)findViewById(R.id.textViewState);
        ivGreen=(ImageView)findViewById(R.id.imageView3);
        ivYellow=(ImageView)findViewById(R.id.imageView4);
        ivRed=(ImageView)findViewById(R.id.imageView2);
        imCamera=(ImageButton)findViewById(R.id.imageButton1);

        ivGreen.setVisibility(View.INVISIBLE);
        ivYellow.setVisibility(View.INVISIBLE);
        ivRed.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        textresult.setVisibility(View.INVISIBLE);



        toggleButtonGrabar.setVisibility(View.INVISIBLE);

        buttonCatalog.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(org.opencv.javacv.facerecognition.FdActivity.this,
        				org.opencv.javacv.facerecognition.ImageGallery.class);
        		i.putExtra("path", mPath);
        		startActivity(i);
        	};
        	});


        text.setOnKeyListener(new View.OnKeyListener() {
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
        		if ((text.getText().toString().length()>0)&&(toggleButtonTrain.isChecked()))
        			toggleButtonGrabar.setVisibility(View.VISIBLE);
        		else
        			toggleButtonGrabar.setVisibility(View.INVISIBLE);

                return false;
        	}
        });



		toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (toggleButtonTrain.isChecked()) {
					textState.setText(getResources().getString(R.string.SEnter));
					buttonSearch.setVisibility(View.INVISIBLE);
					textresult.setVisibility(View.VISIBLE);
					text.setVisibility(View.VISIBLE);
					textresult.setText(getResources().getString(R.string.SFaceName));
					if (text.getText().toString().length() > 0)
						toggleButtonGrabar.setVisibility(View.VISIBLE);


					ivGreen.setVisibility(View.INVISIBLE);
					ivYellow.setVisibility(View.INVISIBLE);
					ivRed.setVisibility(View.INVISIBLE);


				} else {
					textState.setText(R.string.Straininig);
					textresult.setText("");
					text.setVisibility(View.INVISIBLE);

					buttonSearch.setVisibility(View.VISIBLE);
					;
					textresult.setText("");
					{
						toggleButtonGrabar.setVisibility(View.INVISIBLE);
						text.setVisibility(View.INVISIBLE);
					}
			        Toast.makeText(getApplicationContext(),getResources().getString(R.string.Straininig), Toast.LENGTH_LONG).show();
					fr.train();
					textState.setText(getResources().getString(R.string.SIdle));

				}
			}

		});



        toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				grabarOnclick();
			}
		});

        imCamera.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (mChooseCamera==frontCam)
				{
					mChooseCamera=backCam;
					mOpenCvCameraView.setCamBack();
				}
				else
				{
					mChooseCamera=frontCam;
					mOpenCvCameraView.setCamFront();

				}
			}
		});

        buttonSearch.setOnClickListener(new View.OnClickListener() {

     			public void onClick(View v) {
     				if (buttonSearch.isChecked())
     				{
     					if (!fr.canPredict())
     						{
     						buttonSearch.setChecked(false);
     			            Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
     			            return;
     						}
     					textState.setText(getResources().getString(R.string.SSearching));
     					toggleButtonGrabar.setVisibility(View.INVISIBLE);
     					toggleButtonTrain.setVisibility(View.INVISIBLE);
     					text.setVisibility(View.INVISIBLE);
     					faceState=SEARCHING;
     					textresult.setVisibility(View.VISIBLE);
     				}
     				else
     				{
     					faceState=IDLE;
     					textState.setText(getResources().getString(R.string.SIdle));
     					toggleButtonGrabar.setVisibility(View.INVISIBLE);
     					toggleButtonTrain.setVisibility(View.VISIBLE);
     					text.setVisibility(View.INVISIBLE);
     					textresult.setVisibility(View.INVISIBLE);

     				}
     			}
     		});

        boolean success=(new File(mPath)).mkdirs();
        if (!success)
        {
        	Log.e("Error","Error creating directory");
        }
    }

    void grabarOnclick()
    {
    	if (toggleButtonGrabar.isChecked())
			faceState=TRAINING;
			else
			{ if (faceState==TRAINING)	;
			 // train();
			  //fr.train();
			  countImages=0;
			  faceState=IDLE;
			}


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        AppEventsLogger.deactivateApp(this); // facebook tracker
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void checkResult(Result result) {
        commandExecuter.setResult(result, this);
        commandExecuter.executeCommand();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

        AppEventsLogger.activateApp(this); // facebook tracker
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
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
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length==1)&&(faceState==TRAINING)&&(countImages<MAXIMG)&&( (text.getText().toString())!=""   ))
        {


        Mat m=new Mat();
        Rect r=facesArray[0];


        m=mRgba.submat(r);
        mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);


        Utils.matToBitmap(m, mBitmap);
       // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");

        Message msg = new Message();
        String textTochange = "IMG";
        msg.obj = textTochange;
        mHandler.sendMessage(msg);
        if (countImages<MAXIMG)
        {
        	fr.add(m, text.getText().toString());
        	countImages++;
        }

        }
        else
        	 if ((facesArray.length>0)&& (faceState==SEARCHING))
          {
        	  Mat m=new Mat();
        	  m=mGray.submat(facesArray[0]);
        	  mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);


              Utils.matToBitmap(m, mBitmap);
              Message msg = new Message();
              String textTochange = "IMG";
              msg.obj = textTochange;
              mHandler.sendMessage(msg);

              textTochange=fr.predict(m);
              mLikely=fr.getProb();
        	  msg = new Message();
        	  msg.obj = textTochange;
        	  mHandler.sendMessage(msg);

          }
        for (int i = 0; i < facesArray.length; i++){
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }

        if(facesArray.length>0){
            moveSAR(facesArray[0]);
        }
        Log.i("detected faces", facesArray.length + "");

//        Core.flip(mRgba.t(), mRgba.t(), -1);

      //  if(facesArray.length>1){
       //     controller.goUp();
       // }else if(facesArray.length == 1){
        //    controller.goRight();
        // }


        if(mChooseCamera != backCam){
            Core.flip(mRgba, mRgba, 1);
        }
        return mRgba;
    }

    private void moveSAR(Rect rect){
        Point topLeft = rect.tl();
        Point bottomRight = rect.br();
        Log.e("Top Leftttttttttttttt", "" + topLeft.toString());
        Log.e("Bottom Rightttttttttt", "" + bottomRight.toString());

        Point center = new Point((topLeft.x+bottomRight.x)/2, (topLeft.y+bottomRight.y)/2);

        Tutorial3View opencvDis=(Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        int opencvDisHeight =  opencvDis.getHeight();
        int opencvDisWidth = opencvDis.getWidth();

        Log.e("Screen Widthhhhhhhhhhhhhhhh", "" + opencvDisWidth+"");
        Log.e("Screen Heighttttttttttttttt", "" + opencvDisHeight+"");

        int horizontalBlock=checkHorizontalGrid(center.x, opencvDisWidth);

        Log.e("Horizontal blockkkkkkkkkkkkkkkkk",horizontalBlock+"");

        int verticalBlock=checkVerticalGrid(center.y, opencvDisHeight);

        Log.e("Vertical blockkkkkkkkkkkkkkkkk",verticalBlock+"");

        if(horizontalBlock == 2 && verticalBlock == 1){
            return; // almost in center
        }

        // horizontal
        switch (horizontalBlock){
            case 0: // far left
                Log.e("Go","Lefttttttttttttttttttttttttttttttttttttttt");
                controller.goLeft(10);
                break;
            case 1: // left
                Log.e("Go","Leftttttttttttttttttttttttttttttttttttttt");
                controller.goLeft(5);
                break;
            case 3: // rgiht
                Log.e("Go","Rightttttttttttttttttttttttttttttttttttttt");
                controller.goRight(5);
                break;
            case 4: // far right
                Log.e("Go","Rightttttttttttttttttttttttttttttttttttttt");
                controller.goRight(10);
                break;
        }

        switch (verticalBlock){
            case 0:
                Log.e("Go", "Uppppppppppppppppppppppppppppppppp");
                controller.goUp(5);
                break;
            case 2:
                Log.e("Go", "Downnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
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

         int tempGrid = (int)y/(opencvDisHeight/10);

        if(tempGrid<=2){return 0;}
        if(tempGrid>=7){return 2;}
        else{
            return 1;
        }
    }

    private int checkHorizontalGrid(double x, int opencvDisWidth) {


        return (int)x/(opencvDisWidth/5);





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        if (mOpenCvCameraView.numberCameras()>1)
        {
        nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
        mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));

//        mEigen = menu.add("EigenFaces");
//        mLBPH.setChecked(true);
        }
        else
        {imCamera.setVisibility(View.INVISIBLE);

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
        if (item == nBackCam)
        {
        	mOpenCvCameraView.setCamFront();
        	mChooseCamera=frontCam;
        }
        	//fr.changeRecognizer(0);
        else if (item==mFrontCam)
        {
        	mChooseCamera=backCam;
        	mOpenCvCameraView.setCamBack();

        }

        item.setChecked(true);

        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
//        if (mDetectorType != type) {
//            mDetectorType = type;
//
//            if (type == NATIVE_DETECTOR) {
//                Log.i(TAG, "Detection Based Tracker enabled");
//                mNativeDetector.start();
//            } else {
//                Log.i(TAG, "Cascade detector enabled");
//                mNativeDetector.stop();
//            }
//        }
   }




    @Override
    public void processVoiceCommands(String... voiceCommands) {
        //        content.removeAllViews();
//        for (String command : voiceCommands) {
//            TextView txt = new TextView(getApplicationContext());
//            txt.setText(command);
//            txt.setTextSize(20);
//            txt.setTextColor(Color.BLACK);
//            txt.setGravity(Gravity.CENTER);
//            content.addView(txt);
//        }
//        restartListeningService();




        text.setText(voiceCommands[0]);


        final AIConfiguration config = new AIConfiguration("a7ee7ac49bac4559b295d1c38a18812f",
                "9a44c559-6daa-45b7-adc4-375c71de82d7", AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);


//        final AIConfiguration config = new AIConfiguration("a7ee7ac49bac4559b295d1c38a18812f",
//                "9a44c559-6daa-45b7-adc4-375c71de82d7",
//                AIConfiguration.SupportedLanguages.English,
//                AIConfiguration.RecognitionEngine.System);
        final AIDataService aiDataService = new AIDataService(context , config);

        final AIRequest aiRequest = new AIRequest();
//


//
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

                            Log.e("Testing here :", "widooooooooooooooooo");

                            final String finalParameterString = parameterString;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textResultVoice.setText("helooooooooooo");
                                    // Show results in TextView.
                                    textResultVoice.setText("Query:" + result.getResolvedQuery() +
                                            "\nAction: " + result.getAction() +
                                            "\nParameters: " + finalParameterString);
                                }
                            });
                            checkResult(result);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                restartListeningService();
                            }
                        });
                    }
                }).start();


            }

        }.execute(aiRequest);

    }


}
