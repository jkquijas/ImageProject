package com.example.parallel.task;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Callable;

import android.graphics.Color;

public class FloodFillTask implements Callable<int[]>{

	int[]pixels;
	double threshold;
	int minRegionSize;
	int width;
	int height;
	int [] R;
	
	/**
	 * 
	 * @param pixels			The pixel array. Gathered from bitmap.getPixels()
	 * @param width				The sub image's logical width
	 * @param height			The sub image's logical height
	 * @param threshold 		The region difference threshold
	 * @param minRegionSize		The minimum region size allowed
	 */
	public FloodFillTask(int[]pixels, int width, int height, double threshold, int minRegionSize){
		//	Set the dimensions
		this.width = width;
		this.height = height;
		//	Set the parameters
		this.threshold = threshold;
		this.minRegionSize = minRegionSize;
		//	Set the pixel array
		this.pixels = pixels;
		
	}
	@Override
	public int [] call() {
		int [] R = new int[this.pixels.length];//Region vector R 
		
		/* meanRegion at index i contains the mean color intensity at region i*/
		ArrayList<Integer> meanRegion = new ArrayList<Integer>();
		
		/*sizeRegion at index i contains the size of region i*/
		ArrayList<Integer> sizeRegion = new ArrayList<Integer>();
		
		Stack <Integer> stack = new Stack <Integer> ();
	
		int region = 0; //The region counter r
		int p = 0; //The first pixel 'p' without an assigned region 'r'
				
		while(R[p] == 0){//While there are pixels that don't belong to any region
			
			region++; 
			R[p] = region; //Assign region number to R[p]
			
			
			int sumRed = Color.red(pixels[p]);//The sum of red pixel values for the current region r
			int sumGreen = Color.green(pixels[p]);//The sum of gree pixel values for the current region r
			int sumBlue = Color.blue(pixels[p]); //The sum of blue pixel values for the current region r
			
			int n = 1; //The number of pixels in the current region r
			
			stack.push(p);
			while(!stack.isEmpty()){
				int q = stack.pop();
				int[]neighbors = getNeighbors(width, height, q);
				
				//For each neighbor s of q
				for(int neighbor = 0; neighbor < neighbors.length; neighbor++){
					if(neighbors[neighbor] == -1)
						continue;
						
					int s = neighbors[neighbor]; 

					if(R[s] == 0){
						
						
						
						double deltaRed = Color.red(pixels[s]) - (sumRed/n);
						double deltaGreen = Color.green(pixels[s]) - (sumGreen/n);
						double deltaBlue = Color.blue(pixels[s]) - (sumBlue/n);

						double pixelDifference = getPixelValue(deltaRed, deltaGreen, deltaBlue);
												
						if( pixelDifference < threshold){
						
							R[s] = region;
							/*sumRed += ((pixels[s] >> 16) & 0xff);
							sumGreen += ((pixels[s] >> 8) & 0xff);
							sumBlue += (pixels[s] & 0xff);*/
							sumRed += Color.red(pixels[s]);
							sumGreen += Color.green(pixels[s]);
							sumBlue += Color.blue(pixels[s]);
							n++; //There is one more pixel in current region r
							
							
							stack.push(s);
						}
						
					}
					
				}  
			}
			
			
			int color = Color.argb(0xFF,sumRed/n, sumGreen/n, sumBlue/n);
			
			meanRegion.add(color);
			sizeRegion.add(n);

			p = getNextPixel(R); //Find next pixel with unassigned region	
		}
		
		ArrayList<Integer> newMeanRegion = removeSmallRegions(sizeRegion, meanRegion, minRegionSize);
		
		for(int i = 0; i < R.length; i++)
			R[i] = newMeanRegion.get(R[i] - 1); //Map all mean region colors to the corresponding pixels
		return R;		
	}
	
