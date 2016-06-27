package org.opencv.javacv.facerecognition;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import com.googlecode.javacv.cpp.opencv_imgproc;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

public  class PersonRecognizer {
	
	public final static int MAXIMG = 100;
	FaceRecognizer faceRecognizer;
	String mPath;
	int count=0;
	labels labelsFile;

	 static  final int WIDTH= 128;
	 static  final int HEIGHT= 128;;
	 private int mProb=999;
	HashSet<String> set = new HashSet<>();
	 
    PersonRecognizer(String path)
    {
      faceRecognizer =  com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(2,8,8,8,200);
//		faceRecognizer =  com.googlecode.javacv.cpp.opencv_contrib.createFisherFaceRecognizer();
  	 // path=Environment.getExternalStorageDirectory()+"/facerecog/faces/";
     mPath=path;
     labelsFile= new labels(mPath);
     
  
    }
    
//    void changeRecognizer(int nRec)
//    {
//    	switch(nRec) {
//    	case 0: faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(1,8,8,8,100);
//    			break;
//    	case 1: faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createFisherFaceRecognizer();
//    			break;
//    	case 2: faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createEigenFaceRecognizer();
//    			break;
//    	}
//    	train();
//
//    }
    
	void add(Mat m, String description) {
		Bitmap bmp= Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
		 
		Utils.matToBitmap(m,bmp);
		bmp= Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);
		
