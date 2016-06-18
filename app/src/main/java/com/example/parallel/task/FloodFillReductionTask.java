package com.example.parallel.task;

import android.util.Log;

public class FloodFillReductionTask implements Runnable{

	
	private int imWidth;
	private int imHeight;
	
	public int [] top;
	public int [] bottom;
	
	private double threshold;
	
	public FloodFillReductionTask(int width, int height, int [] t, int [] b, double threshold){
		this.imWidth = width;
		this.imHeight = height;
		this.top = t;
		this.bottom = b;
		this.threshold = threshold;
	}
	
	
	public void run(){
		Log.d("Reduction", "In reduction task run");
		int topOffset = (imHeight)*(imWidth)-imWidth;
		for(int col = 0; col < imWidth; col++){	//	For each pixel on the bordering rows
			
			
			//	Check Left bottom pixel
			if(col-1 >= 0){
				if(Math.abs(top[topOffset+col] - bottom[col-1]) < this.threshold && 
						top[topOffset+col] - bottom[col-1] != 0){
					
					mergeRegions(top[topOffset+col], bottom[col-1]);
				}
			}
			
			//Check middle bottom pixel
			if(Math.abs(top[topOffset+col] - bottom[col]) < this.threshold &&
					top[topOffset+col] - bottom[col] != 0){
				
				mergeRegions(top[topOffset+col], bottom[col]);
				
			}
			
			//Check right bottom pixel
			if(col+1 < imWidth){
				if(Math.abs(top[topOffset+col] - bottom[col+1]) < this.threshold &&
						top[topOffset+col] - bottom[col+1] != 0){
					
					mergeRegions(top[topOffset+col], bottom[col+1]);
					
				}
			}
				
		}
	}
	
	public void mergeRegions(int topVal, int bottomVal) {
		Log.d("Reduction", "In merge");
		//	New mean value
		int newVal = (topVal + bottomVal)/2;
		
		for(int i = 0; i < this.top.length; i++){
			if(this.top[i] == topVal)
				this.top[i] = newVal;
			if(this.bottom[i] == bottomVal)
				this.bottom[i] = newVal;
				
		}
		
	}

}
