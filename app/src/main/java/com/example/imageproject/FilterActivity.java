package com.example.imageproject;

import java.io.FileNotFoundException;
import java.io.IOException;



import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This Activity class represents the filter choosing aspect of the application.
 * 
 * @author Jona Q
 * @version 2.0
 */
public class FilterActivity extends ActionBarActivity{

	int width, height;
	int [] pixels;


	//	Navigation Drawer variables
	private String[] filterTitles;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle = "Choose a filter";
	private CharSequence mTitle = "Pikto";

	//	For setting up user selected image
	private Uri fileUri;
	static Bitmap imageBitmap, newBitMap;

	/*Unimplemented. For sharing on FB, Twitter, etc*/
	private ShareActionProvider mShareActionProvider;

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			selectFilter(position);
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);


		/* For setting up the navigation drawer */
		filterTitles = getResources().getStringArray(R.array.filter_array);
		Log.d("Drawer", filterTitles.toString());
		
		// enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //setContentView(R.id.drawer_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        if(mDrawerList == null){
        	Log.d("Drawer", "mDrawerList is null");
        }
        // Set the adapter for the list view
        Log.d("Drawer", filterTitles.toString());
        Log.d("Drawer", "mDrawerList" + mDrawerList.toString());
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, filterTitles));
        
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());//Set the list's click listener
        
        /* Set up the action bar*/
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    Bundle b = getIntent().getExtras();
	    fileUri = Uri.parse(b.getString("imageUri"));
	    
		
		try {
			imageBitmap = Media.getBitmap(this.getContentResolver(), fileUri);
			if(imageBitmap == null){
				Log.d("Filter", "null bitmap");
			}
			else{
				Log.d("Filter", "bitmap not null");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_choose_filter).setVisible(!drawerOpen);
        menu.findItem(R.id.action_save_photo).setVisible(!drawerOpen);
        menu.findItem(R.id.action_share).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_save_photo:
            makeToast("Save feature coming soon :)");
            return true;
        case R.id.action_choose_filter:
            makeToast("Not sure what this will do :)");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
	protected void onStart(){
		super.onStart();
		((ImageView) findViewById(R.id.imageView1)).setImageBitmap(imageBitmap);//Set the previous image's bitmap
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.filter_menu, menu);
	    
	    //Set up ShareActionProvider's default share intent
	    MenuItem shareItem = menu.findItem(R.id.action_share);
	    mShareActionProvider = (ShareActionProvider)
	            MenuItemCompat.getActionProvider(shareItem);
	    mShareActionProvider.setShareIntent(getDefaultIntent());
	    
	    return super.onCreateOptionsMenu(menu);
	}

	
	/**
	 * Simple helper function to display a Toast
	 * @param s String to be displayed
	 */
	protected void makeToast(String s){
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
	/** Defines a default (dummy) share intent to initialize the action provider.
	  * However, as soon as the actual content to be used in the intent
	  * is known or changes, you must update the share intent by again calling
	  * mShareActionProvider.setShareIntent()
	  */
	private Intent getDefaultIntent() {
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("image/*");
	    return intent;
	}
	

	private void selectFilter(int position) {
		//	Prepare dialog
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(true);
		dialog.setIcon(R.drawable.deluxe_picto_box);
		dialog.setTitle("Please wait...");
		dialog.setMessage("Now filtering image");

		//	Extract image data
		width = imageBitmap.getWidth();
		height = imageBitmap.getHeight();
		newBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		pixels = new int[height*width];
		imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

	    switch(position){
	    
	    //	Flood Fill Segmentation
	    case 0: makeToast("Selected Flood Fill Segmentation");
			
			try {
				dialog.show();
	    		
	    		new Thread(new Runnable(){
	    			public void run() {

	    				dialog.setCancelable(true);
	    				dialog.setOnCancelListener(new OnCancelListener(){
	    					@Override
	    					public void onCancel(DialogInterface dialog) {
	    						makeToast("Canceled");
	    					}					
	    				});
	    		int []r = com.example.imagefilters.FloodFillSegmentation.segmentImage(pixels,
	    				com.example.imagefilters.FloodFillSegmentation.THRESHOLD_LOW,
	    				com.example.imagefilters.FloodFillSegmentation.REGION_LARGE, width, height);
	    		
	    		newBitMap.setPixels(r, 0, width, 0, 0, width, height);
	    		dialog.dismiss();
	    			}
	    				/*ParallelFloodFillFilter floodFill = new ParallelFloodFillFilter(tPool, imageBitmap,
	    						FloodFillSegmentation.THRESHOLD_LOW, FloodFillSegmentation.REGION_MEDIUM);
	    				
	    				newBitMap = floodFill.getFilteredImage(imageBitmap);
	    				dialog.dismiss();
	    				
	    			}*/
	    		
	    			
	    		}).start();
	    		
	    		//Set the new bitmap
	    		((ImageView) findViewById(R.id.imageView1)).setImageBitmap(newBitMap);

			}catch (Exception e) {
				
			}
			
    		break;

	    case 1: makeToast("Selected Binary Local Patterns");
			try {

				dialog.show();

				new Thread(new Runnable(){
					public void run() {

						dialog.setCancelable(true);
						dialog.setOnCancelListener(new OnCancelListener(){
							@Override
							public void onCancel(DialogInterface dialog) {
								makeToast("Canceled");
							}
						});
						int [] r = com.example.imagefilters.CensusTransform.applyCensusTransform(imageBitmap);

						newBitMap.setPixels(r, 0, width, 0, 0, width, height);
						dialog.dismiss();
					}

				}).start();

				//Set the new bitmap
				((ImageView) findViewById(R.id.imageView1)).setImageBitmap(newBitMap);

			}catch (Exception e) {

			}

			break;
	    case 2: makeToast("Selected Binary Local Patterns");
			try {

				dialog.show();

				width = imageBitmap.getWidth();
				height = imageBitmap.getHeight();
				while((width & (width - 1)) != 0)width--;
				while((height & (height- 1)) != 0)height--;

				imageBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, true);

				newBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				pixels = new int[height*width];
				imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

				new Thread(new Runnable(){
					public void run() {

						dialog.setCancelable(true);
						dialog.setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								makeToast("Canceled");
							}
						});
						//int [] r = com.example.imagefilters.L0Gradient.applyFilter(imageBitmap);
						//	TODO update this
						int [] r = null;

						newBitMap.setPixels(r, 0, width, 0, 0, width, height);
						dialog.dismiss();
					}

				}).start();

				//Set the new bitmap
				((ImageView) findViewById(R.id.imageView1)).setImageBitmap(newBitMap);


			}catch (Exception e) {

			}

			break;
	    case 3: makeToast("Selected Warping");break;
	    default: makeToast("Undefined Selection");break;
	    }

	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerList);

	}
	
	public void onRestart(){
		super.onRestart();
	}
	public void onStop(){
		super.onStop();
	}
	
	public void onDestroy(){
		super.onDestroy();
	}
	
	
}