		FileOutputStream f;
		try {
			f = new FileOutputStream(mPath+description+"-"+count+".jpg",true);
			count++;
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);
			f.close();

		} catch (Exception e) {
			Log.e("error",e.getCause()+" "+e.getMessage());
			e.printStackTrace();
			
		}
	}
	
	public boolean train(final RelativeLayout linearLayout,final Context context) {
	 	
		File root = new File(mPath);

        FilenameFilter pngFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            
        };
        };

        File[] imageFiles = root.listFiles(pngFilter);

        MatVector images = new MatVector(imageFiles.length);

        int[] labels = new int[imageFiles.length];

        int counter = 0;
        int label;

        IplImage img=null;
        IplImage grayImg;

        int i1=mPath.length();
       
   
        for (File image : imageFiles) {
        	String p = image.getAbsolutePath();
            img = cvLoadImage(p);
            
            if (img==null)
            	Log.e("Error","Error cVLoadImage");
            Log.i("image",p);
            
            int i2=p.lastIndexOf("-");
            int i3=p.lastIndexOf(".");
            int icount=Integer.parseInt(p.substring(i2+1,i3)); 
            if (count<icount) count++;
            
            String description=p.substring(i1,i2);
            
            if (labelsFile.get(description)<0)
            	labelsFile.add(description, labelsFile.max()+1);
            
            label = labelsFile.get(description);

            grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);

            cvCvtColor(img, grayImg, CV_BGR2GRAY);

            images.put(counter, grayImg);

            labels[counter] = label;

            counter++;
			android.os.Handler mUiHandler = new android.os.Handler(Looper.getMainLooper());
			final Bitmap bitImg = IplImageToBitmap(img);
			final String p1 = p.split("/")[6].split("-")[0];
			final ArrayList<ImageItem> imageItems = new ArrayList<>();
			mUiHandler.post(new Runnable() {
				@Override
				public void run() {
//					LinearLayout nameLayout = (LinearLayout) linearLayout.findViewById(R.id.trained_names);
//					ImageView imageView = new ImageView(context);
//					TextView textView = new TextView(context);
					if (!set.contains(p1)) {
						set.add(p1);
//						imageItems.add(new ImageItem(bitImg,p1));
						StartUpActivity.gridAdapter.add(new ImageItem(bitImg,p1));
						StartUpActivity.gridView.setAdapter(StartUpActivity.gridAdapter);
//						StartUpActivity.gridView.notifyAll();
						StartUpActivity.gridAdapter.notifyDataSetChanged();
//						notifyDataSetChanged
//						textView.setText("  "+p1);
//						textView.setTextColor(Color.GREEN);
//						textView.setTextSize(15);
//						imageView.setImageBitmap(bitImg);
//						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//						params.gravity = Gravity.CENTER;
//						imageView.setLayoutParams(params);
//						textView.setLayoutParams(params);
//						LinearLayout linearLayout1 = new LinearLayout(context);
//						linearLayout1.setLayoutParams(params);
//						linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
//						linearLayout1.addView(imageView);
//						linearLayout1.addView(textView);
//						nameLayout.addView(linearLayout1);
					}
				}
			});

        }
        if (counter>0)
        	if (labelsFile.max()>1)
        		faceRecognizer.train(images, labels);
        labelsFile.Save();
	return true;
	}


	public static Bitmap IplImageToBitmap(IplImage src) {
		Bitmap bm=null;
		int width = src.width();
		int height = src.height();
		// Unfortunately cvCvtColor will not let us convert in place, so we need to create a new IplImage with matching dimensions.
		IplImage frame2 = IplImage.create(width, height, opencv_core.IPL_DEPTH_8U, 4);
		opencv_imgproc.cvCvtColor(src, frame2, opencv_imgproc.CV_BGR2RGBA);
		// Now we make an Android Bitmap with matching size ... Nb. at this point we functionally have 3 buffers == image size. Watch your memory usage!
		bm = Bitmap.createBitmap(frame2.width(), frame2.height(), Bitmap.Config.ARGB_8888);
		bm.copyPixelsFromBuffer(frame2.getByteBuffer());
		//src.release();
		frame2.release();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * 1.5), (int) (bm.getHeight() * 1.5), false);
	}
	
	public boolean canPredict()
	{
		if (labelsFile.max()>1)
			return true;
		else
			return false;
		
	}
	
	public String predict(Mat m) {
		if (!canPredict())
			return "";
		int n[] = new int[1];
		double p[] = new double[1];
		IplImage ipl = MatToIplImage(m,WIDTH, HEIGHT);
//		IplImage ipl = MatToIplImage(m,-1, -1);
		
		faceRecognizer.predict(ipl, n, p);
		
		if (n[0]!=-1)
		 mProb=(int)p[0];
		else
			mProb=-1;
	//	if ((n[0] != -1)&&(p[0]<95))
		if (n[0] != -1)
			return labelsFile.get(n[0]);
		else
			return "Unkown";
	}


	

	  IplImage MatToIplImage(Mat m,int width,int heigth)
	  {
		 
		  
		   Bitmap bmp=Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
		  
		   
		   Utils.matToBitmap(m, bmp);
		   return BitmapToIplImage(bmp,width, heigth);
			
	  }

	IplImage BitmapToIplImage(Bitmap bmp, int width, int height) {

		if ((width != -1) || (height != -1)) {
			Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, false);
			bmp = bmp2;
		}

		IplImage image = IplImage.create(bmp.getWidth(), bmp.getHeight(),
				IPL_DEPTH_8U, 4);

		bmp.copyPixelsToBuffer(image.getByteBuffer());
		
		IplImage grayImg = IplImage.create(image.width(), image.height(),
				IPL_DEPTH_8U, 1);

		cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

		return grayImg;
	}


	  
	protected void SaveBmp(Bitmap bmp,String path)
	  {
			FileOutputStream file;
			try {
				file = new FileOutputStream(path , true);
			
			bmp.compress(Bitmap.CompressFormat.JPEG,100,file); 	
		    file.close();
			}
		    catch (Exception e) {
				// TODO Auto-generated catch block
		    	Log.e("",e.getMessage()+e.getCause());
				e.printStackTrace();
			}
		
	  }
	

	public void load(RelativeLayout linearLayout,Context context) {
		train(linearLayout,context);
		
	}

	public int getProb() {
		// TODO Auto-generated method stub
		return mProb;
	}


}
