package com.ioiomint.pixelhelloworld;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
//import android.view.WindowManager;
import android.widget.TextView;

//import com.ledpixelart.pixelopenxc.MainActivity.ConnectTimer;
import com.openxc.VehicleManager;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;
import android.content.pm.PackageManager.NameNotFoundException;
//import com.openxc.measurements.VehicleSpeed;

public class PixelHelloWord<connectTimer, ConnectTimer> extends IOIOActivity   {

   	private ioio.lib.api.RgbLedMatrix.Matrix KIND;  //have to do it this way because there is a matrix library conflict
	private android.graphics.Matrix matrix2;
  	private short[] frame_ = new short[512];
  	public static final Bitmap.Config FAST_BITMAP_CONFIG = Bitmap.Config.RGB_565;
  	private byte[] BitmapBytes;
  	private Bitmap canvasBitmap;
  	private Bitmap originalImage;
  	private int width_original;
  	private int height_original; 	  
  	private float scaleWidth; 
  	private float scaleHeight; 	  	
  	private Bitmap resizedBitmap;  	
	private int resizedFlag = 0;
	
	private VehicleManager mVehicleManager;
	private TextView mVehicleBrakeView;
	private TextView mVehicleSpeedView;
	private int brakePriority = 3;
    private int currentPriority = 0;
    private int pixelFound = 0; 
    private int pedalTimerRunning = 0;
    private Timer _pedalTimer;
    private double speed;
    private double speedDelta;
    private InputStream BitmapInputStream;
    private ioio.lib.api.RgbLedMatrix matrix_;
    
    private ConnectTimer connectTimer; 
    private int matrix_model;
    private Resources resources = null;
	private SharedPreferences prefs;
	private String app_ver;
	protected static final String tag = "openxc";
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //force only portrait mode
        setContentView(R.layout.main);
//	   	KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //only change this if using a different LED matrix than the Pixel Frame
//		frame_ = new short [KIND.width * KIND.height]; //byte array which will be sent to the LED frame
//		BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048		 
//		originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon); //gets the bitmap from your drawables folder
//		WriteImagetoMatrix();
		
//		BitmapInputStream = getResources().openRawResource(R.raw.blank); //load a blank image to clear it
//		loadRGB565();

        mVehicleBrakeView = (TextView) findViewById(R.id.brake_status);
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
		
		Intent intent = new Intent(this, VehicleManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        connectTimer = new ConnectTimer(30000,5000); //pop up a message if PIXEL is not found within 30 seconds
 		connectTimer.start(); 
//		try {
//			clearMatrixImage();
//		} catch (ConnectionLostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
 		resources = this.getResources();
 		setPreferences();
 		
 		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }
    
    private void setPreferences()
    {
     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
     matrix_model = Integer.valueOf(prefs.getString(   
    	        resources.getString(R.string.selected_matrix),
    	        resources.getString(R.string.matrix_default_value))); 
     
//     switch (matrix_model) {  //the user can use other LED displays other than PIXEL's by choosing from preferences
//     case 0:
//    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x16;
//    	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
//    	 break;
//     case 1:
//    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
//    	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
//    	 break;
//     case 2:
//    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32_NEW; //v1 , this matrix has 4 IDC connectors
//    	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
//    	 break;
//     case 3:
//    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2
//    	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
//    	 break;
//     default:	    		 
    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default, it has 2 IDC connectors
    	 BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
//     }
     frame_ = new short [KIND.width * KIND.height];
	 BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	 
	 loadRGB565(); //this function loads a raw RGB565 image to the matrix
    }
    
//    private void WriteImagetoMatrix() {  //here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a canvas, also we'll re-size the image if necessary
//    	
// 		 //let's test if the image is 32x32 resolution
//		 width_original = originalImage.getWidth();
//		 height_original = originalImage.getHeight();
//		 
//		 //if not, no problem, we will re-size it on the fly here		 
//		 if (width_original != KIND.width || height_original != KIND.height) {
//			 resizedFlag = 1;
//			 scaleWidth = ((float) KIND.width) / width_original;
//   		 	 scaleHeight = ((float) KIND.height) / height_original;
//	   		 // create matrix for the manipulation
//	   		 matrix2 = new Matrix();
//	   		 // resize the bit map
//	   		 matrix2.postScale(scaleWidth, scaleHeight);
//	   		 resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0, width_original, height_original, matrix2, true);
//	   		 canvasBitmap = Bitmap.createBitmap(KIND.width, KIND.height, Config.RGB_565); 
//	   		 Canvas canvas = new Canvas(canvasBitmap);
//	   		 canvas.drawRGB(0,0,0); //a black background
//	   	   	 canvas.drawBitmap(resizedBitmap, 0, 0, null);
//	   		 ByteBuffer buffer = ByteBuffer.allocate(KIND.width * KIND.height *2); //Create a new buffer
//	   		 canvasBitmap.copyPixelsToBuffer(buffer); //copy the bitmap 565 to the buffer		
//	   		 BitmapBytes = buffer.array(); //copy the buffer into the type array
//		 }
//		 else {  //if we went here, then the image was already the correct dimension so no need to re-size
//			 resizedFlag = 0;
//			 canvasBitmap = Bitmap.createBitmap(KIND.width, KIND.height, Config.RGB_565); 
//	   		 Canvas canvas = new Canvas(canvasBitmap);
//	   	   	 canvas.drawBitmap(originalImage, 0, 0, null);
//	   		 ByteBuffer buffer = ByteBuffer.allocate(KIND.width * KIND.height *2); //Create a new buffer
//	   		 canvasBitmap.copyPixelsToBuffer(buffer); //copy the bitmap 565 to the buffer		
//	   		 BitmapBytes = buffer.array(); //copy the buffer into the type array
//		 }	       
//		loadImage(); 
//}

