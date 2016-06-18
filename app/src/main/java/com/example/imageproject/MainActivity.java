package com.example.imageproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

public class MainActivity extends ActionBarActivity {

	private static final int SELECT_PHOTO = 100;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
	private static final String PHOTO_SELECTED_STRING = "photoSelected";
	private static final String PHOTO_ERASABLE_STRING = "photoErasable";
	//private Point outPoint;
	
	public static final String imageUriString = "imageUri";
	
	private Uri fileUri;
	private boolean photoFromGallery = false;
	private boolean photoSelected = false;
	private boolean photoErasable = false;
	
	private WindowManager wm;
    private Display display;
    private Point point;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		wm = getWindowManager();
		display = wm.getDefaultDisplay();
		point = new Point();
		display.getSize(point);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		getBaseContext();
		
	    //outPoint = new Point();
	    //display.getSize(outPoint);
		//ActionBar actionBar = getSupportActionBar();
	    //actionBar.setDisplayHomeAsUpEnabled(true);
	    
	}

	/**
	 * Store the state of the activity
	 */
	public void onSaveInstanceState(Bundle outState){
		if(fileUri != null)
			outState.putString(imageUriString, fileUri.toString());
		outState.putBoolean(PHOTO_SELECTED_STRING, photoSelected);
		outState.putBoolean(PHOTO_ERASABLE_STRING, photoErasable);
		super.onSaveInstanceState(outState);
	}
	
	protected void onRestoreInstanceState (Bundle savedInstanceData){
		Log.d("Screen", "in onRestoreInstanceState (Bundle savedInstanceData)");
		photoSelected = savedInstanceData.getBoolean(PHOTO_SELECTED_STRING);
		photoErasable = savedInstanceData.getBoolean(PHOTO_ERASABLE_STRING);
		
		if(photoSelected){
			fileUri = Uri.parse(savedInstanceData.getString(imageUriString));
			Bitmap bitmap;
			try {
				bitmap = Media.getBitmap(this.getContentResolver(), fileUri);
				((ImageView) findViewById(R.id.imageView1)).setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
	    return super.onCreateOptionsMenu(menu);
	    
	}
	
	public boolean onPrepareOptionsMenu(Menu menu){
		menu.findItem(R.id.action_accept_photo).setVisible(photoSelected);      
		menu.findItem(R.id.action_cancel_photo).setVisible(photoErasable);
		
		return true;
	}
	
	/**
	 * This method is invoked when a Menu item in the Action Bar is selected
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		
		if (id == R.id.action_search_photo) {								/*****If searching for a photo******/
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			
			photoFromGallery = true;
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		}
		else if (id == R.id.action_take_photo) {							/****If taking a photo*****/
			Intent photoTakerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			fileUri = MediaStorage.getOutputMediaFileUri(MediaStorage.MEDIA_TYPE_IMAGE); // create a file to save the image
			Log.d("Camera",fileUri.toString());
			
			photoFromGallery = false;//Set flag to false
			photoTakerIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
			display.getSize(point);
			if(point.x > point.y)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			else
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		    // start the image capture Intent
		    startActivityForResult(photoTakerIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		}
		else if (id == R.id.action_accept_photo) {						/*****If accepting a photo******/
			Intent intent = new Intent("com.example.imageproject.Filter");
			
			
			intent.putExtra(imageUriString, fileUri.toString()); //Set the image's URI in the Bundle, as a String

			startActivity(intent); //Start Filter activty
		}
		else if (id == R.id.action_cancel_photo) {						/*******If canceling a photo*******/
			if(!photoFromGallery){
				//If photo came from camera, delete it
				File fdelete = new File(fileUri.getPath());
			    if (fdelete.exists()){
			    	if (fdelete.delete())
			    		Log.d("Camera", "File: " + fileUri.getPath()+" deleted");
			    	else
			    		Log.d("Camera", "File: " + fileUri.getPath()+" NOT deleted");
			    }
			}
			
			photoSelected = false;//Set visibility of Accept menu item to true
			photoErasable = false;//Set visibility of Erase menu item to true
			invalidateOptionsMenu();
			((ImageView)findViewById(R.id.imageView1)).setImageResource(R.drawable.ic_action_photo);
		}
			
		
		return super.onOptionsItemSelected(item);
	}

	
	
	/**
	 * Sets the ImageView's BitMap to be the pixel data in the image selected by the user
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		
		Bitmap yourSelectedImage; 

	    switch(requestCode) { 
	    case SELECT_PHOTO:
	        if(resultCode == RESULT_OK){
	            fileUri = imageReturnedIntent.getData();
	            String[] filePathColumn = {MediaStore.Images.Media.DATA};

	            Cursor cursor = getContentResolver().query(fileUri, filePathColumn, null, null, null);
	            cursor.moveToFirst();

	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String filePath = cursor.getString(columnIndex);
	            cursor.close();
	            
	            yourSelectedImage = BitmapFactory.decodeFile(filePath);
	            ((ImageView) findViewById(R.id.imageView1)).setImageBitmap(yourSelectedImage);
	            
	            photoSelected = true;//Set visibility of Accept menu item to true
				photoErasable = true;//Set visibility of Erase menu item to true
				invalidateOptionsMenu();
	            
	        }
	        break;
	        
	    case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
	    	if(resultCode == RESULT_OK){
	     		try {
	     			Log.d("Capture", "before get bitmap");
	     			yourSelectedImage = Media.getBitmap(this.getContentResolver(), fileUri);
	     			Log.d("Capture", "height = " + yourSelectedImage.getHeight() + ", width = " + yourSelectedImage.getWidth());
	     			Log.d("Capture", "after get bitmap");
	     			//	Resize the bitmap
	     			yourSelectedImage = Bitmap.createScaledBitmap(yourSelectedImage,
	     					point.x, point.y, false);
	     			Log.d("Capture", "height = " + yourSelectedImage.getHeight() + ", width = " + yourSelectedImage.getWidth());
	     			makeToast("Resized image!");
					((ImageView) findViewById(R.id.imageView1)).setImageBitmap(yourSelectedImage);
					
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					
					photoSelected = true;//Set visibility of Accept menu item to true
					photoErasable = true;//Set visibility of Erase menu item to true
					invalidateOptionsMenu();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    		
	    	}
	    	
	    }
	}
	
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

	}
	
	
	/**
	 * Simple helper function to display a Toast
	 * @param s String to be displayed
	 */
	protected void makeToast(String s){
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
}
