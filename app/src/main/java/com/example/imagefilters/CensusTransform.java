package com.example.imagefilters;

import java.util.ArrayList;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by Jona Q on 1/5/2016.
 */
public class CensusTransform {

    public static final int neighborhoodSize = 9;

    public static int [] applyCensusTransform(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int C[][] = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                if (i == 0 || j == 0 || i == height - 1 || j == width - 1) {
                    C[i][j] = 0;
                } else {
                    int redMean = 0;
                    int greenMean = 0;
                    int blueMean = 0;
                    int meanColor = Color.argb(0xFF,0,0,0);
                    for (int r = i - 1; r <= i + 1; r++) {
                        for (int c = j - 1; c <= j + 1; c++) {
                            /*redMean += Color.red(bitmap.getPixel(c, r));
                            greenMean += Color.green(bitmap.getPixel(c, r));
                            blueMean += Color.blue(bitmap.getPixel(c, r));*/
                            meanColor = meanColor | bitmap.getPixel(c, r) << 24 | bitmap.getPixel(c, r) << 16 |
                                    bitmap.getPixel(c, r) << 8 | bitmap.getPixel(c, r) ;
                        }
                    }

                    /*redMean   = (int)Math.floor((double)redMean / (double)neighborhoodSize);
                    greenMean = (int)Math.floor((double)greenMean / (double)neighborhoodSize);
                    blueMean  = (int)Math.floor((double)blueMean / (double)neighborhoodSize);*/
                    meanColor = meanColor / neighborhoodSize;

                    int index = 0;
                    int redC = 0;
                    int greenC = 0;
                    int blueC = 0;

                    for (int r = i - 1; r <= i + 1; r++) {
                        for (int c = j - 1; c <= j + 1; c++) {
                            /*if (Color.red(bitmap.getPixel(c, r)) >= redMean)
                                redC += 2 ^ index;
                            if (Color.green(bitmap.getPixel(c, r)) >= greenMean)
                                greenC += 2 ^ index;
                            if (Color.blue(bitmap.getPixel(c, r)) >= blueMean)
                                blueC += 2 ^ index;
                            */
                            if (bitmap.getPixel(c, r) >= meanColor)
                                meanColor += Math.pow(index, 2);
                            index++;

                        }
                    }

                    C[i][j] = meanColor;//Color.argb(0xFF, redC, greenC, blueC);

                }

            }
        }
        int [] c = new int[width*height];
        for(int i = 0; i < height; i++)
            System.arraycopy(C[i], 0, c, i*width, width);
        return c;
    }


}
