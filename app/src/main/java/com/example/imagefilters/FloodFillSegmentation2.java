/**
 * This class is in charge of performing the 
 * Flood Filled Segmentation algorithm on an image
 * @author Jonathan Quijas
 * 
 */
package com.example.imagefilters;

import java.util.ArrayList;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * In charge of performing Flood fill segmentation algorithm
 * 
 * @author Jonathan Quijas
 *
 */
public class FloodFillSegmentation2 {

	public static final double THRESHOLD_LOW = .2;
	public static final double  THRESHOLD_MEDIUM = .3;
	public static final double THRESHOLD_HIGH = .4;
	
	public static final int REGION_SMALL = 10;
	public static final int REGION_MEDIUM = 20;
	public static final int REGION_LARGE = 30;
	
	/**
	 * 
	 * @param pixels	An integer array containing color values for every image pixel 
	 * @param threshold	The threshold used to denote similarity between regions
	 * @param width		The image's width
	 * @param height	The image's height
	 * @return			An integer array containing the new mean region values
	 */
	public static Bitmap segmentImage(Bitmap bitmap, double threshold, int minRegionSize){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		Bitmap newBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); /*R is a vector of regions*/
		int[][] R = new int [height][width];
		
		/* meanRegion at index i contains the mean color intensity at region i*/
		ArrayList<Integer> meanRegion = new ArrayList<Integer>();
		
		/*sizeRegion at index i contains the size of region i*/
		ArrayList<Integer> sizeRegion = new ArrayList<Integer>();
		
		Stack <Integer> rStack = new Stack <Integer> ();
		Stack <Integer> cStack = new Stack <Integer> ();
	
		int region = 0; //The region counter r
		int r = 0;
		int c = 0; 
		
		int[] p = new int[2]; //The first pixel 'p' without an assigned region 'r'
				
		while(R[r][c] == 0){//While there are pixels that don't belong to any region
			
			region++; 
			R[r][c] = region; //Assign region number to R[p]
			
			
			int sumRed = Color.red(bitmap.getPixel(r, c));//The sum of red pixel values for the current region r
			int sumGreen = Color.green(bitmap.getPixel(r, c));//The sum of gree pixel values for the current region r
			int sumBlue = Color.blue(bitmap.getPixel(r, c)); //The sum of blue pixel values for the current region r
			
			int n = 1; //The number of pixels in the current region r
			
			rStack.push(r);
			cStack.push(c);
			
			while(!rStack.isEmpty()){
				r = rStack.pop();
				c = cStack.pop();
				
				//For each neighbor s of q
				for(int i = Math.max(r-1, 0); i < Math.min(r+1, width); i++)
					for(int j = Math.max(c-1, 0); j < Math.min(c+1, height); j++){
						if(R[i][j] == 0){

							double deltaRed = Color.red(bitmap.getPixel(i, j)) - (sumRed/n);
							double deltaGreen = Color.green(bitmap.getPixel(i, j)) - (sumGreen/n);
							double deltaBlue = Color.blue(bitmap.getPixel(i, j)) - (sumBlue/n);
	
							double pixelDifference = getPixelValue(deltaRed, deltaGreen, deltaBlue);
													
							if( pixelDifference < threshold){
							
								R[i][j] = region;
								
								sumRed += Color.red(bitmap.getPixel(i, j));
								sumGreen += Color.green(bitmap.getPixel(i, j));
								sumBlue += Color.blue(bitmap.getPixel(i, j));
								n++; //There is one more pixel in current region r
								
								
								rStack.push(i);
								cStack.push(j);
							}
						
					}
					
				}  
			}
			
			
			int color = Color.argb(0xFF,sumRed/n, sumGreen/n, sumBlue/n);
			
			meanRegion.add(color);
			sizeRegion.add(n);

			p = getNextPixel(R); //Find next pixel with unassigned region
			
			r = p[0];
			c = p[1];
		}
		
		ArrayList<Integer> newMeanRegion = removeSmallRegions(sizeRegion, meanRegion, minRegionSize);
		
		for(int i = 0; i < R.length; i++){
			for(int j = 0; j < R[i].length; j++){
				R[i][j] = newMeanRegion.get(R[i][j] - 1); //Map all mean region colors to the corresponding pixels		
			}
			newBitMap.setPixels(R[i], 0, width, i, 0, width, height);
		}
		
		
		return newBitMap;
		
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
	
	private static int [] getNextPixel(int[][]R){
		int [] p = new int[2];
		int length = R.length;
		
		for(int i = 0; i < length; i++)
			for(int j = 0; j < R[i].length; j++)
			if(R[i][j] == 0){
				p[0]=i;
				p[1]=j;
				return p;
			}
		
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
		else if(neighbors[7] > height){//Check south neighbor 
			neighbors[7] = -1;
			neighbors[8] = -1;//Flag the southeast neighbor 
			neighbors[6] = -1;//Flag the southwest neighbor
		}
			
		if(neighbors[5] % width == 0){//Check east neighbor
			neighbors[5] = -1;
			neighbors[2] = - 1;//Mark northeast neighbor
			neighbors[8] = -1;//Flag the southeast neighbor 
		}
		else if(neighbors[3] % width == width - 1){ //Check west neighbor
			neighbors[3] = -1;
			neighbors[0] = - 1;//Flag northwest neighbor
			neighbors[6] = -1;//Flag the southwest neighbor
		
		}
		
	
		
		return neighbors;
			
		
		
	}
}
