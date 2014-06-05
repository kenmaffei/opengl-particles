/*******************************************************************************
 * Copyright 2014 Kenneth Maffei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.kennethmaffei.particles;

import java.io.InputStream;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Our main activity.
 * 
 * @author Kenneth Maffei
 *
 */
public class MainActivity extends Activity {

	private GLSurfaceView glSurfaceView;
	private GLRenderer glRenderer;
	private float heightScale;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Globals.context = this;
		
		//Requesting to turn the title OFF 
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		//Making it full screen 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
		WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		WindowManager wm = getWindowManager();
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		
		Globals.deviceWidth = dm.widthPixels;
		Globals.deviceHeight = dm.heightPixels;
		
		glSurfaceView = new GLSurfaceView(this);
		glRenderer = new GLRenderer();
		glSurfaceView.setRenderer(glRenderer); 
		setContentView(glSurfaceView);
		
		ImageView steamButton = new ImageView(this);
		RelativeLayout rl1 = new RelativeLayout(this);
		rl1.addView(steamButton);
		rl1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		
		ImageView fireButton = new ImageView(this);
		RelativeLayout rl2 = new RelativeLayout(this);
		rl2.addView(fireButton);
		rl2.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		
		ImageView smokeButton = new ImageView(this);
		RelativeLayout rl3 = new RelativeLayout(this);
		rl3.addView(smokeButton);
		rl3.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		
		heightScale = Globals.deviceHeight/1280.0f;
		
		//Lay out the buttons dynamically
		Bitmap bitmap = loadGraphic("steamButton.jpg");
		if(bitmap != null) {
			steamButton.setImageBitmap(bitmap);
			
			int layoutHeight = 2*(int)(Globals.deviceHeight - bitmap.getHeight());
			int layoutWidth = bitmap.getWidth();
			
			RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
			addContentView(rl1, rlParams);
			
			steamButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					glRenderer.setToSteam();
				} 
			});
		}
		
		bitmap = loadGraphic("fireButton.jpg");
		if(bitmap != null) {
			fireButton.setImageBitmap(bitmap);
			
			int layoutHeight = 2*(int)(Globals.deviceHeight - bitmap.getHeight());
			int layoutWidth = 2*(int)Globals.deviceWidth - bitmap.getWidth();
			
			RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
			addContentView(rl2, rlParams);
			
			fireButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					glRenderer.setToFire();
				} 
			});
		}
		
		bitmap = loadGraphic("smokeButton.jpg");
		if(bitmap != null) {
			smokeButton.setImageBitmap(bitmap);
			
			int layoutHeight = 2*(int)(Globals.deviceHeight - bitmap.getHeight());
			int layoutWidth = (int)Globals.deviceWidth;
			
			RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(layoutWidth, layoutHeight);
			addContentView(rl3, rlParams);
			
			smokeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					glRenderer.setToBlackSmoke();
				} 
			});
		}
	}
	
	private Bitmap loadGraphic(String file) {
		Bitmap bitmap = null;
		try{
			InputStream is = getAssets().open(file); 
			int size = is.available(); 
			byte[] buffer = new byte[size]; 
			is.read(buffer, 0, size);
			is.close(); 
			
			BitmapFactory.Options opt = new BitmapFactory.Options(); 
			opt.inDither = false;
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			
			Bitmap tmp = BitmapFactory.decodeByteArray(buffer, 0, size, opt);
			bitmap = Bitmap.createScaledBitmap(tmp, (int)(tmp.getWidth()*heightScale), (int)(tmp.getHeight()*heightScale), true);
		}
		catch(Exception e) {
			
		}
		return bitmap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override 
    protected void onResume() { 
        super.onResume(); 

        glSurfaceView.onResume(); 
    } 

	@Override 
    protected void onPause() { 
        super.onPause(); 

        glSurfaceView.onPause(); 
    } 
}
