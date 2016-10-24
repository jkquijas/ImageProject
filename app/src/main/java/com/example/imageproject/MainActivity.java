package com.example.imageproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;


import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;


import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

import com.example.imagefilters.FloodFillSegmentation;

public class MainActivity extends AppCompatActivity  {

	private static final int SELECT_PHOTO = 100;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
	private static final String PHOTO_SELECTED_STRING = "photoSelected";
	private static final String PHOTO_ERASABLE_STRING = "photoErasable";

	public static final String imageUriString = "imageUri";
	
	private Uri fileUri;
	//	Screen dimension variables
	private WindowManager wm;
    private Display display;
	private Point point;

	//	Action Toolbar variables
	private Toolbar toolbar;

	//	Navigation Drawer variables
	private String[] filterTitles;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle = "Choose a filter";
	private CharSequence mTitle = "Pikto";

	//	Image variables

	Bitmap selectedImage;
	Bitmap newBitMap;
	int height;
	int width;
	int savedState = 0;
	boolean canceledFilter = false;

	//	Dialog variables
	static MyProgressDialog dialog;

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectFilter(position);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		if(savedState == 0) {
			Log.d("savedState onCreate", ""+savedState);

			savedState = 1;
		}

		setContentView(R.layout.activity_main);
		dialog = new MyProgressDialog(this);

		//	Set toolbar
		toolbar = (Toolbar)findViewById(R.id.my_toolbar);
		toolbar.setBackgroundResource(R.color.palette_1_primary_1);