    public void loadImage() {
 		int y = 0;
 		for (int i = 0; i < frame_.length; i++) {
 			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
 			y = y + 2;
 		}
 		//we're done with the images so let's recycle them to save memory
	    canvasBitmap.recycle();
	    originalImage.recycle(); 
	    
	    if ( resizedFlag == 1) {
	    	resizedBitmap.recycle(); //only there if we had to resize an image
	    }
 	}
  	
    private void loadRGB565() {
		   
		try {
  			int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads the
  																				// input stream
  																				// into a
  																				// byte array
  			Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
  		} catch (IOException e) {
  			e.printStackTrace();
  		}

  		int y = 0;
  		for (int i = 0; i < frame_.length; i++) {
  			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
  			y = y + 2;
  		}
	   
  }
	
    ///********** IOIO Part of the Code ************************
    class IOIOThread extends BaseIOIOLooper { 
  		private ioio.lib.api.RgbLedMatrix matrix_;

  		@Override
  		protected void setup() throws ConnectionLostException {
  			matrix_ = ioio_.openRgbLedMatrix(KIND);
  		}

  		@Override
  		public void loop() throws ConnectionLostException {
  		
  			matrix_.frame(frame_); //writes whatever is in the frame_ byte array to the Pixel RGB Frame. 
  								   //since this is a loop running constantly, you can simply load other things into frame_ and then this part will take care of updating it to the LED matrix
  			}	
  		}

  	@Override
  	protected IOIOLooper createIOIOLooper() {
  		return new IOIOThread();
  	}
    ////**************************************************************
  	protected void onDestroy() {
	     super.onDestroy();
	       connectTimer.cancel();  //if user closes the program, need to kill this timer or we'll get a crash
	     //  _pedalTimer.cancel();
	     //  _birdTimer.cancel();
	     //  _thxTimer.cancel();
	     //  _rapidBrakeTimer.cancel();
	   }
//  	private void UpdateRapidBrake()  {
//	       i++;
//				
//			if (i == _rapidBrakeDisplayTime) {  //how long to display the rapidBrake image
//					_rapidBrakeTimer.cancel();
//		       		i = 0;
//		       		rapidBrakeTimerRunning = 0;
//					currentPriority = 0;
//					try {
//						clearMatrixImage(); //don't forget to clear as if we're at 0 speed and the brake was on, this image will stay there
//					} catch (ConnectionLostException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			}
//	       	
//	    }
//  	
  	public class ConnectTimer extends CountDownTimer
	{

		public ConnectTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		public void onFinish()
			{
				if (pixelFound == 0) {
					showNotFound (); 					
				}
				
			}

		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
  	
