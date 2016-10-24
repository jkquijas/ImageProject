/**
 * This class is in charge of performing the 
 * Flood Filled Segmentation algorithm on an image
 * @author Jonathan Quijas
 * 
 */
package com.example.imagefilters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import android.graphics.Color;
import android.util.Log;

/**
 * In charge of performing Flood fill segmentation algorithm
 * 
 * @author Jonathan Quijas
 *
 */
public class FloodFillSegmentation {

	private class Pixel{
		private int index;
		private Pixel left;
		private Pixel right;
		Pixel(int i){
			index = i;
		}
		public int getIndex(){
			return index;
		}
		public void setLeft(Pixel l){
			left = l;
		}
		public void setRight(Pixel r){
			right = r;
		}
		public Pixel getLeft(){
			return left;
		}
		public Pixel getRight(){
			return right;
		}
	}
	private class PixelList{
		private Pixel head;
		private Pixel tail;
		private int size;
		PixelList(Pixel h, Pixel t){
			head = h;
			tail = t;

			head.setRight(tail);
			tail.setLeft(head);

			size = 2;
		}

		boolean isEmpty(){
			return size == 0;
		}

		int pop(){
			return head.getIndex();
		}
		void insertPixel(Pixel p){
			tail.setRight(p);
			p.setLeft(tail);
			tail = p;
			size++;
		}
		void deletePixel(Pixel p){
			if(head.getIndex() == p.getIndex()){
				head = head.getRight();
			}
			else if(tail.getIndex() == p.getIndex()){
				tail = tail.getLeft();
			}
			else{
				p.getLeft().setRight(p.getRight());
				p.getRight().setLeft(p.getLeft());
			}

			size--;
		}
	}

	private class PixelTracker{
		private HashMap<Integer, Pixel> hm;
		private PixelList pixelList;
		PixelTracker(int n){

			hm = new HashMap<Integer, Pixel>();
			Pixel p1 = new Pixel(0);
			hm.put(0, p1);

			Pixel p2 = new Pixel(1);
			hm.put(1, p2);

			pixelList = new PixelList(hm.get(0), hm.get(1));
			for(int i = 2; i < n; i++){
				Pixel pixel = new Pixel(i);
				hm.put(i, pixel);
				pixelList.insertPixel(hm.get(i));
			}


		}
		void removePixel(int i){
			pixelList.deletePixel(hm.get(i));
		}
		int popUnassignedPixel(){
			if(pixelList.isEmpty()) {
				return 0;
			}
			return pixelList.pop();
		}

	}

	public static final double THRESHOLD_LOW = .15;
	public static final double  THRESHOLD_MEDIUM = .3;
	public static final double THRESHOLD_HIGH = .4;
	
	public static final int REGION_SMALL = 10;
	public static final int REGION_MEDIUM = 20;
	public static final int REGION_LARGE = 30;

	public static PixelTracker pixelTracker;
	