		//	Get screen dimensions
		wm = getWindowManager();
		display = wm.getDefaultDisplay();
		point = new Point();
		display.getSize(point);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}


		/* For setting up the navigation drawer */
		filterTitles = getResources().getStringArray(R.array.filter_array);
		Log.d("Drawer", filterTitles.toString());

		//setContentView(R.id.drawer_layout);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				toolbar, R.string.drawer_open, R.string.drawer_close) {


			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				//getSupportActionBar().setTitle(mTitle);
				//invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		// enable ActionBar app icon to behave as action to toggle nav drawer
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerToggle.syncState();

		mDrawerLayout.addDrawerListener(mDrawerToggle);


		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		if(mDrawerList == null){
			Log.d("Drawer", "mDrawerList is null");
		}
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, filterTitles));
		mDrawerList.setOnItemClickListener(new MainActivity.DrawerItemClickListener());//Set the list's click listener


		//	Prepare dialog
		dialog.setCancelable(true);
		dialog.setIcon(R.mipmap.pikto_launcher_2);
		dialog.setTitle("Please wait");
		dialog.setMessage("Filtering image");


		Typeface face=Typeface.createFromAsset(getAssets(),"fonts/Beef'd.ttf");
		TextView appTitle = (TextView)findViewById(R.id.toolbar_text);
		appTitle.setTypeface(face);

		getBaseContext();
	}


	/**
	 * Store the state of the activity for when the selection activity starts
	 * @param outState
     */
	public void onSaveInstanceState(Bundle outState){

		if(fileUri != null)
			outState.putString(imageUriString, fileUri.toString());
		if(savedState == 2) {
			outState.putParcelable(getResources().getString(R.string.temp_bitmap_string), selectedImage);
			outState.putInt("SavedWidth", width);
			outState.putInt("SavedHeight", height);
		}
		else if(savedState == 3)
			outState.putParcelable(getResources().getString(R.string.temp_bitmap_string), newBitMap);

		outState.putInt("SavedState", savedState);

		super.onSaveInstanceState(outState);
	}

	/**
	 * This method sets the ImageView's bitmap to be the user selected image
	 * @param savedInstanceData
     */
	protected void onRestoreInstanceState (Bundle savedInstanceData){
		if(savedInstanceData != null) {
			savedState = savedInstanceData.getInt("SavedState");

			//	Restore image
			if(savedState == 2){
				width = savedInstanceData.getInt("SavedWidth");
				height = savedInstanceData.getInt("SavedHeight");
				selectedImage = savedInstanceData.getParcelable(getResources().getString(R.string.temp_bitmap_string));
				((ImageView)findViewById(R.id.imageView1)).setImageBitmap(selectedImage);
			}
			else if(savedState == 3){
				newBitMap = savedInstanceData.getParcelable(getResources().getString(R.string.temp_bitmap_string));
				((ImageView)findViewById(R.id.imageView1)).setImageBitmap(newBitMap);
			}
			//	Unlock drawer
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);



			//	Set correct prompt text
			TextView promptText = (TextView) findViewById(R.id.prompt_textview);
			String prompt = "saved state is neither 1 or 2";
			Log.d("restore", ""+savedState);
			if(savedState == 1){
				prompt = getResources().getString(R.string.select_photo_prompt);
			}
			else if(savedState == 2){
				prompt = getResources().getString(R.string.select_filter_prompt);
			}
			else{
				prompt = getResources().getString(R.string.finished_prompt);
			}
			promptText.setText(prompt);

		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar

		boolean t = super.onCreateOptionsMenu(menu);
		Log.d("onCreateOptionsMenu", ""+t);
		getMenuInflater().inflate(R.menu.main, menu);
	    return t;


	}

	
	public boolean onPrepareOptionsMenu(Menu menu){
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
		Log.d("onOtionsItemSelected", ""+item.toString());
		//	If searching for a photo
		switch (item.getItemId()) {
			case(R.id.action_search_photo):
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");

				startActivityForResult(photoPickerIntent, SELECT_PHOTO);
				break;
			//	If taking a photo
			case(R.id.action_take_photo):
				Intent photoTakerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				fileUri = MediaStorage.getOutputMediaFileUri(MediaStorage.MEDIA_TYPE_IMAGE); // create a file to save the image
				Log.d("Camera",fileUri.toString());

				photoTakerIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
				display.getSize(point);
				if(point.x > point.y)
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				else
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

				// start the image capture Intent
				startActivityForResult(photoTakerIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				break;
			case(android.R.id.home):
				mDrawerLayout.openDrawer(Gravity.LEFT);  // OPEN DRAWER
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	
	/**
	 * Sets the ImageView's BitMap to be the pixel data in the image selected by the user
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {


	    switch(requestCode) {
		//	Handle bitmap on our imageview after image selection
	    case SELECT_PHOTO:
	        if(resultCode == RESULT_OK){
	            fileUri = imageReturnedIntent.getData();
	            String[] filePathColumn = {MediaStore.Images.Media.DATA};
	            Cursor cursor = getContentResolver().query(fileUri, filePathColumn, null, null, null);
	            cursor.moveToFirst();

	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String filePath = cursor.getString(columnIndex);
	            cursor.close();

				Log.d("Dimensions", ""+point.x+", "+point.y);
				selectedImage = decodeSampledBitmapFromFile(filePath, point.x, point.y);
				Log.d("Dimensions", ""+selectedImage.getWidth()+", "+selectedImage.getHeight());

	        }
	        break;
	        
	    case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
	    	if(resultCode == RESULT_OK){
	     		try {
	     			selectedImage = Media.getBitmap(this.getContentResolver(), fileUri);
	     			Log.d("Capture", "height = " + selectedImage.getHeight() + ", width = " + selectedImage.getWidth());
	     			//	Resize the bitmap
	     			selectedImage = Bitmap.createScaledBitmap(selectedImage,
	     					point.x, point.y, false);
	     			Log.d("Capture", "height = " + selectedImage.getHeight() + ", width = " + selectedImage.getWidth());
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }

		savedState = 2;

		height = selectedImage.getHeight();
		width = selectedImage.getWidth();

		((ImageView) findViewById(R.id.imageView1)).setImageBitmap(selectedImage);
		TextView promptText = (TextView)findViewById(R.id.prompt_textview);
		promptText.setText(R.string.select_filter_prompt);
		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		promptText.setAnimation(shake);

		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mDrawerLayout.openDrawer(Gravity.LEFT);  // OPEN DRAWER






	}

	
	/**
	 * Simple helper function to display a Toast
	 * @param s String to be displayed
	 */
	protected void makeToast(String s){
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Handles filter selection logic
	 * @param position	The selected filter's index position on list
     */
	private void selectFilter(int position) {
		//	Create placeholder for results
		newBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//	Store pixel values of selected image in an int array
		int [] pixels = new int[height*width];
		selectedImage.getPixels(pixels, 0, width, 0, 0, width, height);


		switch(position){
			//	Flood Fill Segmentation
			case 0: makeToast("Selected Flood Fill Segmentation");
				try {
					dialog.show();
					new Thread(new MyRunnable(pixels)).start();
				}catch (Exception e) {
					Log.e("FillFloodSegmentation", "Failed to successfully run and finish floodfill filter");
				}
				break;

			case 1: makeToast("Selected Binary Local Patterns");
				break;
			case 2: makeToast("Selected Binary Local Patterns");
				break;
			case 3: makeToast("Selected Warping");break;
			default: makeToast("Undefined Selection");break;
		}

		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);

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
			setHasOptionsMenu(true);
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			Typeface face=Typeface.createFromAsset(getContext().getAssets(),"fonts/Beef'd.ttf");
			TextView appPrompt = (TextView) rootView.findViewById(R.id.prompt_textview);
			appPrompt.setTypeface(face);
			Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
			//shake.setRepeatMode(Animation.INFINITE);
			appPrompt.setAnimation(shake);
			return rootView;
		}
	}

	/**
	 * Custom Runnable that receives array of pixel vales as input
	 */
	public class MyRunnable implements Runnable {
		private int [] data;
		public MyRunnable(int [] data) {
			this.data = data;
		}
		public void run() {
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
					makeToast("Canceled");
					newBitMap = selectedImage;
					savedState = 0;
				}
			});
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					ImageView imageView = (ImageView)findViewById(R.id.imageView1);
					imageView.setImageBitmap(newBitMap);
					//	If we canceled, return to state 2
					if(savedState == 0){
						savedState = 2;
					}
					//	Else, we finished. State 3
					else{
						savedState = 3;
						Typeface face=Typeface.createFromAsset(getAssets(),"fonts/Beef'd.ttf");
						TextView appPrompt = (TextView) findViewById(R.id.prompt_textview);
						appPrompt.setText(R.string.finished_prompt);
						appPrompt.setTypeface(face);
						Animation shake = AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake);
						//shake.setRepeatMode(Animation.INFINITE);
						appPrompt.setAnimation(shake);

					}

				}

			});
			int []r = com.example.imagefilters.FloodFillSegmentation.segmentImage(this.data,
					FloodFillSegmentation.THRESHOLD_LOW,
					FloodFillSegmentation.REGION_LARGE, width, height);
			newBitMap.setPixels(r, 0, width, 0, 0, width, height);
			dialog.dismiss();
		}

	}//	End of MyRunnable class declaration

	public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight || (halfWidth / inSampleSize) >= reqWidth) {
				Log.d("Dimensions", "in while: " + inSampleSize);
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public class MyProgressDialog extends ProgressDialog {
		public MyProgressDialog(Context context) {
			super(context, R.style.MyProgressDialogTheme);
		}

		public MyProgressDialog(Context context, int theme) {
			super(context, theme);
		}
	}
}
