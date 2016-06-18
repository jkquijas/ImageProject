package com.example.parallel;

import java.util.concurrent.ThreadPoolExecutor;

import android.graphics.Bitmap;

/**
 * Abstract class representing the main and shared functionalities
 * between Filter classes
 * 
 * @author Jona Q
 *
 */
public abstract class ParallelFilter {
	
	int numThreads;
	ThreadPoolExecutor threadPool;
	
	/**Returns a filtered bitmap*/
	public abstract Bitmap getFilteredImage(Bitmap bitmap);

}