	/**
	 * 
	 * @param pixels	An integer array containing color values for every image pixel 
	 * @param threshold	The threshold used to denote similarity between regions
	 * @param width		The image's width
	 * @param height	The image's height
	 * @return			An integer array containing the new mean region values
	 */
	public static int [] segmentImage(int[]pixels, double threshold, int minRegionSize, int width, int height){

		int numPixels = width * height;

		//	R is an array of regions
		int [] R = new int[numPixels];

		pixelTracker = new FloodFillSegmentation().new PixelTracker(numPixels);


		//	meanRegion at index i contains the mean color intensity at region i
		ArrayList<Integer> meanRegion = new ArrayList<Integer>();
		
		//	sizeRegion at index i contains the size of region i
		ArrayList<Integer> sizeRegion = new ArrayList<Integer>();
		
		Stack <Integer> stack = new Stack <Integer> ();

		//	The region counter r
		int region = 0;
		//	The first pixel 'p' without an assigned region 'r'
		int p = 0;

		//	While there are pixels that don't belong to any region
		while(R[p] == 0){
			//	Assign region number to R[p]
			region++; 
			R[p] = region;
			pixelTracker.removePixel(p);

			
			
			int sumRed = Color.red(pixels[p]);//The sum of red pixel values for the current region r
			int sumGreen = Color.green(pixels[p]);//The sum of green pixel values for the current region r
			int sumBlue = Color.blue(pixels[p]); //The sum of blue pixel values for the current region r
			
			int n = 1; //The number of pixels in the current region r
			
			stack.push(p);
			while(!stack.isEmpty()){
				int q = stack.pop();
				int[]neighbors = getNeighbors(width, height, q);
				
				//For each neighbor s of q
				for(int neighbor = 0; neighbor < neighbors.length; neighbor++){
					//	Avoid edges
					if(neighbors[neighbor] == -1)
						continue;
						
					int s = neighbors[neighbor]; 
					//	If q's neighbor s has no region assigned to it
					if(R[s] == 0){
						
						double deltaRed = Color.red(pixels[s]) - (sumRed/n);
						double deltaGreen = Color.green(pixels[s]) - (sumGreen/n);
						double deltaBlue = Color.blue(pixels[s]) - (sumBlue/n);

						double pixelDifference = getPixelValue(deltaRed, deltaGreen, deltaBlue);
						//	If q's neighbor s is "close enough" in color, assign to region
						if( pixelDifference < threshold){
						
							R[s] = region;
							pixelTracker.removePixel(s);

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

			//p = getNextPixel(R); //Find next pixel with unassigned region
			p = getNextPixel(); //Find next pixel with unassigned region
		}
		
		ArrayList<Integer> newMeanRegion = removeSmallRegions(sizeRegion, meanRegion, minRegionSize);

		//	Map all mean region colors to the corresponding pixels
		for(int i = 0; i < R.length; i++)
			R[i] = newMeanRegion.get(R[i] - 1);
		
		return R;
		
	}
	


	private static ArrayList<Integer> removeSmallRegions(ArrayList<Integer> sizeRegion, ArrayList<Integer> meanRegion, int minRegionSize){

		ArrayList<Integer> smallRegions = new ArrayList<Integer>();
		ArrayList<Integer> smallMeanRegions = new ArrayList<Integer>();
		ArrayList<Integer> bigMeanRegions = new ArrayList<Integer>();
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		for(int i =0; i < sizeRegion.size(); i++) {
			if (sizeRegion.get(i) < minRegionSize) {
				smallMeanRegions.add(meanRegion.get(i));
				smallRegions.add(i);
			} else {
				bigMeanRegions.add(meanRegion.get(i));
				hm.put(meanRegion.get(i), i);
			}

		}

		Collections.sort(bigMeanRegions);

		//	Check every regions' size
		for(int i = 0; i < smallRegions.size(); i++){
			boolean found = false;
			int lo = 0;
			int hi =  bigMeanRegions.size()-1;
			int mid = -2;
			int newMid = -1;

			while(!found){
				mid = (hi+lo)/2;
				//Log.d("Removing small regions", "Region "+i+", mid = " + mid + ", newMid = "+newMid);

				if(mid == newMid)
					found = true;
				if(smallMeanRegions.get(i) < bigMeanRegions.get(mid)){
					hi = mid;
				}
				else{
					lo = mid;
				}
				newMid = (hi+lo)/2;
			}
			meanRegion.set(smallRegions.get(i), bigMeanRegions.get(mid));


			int newRed = (Color.red(meanRegion.get(smallRegions.get(i))) + Color.red(bigMeanRegions.get(mid)))/2;
			int newGreen = (Color.green(meanRegion.get(smallRegions.get(i))) + Color.green(bigMeanRegions.get(mid)))/2;
			int newBlue = (Color.blue(meanRegion.get(smallRegions.get(i))) + Color.blue(bigMeanRegions.get(mid)))/2;

			int newColor = Color.argb(0xFF, newRed, newGreen, newBlue);

			meanRegion.set(smallRegions.get(i), newColor);
			meanRegion.set(hm.get(bigMeanRegions.get(mid)), newColor);


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

	private static int getNextPixel(){
		return pixelTracker.popUnassignedPixel();
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
			neighbors[5] = -1;
			neighbors[2] = -1;//Mark northeast neighbor
			neighbors[8] = -1;//Flag the southeast neighbor 
		}
		else if((p%width)-1 < 0){ //Check west neighbor
			neighbors[3] = -1;
			neighbors[0] = -1;//Flag northwest neighbor
			neighbors[6] = -1;//Flag the southwest neighbor
		}

		return neighbors;
		
	}
}