	private static ArrayList<Integer> removeSmallRegions(ArrayList<Integer> sizeRegion, ArrayList<Integer> meanRegion, int minRegionSize){

		for(int i = 0; i< sizeRegion.size(); i++){ //Check every regions' size 
			if(sizeRegion.get(i) < minRegionSize){ //if the region's size is less than the minimum allowed size
				
				int newRegion = 0;
				double pixelDifference = 1000;
				
				for(int j = 0; j < sizeRegion.size(); j++){
					
					if(sizeRegion.get(j) >= minRegionSize){
						
						int currentColor = meanRegion.get(i);
						int compareColor = meanRegion.get(j);
						
						double deltaRed = Color.red(currentColor) - Color.red(compareColor);
						double deltaGreen = Color.green(currentColor) - Color.green(compareColor);
						double deltaBlue = Color.blue(currentColor) - Color.blue(compareColor);
						
						double currentDifference = getPixelValue(deltaRed, deltaGreen, deltaBlue);
						
						if(pixelDifference > currentDifference){
							pixelDifference = currentDifference;
							newRegion = j;
							
						}
					}				 
				}
				
				sizeRegion.set(newRegion, sizeRegion.get(newRegion) + sizeRegion.get(i));
				sizeRegion.set(i, sizeRegion.get(newRegion) + sizeRegion.get(i));
				
				int newRed = (Color.red(meanRegion.get(i)) + Color.red(meanRegion.get(newRegion)))/2;
				int newGreen = (Color.green(meanRegion.get(i)) + Color.green(meanRegion.get(newRegion)))/2; 		
				int newBlue = (Color.blue(meanRegion.get(i)) + Color.blue(meanRegion.get(newRegion)))/2;
				
				int newColor = Color.argb(0xFF, newRed, newGreen, newBlue);
				
				meanRegion.set(i, newColor);
				meanRegion.set(newRegion, newColor);
			}
		}
		
		return meanRegion;
		
	}
	
private static double getPixelValue(double red, double green, double blue){
		
		red = red/255;     /*Normalize the color values*/
		green = green/255;
		blue = blue/255;
		
		/*Compute the color norm value*/
		double pixelValue = Math.sqrt(Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2));
		
		return pixelValue;
	}
	
	private static int getNextPixel(int[]R){
		int p;
		int length = R.length;
		
		for(p = 0; p < length; p++){
			if(R[p] == 0)
				return p;
		}
		if(p == length)
			return p = 0;
		
		return p;
	}
	
	
private static int[] getNeighbors(int width, int height, int p){
		
		int[]neighbors = new int[9];
		
		
		neighbors[0] = (p - width) - 1;//Northwest
		neighbors[1] = p - width; //North
		neighbors[2] = (p - width) + 1 ;//Northeast
		neighbors[3] = p - 1;//West
		neighbors[4] = p;
		neighbors[5] = p + 1;//East
		neighbors[6] = (p + width) - 1;//Southwest
		neighbors[7] = p + width; //South
		neighbors[8] = (p + width) + 1;//Southeast
		
		
		if(neighbors[1] < 0){ //Check north neighbor
			neighbors[1] = -1;
			neighbors[2] = -1;//Flag northeast neighbor
			neighbors[0] = -1;//Flag northwest neighbor
		}
		else if(neighbors[7] > (height-1)*(width-1)){//Check south neighbor
		//else if(neighbors[7] >= height){//Check south neighbor 
			neighbors[7] = -1;
			neighbors[8] = -1;//Flag the southeast neighbor 
			neighbors[6] = -1;//Flag the southwest neighbor
		}
			
		if((p%width)+1 >= width){//Check east neighbor
		//if(neighbors[5] % width == 0){//Check east neighbor
			neighbors[5] = -1;
			neighbors[2] = -1;//Mark northeast neighbor
			neighbors[8] = -1;//Flag the southeast neighbor 
		}
		else if((p%width)-1 < 0){ //Check west neighbor
		//else if(neighbors[3] % width == width - 1){ //Check west neighbor
			neighbors[3] = -1;
			neighbors[0] = -1;//Flag northwest neighbor
			neighbors[6] = -1;//Flag the southwest neighbor
		
		}
		
	
		
		return neighbors;
			
		
		
	}


}
