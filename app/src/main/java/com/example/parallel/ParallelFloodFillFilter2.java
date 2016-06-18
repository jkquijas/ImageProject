package com.example.parallel;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;












import com.example.parallel.task.FloodFillReductionTask2;
import com.example.parallel.task.FloodFillTask2;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

/**
 * This class represents a Parallel Floodfill Segmentation.
 * 
 * @author Jona Q
 *
 */
public class ParallelFloodFillFilter2 extends ParallelFilter{
	
	private double threshold;
	private int minRegionSize;
	
	private static final int CHUNK_SIZE = 32;
	
	static ArrayList<int[]>regionList = new ArrayList<int[]>();
	static int [] pixels;
	static int [] R;
	static CountDownLatch countDown;
	static int imWidth;
	static int imHeight;
	/**
	 * Creates an instance of a Parallel FloodFill Filter 
	 * @param tpool				A thread pool. Must be initialized.
	 * @param bitmap			The bitmap to process
	 * @param threshold			The threshold in allowed region difference
	 * @param minRegionSize		The minimum region size allowed
	 */
	public ParallelFloodFillFilter2(ThreadPoolExecutor tpool, Bitmap bitmap, double threshold, int minRegionSize){
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
		
		Log.d("Flood2", "In getFilteredImage()");
		imWidth = bitmap.getWidth();
		imHeight = bitmap.getHeight();
		
		//	Scale bitmap to have rows multiples of numThreads
		while(imHeight % CHUNK_SIZE != 0)
			imHeight--;

		bitmap = Bitmap.createScaledBitmap (bitmap, imWidth, imHeight, false);
		
		//	Initialize pixel value array
		pixels = new int[imHeight*imWidth];
		R = new int[imHeight*imWidth];
		
		bitmap.getPixels(pixels, 0, imWidth, 0, 0, imWidth, imHeight);
		
		//	Initialize new bitmap to new dimensions
		Bitmap newBitmap = Bitmap.createBitmap(imWidth, imHeight, Bitmap.Config.ARGB_8888);
		
		int numChunks = imHeight/CHUNK_SIZE;
		Log.d("Reduction", "Num chunks "+numChunks);
		countDown = new CountDownLatch(numChunks);


		
		
		// Fire the FloodFill Tasks
		for(int i = 0; i < imHeight; i+= CHUNK_SIZE){
			Log.d("Flood2", "Creating task "+i);
			this.threadPool.execute(new FloodFillTask2(countDown,pixels, R, i*imWidth,
					imWidth, CHUNK_SIZE,threshold, minRegionSize));
			
		}
		
		//	Wait!
		try {
			countDown.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//	Reduce!
		int chunk = CHUNK_SIZE;
		while(numChunks != 1){
			numChunks = numChunks/2;
			Log.d("Reduction", "Num chunks "+numChunks);
			countDown = new CountDownLatch(numChunks);
			for(int i = imWidth*chunk-imWidth; i < imHeight*imWidth; i+=(chunk*imWidth*2)){
				Log.d("Flood2", "Reduction task "+i);
				//this.threadPool.execute(new TestRunnable(countDown, R,i,imWidth));
				this.threadPool.execute(new FloodFillReductionTask2(countDown,imWidth, chunk,R,
						i,i+imWidth,threshold));
			}
			//	Wait
			try {
				countDown.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			chunk *= 2;
			 
		}
		
			
		/*for(int numT = 0; numT <= numThreads/2; numT+=2){//	For each two neighboring blocks
			
			this.threadPool.execute(new FloodFillReductionTask(
					imWidth, regionList.get(numT).length/imWidth,
					regionList.get(numT), regionList.get(numT+1), threshold));
			
			
		}*/
		
		/*try {
			this.threadPool.shutdown();
			this.threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Log.d("Reduction", "Done!");
		/*
		
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
		
		Log.d("Parallel", "returning bitmap!");*/
		//	Finally, return the new bitmap
		//for(int i = 0; i < R.length;i++)
			//Log.d("Flood2", ""+R[i]);
		newBitmap.setPixels(R, 0, imWidth, 0, 0, imWidth, imHeight);
		return newBitmap;
		
	}

	private class TestRunnable implements Runnable{
		int[]R;
		int i;
		int w;
		CountDownLatch c;
		TestRunnable(CountDownLatch c, int [] R, int i, int w){
			this.c=c;
			this.R= R;
			this.i = i;
			this.w=w;
		}
		
		public void run(){
			for(int i = this.i; i<this.i+this.w ; i++){
				this.R[i] += 200;
			}
			c.countDown();
		}
	}
}
