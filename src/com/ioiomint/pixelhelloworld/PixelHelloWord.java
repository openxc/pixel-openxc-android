package com.ioiomint.pixelhelloworld;

import java.nio.ByteBuffer;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;

public class PixelHelloWord extends IOIOActivity   {

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
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //force only portrait mode
        setContentView(R.layout.main);
	   	KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //only change this if using a different LED matrix than the Pixel Frame
		frame_ = new short [KIND.width * KIND.height]; //byte array which will be sent to the LED frame
		BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048		 
		originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.apple); //gets the bitmap from your drawables folder
		WriteImagetoMatrix();
     
    }
    
    private void WriteImagetoMatrix() {  //here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a canvas, also we'll re-size the image if necessary
    	
 		 //let's test if the image is 32x32 resolution
		 width_original = originalImage.getWidth();
		 height_original = originalImage.getHeight();
		 
		 //if not, no problem, we will re-size it on the fly here		 
		 if (width_original != KIND.width || height_original != KIND.height) {
			 resizedFlag = 1;
			 scaleWidth = ((float) KIND.width) / width_original;
   		 	 scaleHeight = ((float) KIND.height) / height_original;
	   		 // create matrix for the manipulation
	   		 matrix2 = new Matrix();
	   		 // resize the bit map
	   		 matrix2.postScale(scaleWidth, scaleHeight);
	   		 resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0, width_original, height_original, matrix2, true);
	   		 canvasBitmap = Bitmap.createBitmap(KIND.width, KIND.height, Config.RGB_565); 
	   		 Canvas canvas = new Canvas(canvasBitmap);
	   		 canvas.drawRGB(0,0,0); //a black background
	   	   	 canvas.drawBitmap(resizedBitmap, 0, 0, null);
	   		 ByteBuffer buffer = ByteBuffer.allocate(KIND.width * KIND.height *2); //Create a new buffer
	   		 canvasBitmap.copyPixelsToBuffer(buffer); //copy the bitmap 565 to the buffer		
	   		 BitmapBytes = buffer.array(); //copy the buffer into the type array
		 }
		 else {  //if we went here, then the image was already the correct dimension so no need to re-size
			 resizedFlag = 0;
			 canvasBitmap = Bitmap.createBitmap(KIND.width, KIND.height, Config.RGB_565); 
	   		 Canvas canvas = new Canvas(canvasBitmap);
	   	   	 canvas.drawBitmap(originalImage, 0, 0, null);
	   		 ByteBuffer buffer = ByteBuffer.allocate(KIND.width * KIND.height *2); //Create a new buffer
	   		 canvasBitmap.copyPixelsToBuffer(buffer); //copy the bitmap 565 to the buffer		
	   		 BitmapBytes = buffer.array(); //copy the buffer into the type array
		 }	
        
		loadImage(); 
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
  								   //since this is a loop running constrantly, you can simply load other things into frame_ and then this part will take care of updating it to the LED matrix
  			}	
  		}

  	@Override
  	protected IOIOLooper createIOIOLooper() {
  		return new IOIOThread();
  	}
    ////**************************************************************
}