  	private void showNotFound() {	
		AlertDialog.Builder alert=new AlertDialog.Builder(this);
		alert.setTitle(getResources().getString(R.string.notFoundString)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.bluetoothPairingString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
   }
   
////************ Create Images to be displayed on the Board ********************/////
  	private void clearMatrixImage() throws ConnectionLostException {
  		//let's clear the image
		BitmapInputStream = getResources().openRawResource(R.raw.blank); //load a blank image to clear it
		loadRGB565();    	
		matrix_.frame(frame_); 
  	}  

	private void writeBrakeImage() throws ConnectionLostException {
//		originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.apple); //gets the bitmap from your drawables folder
//		WriteImagetoMatrix();		
		BitmapInputStream = getResources().openRawResource(R.raw.footbrake); 
	   	loadRGB565();    	
	   	matrix_.frame(frame_); 
	   }
  	private void writeSuddenBrakeImage() throws ConnectionLostException {
		   	 BitmapInputStream = getResources().openRawResource(R.raw.rapid_brake); //load a blank image to clear it
		   	 loadRGB565();    	
		   	 matrix_.frame(frame_); 
		   }
	
	///*********** Vehicle Service and Binds*****************////
	
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        Log.i("openxc", "Bound to VehicleManager");
	        mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
	        
	        // setting up all the listeners to capture the data we want
	        
	        try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	        
	       try {
				mVehicleManager.addListener(BrakePedalStatus.class, mBrakeListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}	
	    }
	    public void onServiceDisconnected(ComponentName className) {
	        Log.w("openxc", "VehicleService disconnected unexpectedly");
	        mVehicleManager = null;
	    }
  };//end of service connection
  
  	VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
		    public void receive(Measurement measurement) {
		    	final VehicleSpeed _speed = (VehicleSpeed) measurement;
		        PixelHelloWord.this.runOnUiThread(new Runnable() {
		            public void run() {
		            	speed = _speed.getValue().doubleValue() * 0.621371; //we need to convert km/h to mp/h
		            	String speedString = String.format("%.1f", speed);	
		               //mVehicleSpeedView.setText(
		                   // "Vehicle speed (mp/h): " + _speed.getValue().doubleValue());
		            	 mVehicleSpeedView.setText(speedString);
		            }
		        });
		    }
	};
	
	BrakePedalStatus.Listener mBrakeListener = new BrakePedalStatus.Listener() {
	    public void receive(Measurement measurement) {
	    	final BrakePedalStatus _brakeStatus = (BrakePedalStatus) measurement;
	        PixelHelloWord.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	
	            	boolean brakesBoolean = _brakeStatus.getValue().booleanValue();
	            	String brakesText;
	            	if (brakesBoolean == true) {
	            		brakesText = "On";
	            	}
	            	else {
	            		brakesText = "Off";
	            	}
	            	
	            	mVehicleBrakeView.setText(brakesText);
	            	
	            	
	            	//mVehicleBrakeView.setText(
	            	// 	"Brake: " + _brakeStatus.getValue().booleanValue());
	            	
	            	//Log.w("openxc", "current priority " + currentPriority); 
	            	
	            	if (brakePriority >= currentPriority && pixelFound == 1) { 
	            		
	            		if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
	            			//pedalTimer.cancel();
	            			_pedalTimer.cancel();
	            			pedalTimerRunning = 0;
	            			Log.w("openxc", "brake killed the pedal timer"); 
	            		}
	            		
	            		//pedalTimer.cancel(); //stop the timer
	            		//pedalTimerRunning = 0;
	            		
	            		boolean breakValue = _brakeStatus.getValue().booleanValue();
	            		if (breakValue == true ) {
//	            		if (brakesBoolean == true){	
	            			currentPriority = brakePriority;
	            			Log.w("openxc", "brake was true"); 
	            			Log.w("openxc", "Speed Delta: " + speedDelta);
	            			if (speedDelta > 2) {
	            				try {
	            					writeSuddenBrakeImage();  //we'll need to add some code here to hold this image as well for the sudden brake acceleration
								} catch (ConnectionLostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	            			}
	            			else {
	            				try {
		            				writeBrakeImage();
								} catch (ConnectionLostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	            			}
			            		
	            			
	            		}
	            		else {
	            			 
	            			Log.w("openxc", "brake was false"); 
	            			 currentPriority = 0;
	            			// try {
							 try {
								clearMatrixImage();
							} catch (ConnectionLostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	            		}
	            		 
//	            		 Log.w("openxc", breakValue); 
	            		
	            	}
	            }
	        });
	    }
	};
}