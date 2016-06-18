package com.example.parallel;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.example.parallel.task.FloodFillReductionTask;
import com.example.parallel.task.FloodFillTask;

import android.graphics.Bitmap;
import android.util.Log;
/**
 * This class represents a Parallel Floodfill Segmentation.
 * 
 * @author Jona Q
 *
 */
public class ParallelFloodFillFilter extends ParallelFilter{
	
	private double threshold;
	private int minRegionSize;
	
	static ArrayList<int[]>regionList = new ArrayList<int[]>();
	
	/**
	 * Creates an instance of a Parallel FloodFill Filter 
	 * @param tpool				A thread pool. Must be initialized.
	 * @param bitmap			The bitmap to process
	 * @param threshold			The threshold in allowed region difference
	 * @param minRegionSize		The minimum region size allowed
	 */
	public ParallelFloodFillFilter(ThreadPoolExecutor tpool, Bitmap bitmap, double threshold, int minRegionSize){
		this.numThreads = tpool.getCorePoolSize();
		this.threadPool = tpool;
		this.threshold = threshold;
		this.minRegionSize = minRegionSize;
	}

	/**
	 * Launches tasks in parallel to process the input bitmap
	 * and perform floodfill segmentation
	 */
	public Bitmap getFilteredImage(Bitmap bitmap) {
		
		//	Get dimensions
		int imWidth = bitmap.getWidth();
		int imHeight = bitmap.getHeight();
		
		//	Scale bitmap to have rows multiples of numThreads
		while(imHeight % numThreads != 0)
			imHeight--;
		bitmap = Bitmap.createScaledBitmap (bitmap, imWidth, imHeight, false);
		
		//	Initialize new bitmap to new dimensions
		Bitmap newBitmap = Bitmap.createBitmap(imWidth, imHeight, Bitmap.Config.ARGB_8888);
		
		//	Initialize pixel value arrays
		int [][] pixels = new int[numThreads][(imHeight/numThreads) * (imWidth)];
		
		//	To store the result of the tasks (callables)
		ArrayList<Future<int[]>> futureList = new ArrayList<Future<int[]>>();
		
		int rowOffset = 0;	//	to compute bitmap row offset
		//	For each task
		for(int numT = 0; numT < numThreads; numT++){
			//	Get pixel values for each
			bitmap.getPixels(pixels[numT], 0, imWidth, 0, rowOffset, imWidth, imHeight/numThreads);
			
			//	Add the Future result in the array list
			futureList.add( this.threadPool.submit(new FloodFillTask(pixels[numT],
																imWidth,
																imHeight/numThreads,
																threshold,
																this.minRegionSize)
											) 
					);
			
			//	Update the row offset variable
			rowOffset+=(imHeight/numThreads-1);
		}
		
		
		//	Get the pixel arrays from the Future objects
		for(int numT = 0; numT < numThreads; numT++){
			try {
				regionList.add(numT,futureList.get(numT).get());
			} catch (Exception e) {}
		}
		
		for(int numT = 0; numT <= numThreads/2; numT+=2){//	For each two neighboring blocks
			
			this.threadPool.execute(new FloodFillReductionTask(
					imWidth, regionList.get(numT).length/imWidth,
					regionList.get(numT), regionList.get(numT+1), threshold));
			
			
		}
		
		
		
		rowOffset = 0;	//Again, bitmap row offset
		//	For each task
		for(int numT = 0; numT < numThreads; numT++){
			
				//	Get the results and set them in the new bitmap
			newBitmap.setPixels(regionList.get(numT),
					0, imWidth, 0, rowOffset, imWidth, imHeight/numThreads);
				//	Compute row offset
			rowOffset+=(imHeight/numThreads-1);
			Log.d("Parallel", "end of loop " + numT);
		}
		
		Log.d("Parallel", "returning bitmap!");
		//	Finally, return the new bitmap
		return newBitmap;
		
	}

}
