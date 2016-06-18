package com.example.imageproject;

import java.io.FileNotFoundException;
import java.io.IOException;



import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.imagefilters.FloodFillSegmentation;
import com.example.parallel.ParallelFloodFillFilter;
import com.example.parallel.ParallelFloodFillFilter2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This Activity class represents the filter choosing aspect of the application.
 * 
 * @author Jona Q
 * @version 1.0
 */
public class FilterActivity extends ActionBarActivity{

/*For the Navigation Drawer*/
private String[] filterTitles;
private DrawerLayout mDrawerLayout;
private ActionBarDrawerToggle mDrawerToggle;
private ListView mDrawerList;
private CharSequence mDrawerTitle = "Choose a filter";
private CharSequence mTitle = "Pikto";

//	Parallel computing variables
private static Runtime runtime = Runtime.getRuntime(); 				//	Runtime variable
private static int NUMBER_OF_CORES = runtime.availableProcessors();	//	Number of cores
private static int KEEP_ALIVE_TIME = 5;
private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1024);
private ThreadPoolExecutor tPool = new ThreadPoolExecutor(NUMBER_OF_CORES,
		NUMBER_OF_CORES, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, workQueue);


int width, height;
int [] pixels;
/*For setting up the chosen image*/
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

		// Get the title
		//mTitle = mDrawerTitle = getTitle();
		
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
	
	public void imageViewTest(View v){
		Toast.makeText(this, "Id: "+v.getId(), Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "R: "+R.id.imageView1, Toast.LENGTH_SHORT).show();
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
			View rootView = inflater.inflate(R.layout.fragment_filter, container,
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
	
	/** Swaps fragments in the main content view */
	private void selectFilter(int position) {
	    // Create a new fragment and specify the planet to show based on position
	    switch(position){
	    
	    /* Flood Fill Segmentation */
	    case 0: makeToast("Selected Flood Fill Segmentation");
			
			try {
				final ProgressDialog dialog = new ProgressDialog(this);
				dialog.setCancelable(true);
				dialog.setIcon(R.drawable.deluxe_picto_box);
				dialog.setTitle("Please wait...");
				dialog.setMessage("Now filtering image");
				dialog.show();
				
				/////////////////////////////////////////PARALLEL
				/*imageBitmap = Media.getBitmap(this.getContentResolver(), fileUri);
				ParallelFloodFillFilter2 floodFill = new ParallelFloodFillFilter2(tPool, imageBitmap,
						FloodFillSegmentation.THRESHOLD_LOW, FloodFillSegmentation.REGION_MEDIUM);
				
				newBitMap = floodFill.getFilteredImage(imageBitmap);*/
				
				//////////////////////////////////////////////////////////////////////
				width = imageBitmap.getWidth();
	    		height = imageBitmap.getHeight();
	    		newBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    		pixels = new int[height*width];
	    		imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
	    		
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
	    				com.example.imagefilters.FloodFillSegmentation.REGION_SMALL, width, height);
	    		
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
	    		
	    		//dialog.dismiss();
	    		
			}catch (Exception e) {
				
			}
			
    		break;

	    case 1: makeToast("Selected Binary Local Patterns");
			try {
				final ProgressDialog dialog = new ProgressDialog(this);
				dialog.setCancelable(true);
				dialog.setIcon(R.drawable.deluxe_picto_box);
				dialog.setTitle("Please wait...");
				dialog.setMessage("Now filtering image");
				dialog.show();


				width = imageBitmap.getWidth();
				height = imageBitmap.getHeight();
				newBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				pixels = new int[height*width];
				imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

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
	    				/*ParallelFloodFillFilter floodFill = new ParallelFloodFillFilter(tPool, imageBitmap,
	    						FloodFillSegmentation.THRESHOLD_LOW, FloodFillSegmentation.REGION_MEDIUM);

	    				newBitMap = floodFill.getFilteredImage(imageBitmap);
	    				dialog.dismiss();

	    			}*/


				}).start();

				//Set the new bitmap
				((ImageView) findViewById(R.id.imageView1)).setImageBitmap(newBitMap);

				//dialog.dismiss();

			}catch (Exception e) {

			}

			break;
	    case 2: makeToast("Selected Binary Local Patterns");
			try {
				final ProgressDialog dialog = new ProgressDialog(this);
				dialog.setCancelable(true);
				dialog.setIcon(R.drawable.deluxe_picto_box);
				dialog.setTitle("Please wait...");
				dialog.setMessage("Now filtering image");
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
						int [] r = com.example.imagefilters.L0Gradient.applyFilter(imageBitmap);

						newBitMap.setPixels(r, 0, width, 0, 0, width, height);
						dialog.dismiss();
					}

				}).start();

				//Set the new bitmap
				((ImageView) findViewById(R.id.imageView1)).setImageBitmap(newBitMap);

				//dialog.dismiss();

			}catch (Exception e) {

			}

			break;
	    case 3: makeToast("Selected Warping");break;
	    default: makeToast("Undefined Selection");break;
	    }

	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    //setTitle(mPlanetTitles[position]);
	    mDrawerLayout.closeDrawer(mDrawerList);
	    
	    
	}
	
	public void onRestart(){
		super.onRestart();
		tPool.shutdownNow();
		super.onStop();
	}
	public void onStop(){
		tPool.shutdownNow();
		super.onStop();
	}
	
	public void onDestroy(){
		super.onDestroy();
		tPool.shutdownNow();
		super.onStop();
	}
	
	
}
