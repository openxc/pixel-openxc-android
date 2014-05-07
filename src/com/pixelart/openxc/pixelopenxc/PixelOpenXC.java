package com.pixelart.openxc.pixelopenxc;

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

public class PixelOpenXC<connectTimer, ConnectTimer> extends IOIOActivity   {

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
//    private int pedalTimerRunning = 0;
//    private Timer _pedalTimer;
//    private double speedDelta;
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

        mVehicleBrakeView = (TextView) findViewById(R.id.brake_status);
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
		
		if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        
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
     KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default, it has 2 IDC connectors
     BitmapInputStream = getResources().openRawResource(R.raw.openxcgrey);
//     }
     frame_ = new short [KIND.width * KIND.height];
	 BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	 
	 loadRGB565(); //this function loads a raw RGB565 image to the matrix
    }

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
//  		private ioio.lib.api.RgbLedMatrix matrix_;

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
  	@Override
  	public void onDestroy() {
	     super.onDestroy();
	     connectTimer.cancel();  //if user closes the program, need to kill this timer or we'll get a crash
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i("openxc", "Unbinding from Vehicle Manager");
            unbindService(mConnection);
            mVehicleManager = null;
        }
	   }

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
//  	private void writeSuddenBrakeImage() throws ConnectionLostException {
//		   	 BitmapInputStream = getResources().openRawResource(R.raw.rapid_brake); //load a blank image to clear it
//		   	 loadRGB565();    	
//		   	 matrix_.frame(frame_); 
//		   }
//	
	///*********** Vehicle Service and Binds*****************////
	
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        Log.i("openxc", "Bound to VehicleManager");
	        mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
	        
	        // setting up all the listeners to capture the data we want
	        
	        try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
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
    	double last_speed = -1;
	    public void receive(Measurement measurement) {
	    	final VehicleSpeed _speed = (VehicleSpeed) measurement;
        	final double speed = _speed.getValue().doubleValue() * 0.621371; //we need to convert km/h to mph
        	if(speed != last_speed) {
		        PixelOpenXC.this.runOnUiThread(new Runnable() {
		            public void run() {
		            	String speedString = String.format("%.1f", speed);	
		               //mVehicleSpeedView.setText(
		                   // "Vehicle speed (mp/h): " + _speed.getValue().doubleValue());
		            	 mVehicleSpeedView.setText("Vehicle speed (mph): " + speedString);
		            }
		        });
	            last_speed = speed;
        	}
	    }
	};
	
	BrakePedalStatus.Listener mBrakeListener = new BrakePedalStatus.Listener() {
		String last_brakeStatus = "";
	    public void receive(Measurement measurement) {
	    	final BrakePedalStatus _brakeStatus = (BrakePedalStatus) measurement;
        	final boolean brakesBoolean = _brakeStatus.getValue().booleanValue();
        	if(_brakeStatus.toString() != last_brakeStatus) {
		        PixelOpenXC.this.runOnUiThread(new Runnable() {
		            public void run() {
		            	mVehicleBrakeView.setText(_brakeStatus.toString());
		            	     	
		            	//mVehicleBrakeView.setText(
		            	// 	"Brake: " + _brakeStatus.getValue().booleanValue());
		            	
		            	//Log.w("openxc", "current priority " + currentPriority); 
	//	            	
	//	            	if (brakePriority >= currentPriority && pixelFound == 1) { 
	//	            		
	//	            		if (pedalTimerRunning == 1) { //now let's check if the timer is running and start it if not
	//	            			//pedalTimer.cancel();
	//	            			_pedalTimer.cancel();
	//	            			pedalTimerRunning = 0;
	//	            			Log.w("openxc", "brake killed the pedal timer"); 
	//	            		}
	//	            		
	//	            		//pedalTimer.cancel(); //stop the timer
	//	            		//pedalTimerRunning = 0;
	//	            		
	//	            		boolean breakValue = _brakeStatus.getValue().booleanValue();
	//	            		if (breakValue == true ) {
	////	            		if (brakesBoolean == true){	
	//	            			currentPriority = brakePriority;
	//	            			Log.w("openxc", "brake was true"); 
	//	            			Log.w("openxc", "Speed Delta: " + speedDelta);
	//	            			if (speedDelta > 2) {
	//	            				try {
	//	            					writeSuddenBrakeImage();  //we'll need to add some code here to hold this image as well for the sudden brake acceleration
	//								} catch (ConnectionLostException e) {
	//									// TODO Auto-generated catch block
	//									e.printStackTrace();
	//								}
	//	            			}
	//	            			else {
	//	            				try {
	//		            				writeBrakeImage();
	//								} catch (ConnectionLostException e) {
	//									// TODO Auto-generated catch block
	//									e.printStackTrace();
	//								}
	//	            			}
	//			            		
	//	            			
	//	            		}
	//	            		else {
	//	            			 
	//	            			Log.w("openxc", "brake was false"); 
	//	            			 currentPriority = 0;
	//	            			// try {
	//							 try {
	//								clearMatrixImage();
	//							} catch (ConnectionLostException e) {
	//								// TODO Auto-generated catch block
	//								e.printStackTrace();
	//							}
	//	            		}
	//	            		 
	////	            		 Log.w("openxc", breakValue); 
	//	            		
	//	            	}
		            }
		        });
	        	new Thread(new Runnable() {
	        		public void run() {
		            	if (brakesBoolean == true) {
		            		try {
		            			if(matrix_ != null) {
		            				writeBrakeImage();  //we'll need to add some code here to hold this image as well for the sudden brake acceleration
		            			}
							} catch (ConnectionLostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            	}
		            	else {
		            		try {
		            			if(matrix_ != null) {
		            				clearMatrixImage();
		            			}
							} catch (ConnectionLostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            	}
	        		}
	        	}).start();
                last_brakeStatus = _brakeStatus.toString();
        	}
	    }
	};
}