package com.example.imagefilters;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.jtransforms.fft.DoubleFFT_2D;

/**
 * Created by Jona Q on 1/9/2016.
 */
public class L0Gradient {


    public static int [] applyFilter(Bitmap bitmap) {
        //  Get image dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //  Allocate S matrix, initialize as input image
        double [][][] S  = new double [3][height][width];
        double [][][] FS = new double [3][height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                S[0][i][j] = ((double)Color.red(bitmap.getPixel(j,i)))/255;
                S[1][i][j] = ((double)Color.green(bitmap.getPixel(j, i)))/255;
                S[2][i][j] = ((double)Color.blue(bitmap.getPixel(j,i)))/255;
            }
        }

        //  Useful variables
        double kappa = 2.0;
        double lambda = 2e-2;
        double beta = 2*lambda;
        double betamax = 1e5;

        //  Point-spread functions
        double [][] fx = {{1, -1}};
        double [][] fy = {{1}, {-1}};

        //  Compute Optical Transfer function from point-spread functions
        DoubleFFT_2D fft = new DoubleFFT_2D(height, width);


        fft.realForward(fx);
        fft.realForward(fy);

        //  Nominator terms
        double [][][] Normin2 = new double [3][height][width];
        double [][][] Normin1 = new double [3][height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                Normin1[0][i][j] = S[0][i][j];
                Normin1[1][i][j] = S[1][i][j];
                Normin1[2][i][j] = S[2][i][j];
            }
        }

        fft.realForward(Normin1[0]);
        fft.realForward(Normin1[1]);
        fft.realForward(Normin1[2]);

        //  Denominator terms
        double [][][] Denormin2 = new double [3][height][width];
        double [][][] Denormin = new double [3][height][width];

        //  Gradients
        double [][][] h = new double[3][height][width];
        double [][][] v = new double[3][height][width];

        //  Compute second term in denominator, which is not modified
        for(int k = 0; k < 3; k++){
            for(int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Denormin2[k][i][j] = Math.pow(Math.abs(fx[i][j]),2) + Math.pow(Math.abs(fy[i][j]),2);
                }
            }
        }


        while(beta < betamax){

            //  Compute denominator term
            for (int k = 0; k < 3; k++) {
                for(int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        Denormin[k][i][j] = 1*beta*Denormin2[k][i][j];
                    }
                }
            }

            //  Compute horizontal gradient
            for(int k = 0; k < 3; k++){
                for(int i = 0; i < height; i++) {
                    for (int j = 0; j < width-1; j++) {
                        h[k][i][j] = S[k][i][j+1] - S[k][i][j];
                    }
                }
            }
            for(int k = 0; k < 3; k++) {
                for (int i = 0; i < height; i++) {
                    h[k][i][width-1] = S[k][i][0] - S[k][i][width-1];
                }
            }

            //  Compute vertical gradient
            for(int k = 0; k < 3; k++){
                for(int i = 0; i < height-1; i++) {
                    for (int j = 0; j < width; j++) {
                        v[k][i][j] = S[k][i+1][j] - S[k][i][j];
                    }
                }
            }
            for(int k = 0; k < 3; k++) {
                for (int j = 0; j < width; j++) {
                    v[k][height - 1][j] = S[k][0][j] - S[k][height - 1][j];
                }
            }

            //  Index where to make h and v zero
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    double sum = 0;
                    for(int k = 0; k < 3; k++){
                        sum += Math.pow(h[k][i][j], 2) + Math.pow(v[k][i][j], 2);
                    }
                    if(sum < lambda/beta) {
                        for (int k = 0; k < 3; k++) {
                            h[k][i][j] = 0;
                            v[k][i][j] = 0;
                        }
                    }
                }
            }

            //  Compute horizontal gradient
            for(int k = 0; k < 3; k++){
                for(int i = 0; i < height; i++) {
                    for (int j = 1; j < width-1; j++) {
                        Normin2[k][i][j] = -1*(h[k][i][j+1] - h[k][i][j]);
                    }
                }
            }
            for(int k = 0; k < 3; k++) {
                for (int i = 0; i < height; i++) {
                    Normin2[k][i][0] = h[k][i][width-1] - h[k][i][0];
                }
            }
            //  Compute vertical gradient
            for(int k = 0; k < 3; k++){
                for(int i = 1; i < height-1; i++) {
                    for (int j = 0; j < width; j++) {
                        Normin2[k][i][j] = Normin2[k][i][j] + -1*(v[k][i+1][j] - v[k][i][j]);
                    }
                }
            }
            for(int k = 0; k < 3; k++) {
                for (int j = 0; j < width; j++) {
                    Normin2[k][0][j] = Normin2[k][0][j] + (v[k][height-1][j] - v[k][0][j]);
                }
            }

            //  Compute FFT of second nominator term
            for(int k = 0; k < 3; k++) {
                fft.realForward(Normin2[k]);
            }

            for(int k = 0; k < 3; k++){
                for(int i = 0; i < height; i++){
                    for(int j = 0; j < width; j++){
                        FS[k][i][j] = Normin1[k][i][j] + beta * Normin2[k][i][j] / Denormin[k][i][j];
                    }
                }
            }
            for(int k = 0; k < 3; k++) {
                fft.realInverse(FS[k], false);
            }

            //  Update S
            for(int k = 0; k < 3; k++){
                for(int i = 0; i < height; i++){
                    for(int j = 0; j < width; j++){
                        S[k][i][j] = FS[k][i][j];
                    }
                }
            }

            //  Update iteration parameter
            beta = beta*kappa;

        }// End of while-loop

        int [][] intS = new int[height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                intS[i][j] = Color.argb(0xFF, (int)(S[0][i][j]*255), (int)(S[1][i][j]*255), (int)(S[2][i][j]*255));
            }
        }
        int [] s = new int[height*width];
        for(int i = 0; i < height; i++) {
            System.arraycopy(intS[i], 0, s, i * width, width);
        }

        return s;
    }
}
