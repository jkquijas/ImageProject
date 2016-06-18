package com.example.parallel.task;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import android.graphics.Color;
import android.util.Log;

public class FloodFillReductionTask2 implements Runnable{

	
	private int imWidth;
	private int imHeight;
	private int topIndex;
	private int bottomIndex;
	public int [] R;
	CountDownLatch countDown;
	
	private double threshold;
	
	public FloodFillReductionTask2(CountDownLatch countDown, int width, int height, int[]R,int indTop, int indBottom, double threshold){
		this.R = R;
		this.imWidth = width;
		this.imHeight = height;
		this.threshold = threshold;
		this.topIndex = indTop;
		this.bottomIndex = indBottom;
		this.countDown = countDown;
		
	}
	
	
	public void run(){
		//Log.d("Reduction", "In reduction task run");
		for(int col = 0; col < imWidth; col++){	//	For each pixel on the bordering rows
			
			double diff;
			
			//	Check Left bottom pixel
			if(col-1 >= 0 && bottomIndex+col < R.length){
				diff = computeDifference(R[topIndex+col], R[bottomIndex+col-1]);
				Log.d("Reduction", "Diff "+diff+" Threshold " + threshold);
				if(diff < this.threshold && diff != 0){
					
					mergeRegions(R[topIndex+col], R[bottomIndex+col-1]);
				}
			}
			
			if(bottomIndex+col < R.length){
			//Check middle bottom pixel
				diff = computeDifference(R[topIndex+col], R[bottomIndex+col]);
			if(diff < this.threshold && diff != 0){
				
				mergeRegions(R[topIndex+col], R[bottomIndex+col]);
				
			}
			}
			
			//Check right bottom pixel
			if(col+1 < imWidth &&bottomIndex+col < R.length ){
				diff = computeDifference(R[topIndex+col], R[bottomIndex+col+1]);
				if(diff < this.threshold && diff != 0){
					
					mergeRegions(R[topIndex+col], R[bottomIndex+col+1]);
					
				}
			}
				
		}
		synchronized (this) {
			this.countDown.countDown();
		}
		
	}
	
	public double computeDifference(int a, int b){
		//Log.d("Reduction", "In computeDifference");
		
		//Log.d("Reduction", "a "+a+" b "+b);
		double deltaRed = (Color.red(a) - Color.red(b))*1.0/255.0;
		//Log.d("Reduction", "deltared "+deltaRed);
		double deltaGreen = (Color.green(a) - Color.green(b))*1.0/255.0;
		double deltaBlue = (Color.blue(a) - Color.blue(b))*1.0/255.0;

		return Math.sqrt(Math.pow(deltaRed, 2) + Math.pow(deltaGreen, 2) + Math.pow(deltaBlue, 2));
	}
	
	public void mergeRegions(int topVal, int bottomVal) {
		Log.d("Reduction", "In merge");
		//	New mean value
		int r = (Color.red(topVal)+Color.red(bottomVal))/2;
		int g = (Color.green(topVal)+Color.green(bottomVal))/2;
		int b = (Color.blue(topVal)+Color.blue(bottomVal))/2;
		
		int newVal = Color.argb(0xFF,r, g, b);
		int topOffset = topIndex-imHeight+imWidth;
		for(int i = 0; i < this.imWidth*this.imHeight; i++){
			if(this.R[topOffset+i] == topVal)
				this.R[topOffset+i] = newVal;
			if(this.R[bottomIndex+i] == bottomVal)
				this.R[bottomIndex+i] = newVal;
				
		}
		
	}

}
