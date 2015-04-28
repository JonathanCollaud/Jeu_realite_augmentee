import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public class HoughTransform extends PApplet {
  Capture cam;
  PImage img;
  
  public void setup() {
    size(640, 480);
    String[] cameras = Capture.list();
    println(cameras.length);
    if (cameras.length == 0) {
      println("There are no cameras available for capture.");
      exit();
    } else {
      println("Available cameras:");
      for (int i = 0; i < cameras.length; i++) {
        println(cameras[i]);
      }
      cam = new Capture(this, cameras[0]);
      cam.start();
    }
  }

  public void draw() {
    if (cam.available() == true) {
      cam.read();
    }
    img = cam.get();
    image(img, 0, 0);
    hough(sobel(img));
  }
  
  public void hough(PImage edgeImg){
    float discStepsPhi = 0.06f;
    float discStepsR = 2.5f;
    
    // dimensions of the accumulator
    int phiDim = (int) (Math.PI / discStepsPhi);
    int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discStepsR);
    
    // our accumulator (with a 1 pix margin around)
    int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
    
    // Fill the accumulator: on edge points (ie, white pixels of the edge
    // image), store all possible (r, phi) pairs describing lines going
    // through the point.
    int x, y, accR, accPhi;
    for (y = 0; y < edgeImg.height; y++) {
      for (x = 0; x < edgeImg.width; x++) {
        // Are we on an edge?
        if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
          
          // ...determine here all the lines (r, phi) passing through
          // pixel (x,y), convert (r,phi) to coordinates in the
          // accumulator, and increment accordingly the accumulator.
          
          for (accPhi = 0; accPhi < phiDim; accPhi++){
            accR = (int)Math.round((x * Math.cos(accPhi*discStepsPhi) + y * Math.sin(accPhi*discStepsPhi))/discStepsR + (rDim - 1)/2);
            accumulator[(accPhi + 1) * (rDim + 2) + accR + 1]++;
          }
        }
      }
    }
    
    PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
    for (int i = 0; i < accumulator.length; i++) {
      houghImg.pixels[i] = color(min(255, accumulator[i]));
    }
    houghImg.updatePixels();
    
    // Plot the lines
    int x0, y0, x1, y1, x2, y2, x3, y3;
    float r, phi; 
    for (int idx = 0; idx < accumulator.length; idx++) {
      if (accumulator[idx] > 300) {
        // first, compute back the (r, phi) polar coordinates:
        accPhi = (int) (idx / (rDim + 2)) - 1;
        accR = idx - (accPhi + 1) * (rDim + 2) - 1;
        r = (accR - (rDim - 1) * 0.5f) * discStepsR;
        phi = accPhi * discStepsPhi;
        
        // Cartesian equation of a line: y = ax + b
        // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
        // => y = 0 : x = r / cos(phi)
        // => x = 0 : y = r / sin(phi)
        // compute the intersection of this line with the 4 borders of // the image
        x0 = 0;
        y0 = (int) (r / sin(phi));
        x1 = (int) (r / cos(phi));
        y1 = 0;
        x2 = edgeImg.width;
        y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
        y3 = edgeImg.width;
        x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
        
        // Finally, plot the lines
        stroke(204,102,0);
        if (y0 > 0) {
          if (x1 > 0)
            line(x0, y0, x1, y1);
          else if (y2 > 0)
            line(x0, y0, x2, y2);
          else
            line(x0, y0, x3, y3);
        } else {
          if (x1 > 0) {
            if (y2 > 0)
              line(x1, y1, x2, y2);
            else
              line(x1, y1, x3, y3);
          } else
            line(x2, y2, x3, y3);
        }
      }
    }
    
    image(houghImg, 800, 0);
  }
  
  public PImage sobel(PImage img) {
    int x, y, i, j;
    float[][] hKernel = { 
      {0, 1, 0},
      {0, 0, 0}, 
      {0, -1, 0}};
    float[][] vKernel = { 
      {0, 0, 0},
      {1, 0, -1}, 
      {0, 0, 0}};
    float weight = 1.f;
    PImage preResult = createImage(img.width, img.height, ALPHA);
    PImage result = createImage(img.width, img.height, ALPHA);
    // clear the image
    for (i = 0; i < img.width * img.height; i++) {
      result.pixels[i] = color(0);
    }
    float[] buffer = new float[img.width * img.height];
    
    float minTreshold = 105;
    float maxTreshold = 135;
    
    float pixelHue;
    for (y = 0; y < img.height; y++) {
      for (x = 0; x < img.width; x++) {
        pixelHue = hue(img.pixels[y * img.width + x]);
        if (pixelHue>minTreshold && pixelHue<maxTreshold) {
          preResult.pixels[y * img.width + x] = color(255);
        } else {
          preResult.pixels[y * img.width + x] = color(0);
        }
      }
    }
    
    // *************************************
    // Implement here the double convolution
    int kernelHalfSize = hKernel.length/2;
    int pRW = preResult.width;
    int pRH = preResult.height;
    float maxBrightness=0;
    float sum_h, sum_v, sum, pixelBrightness;
    for (y = 1; y < preResult.height - 1; y++) {
      for (x = 1; x < preResult.width - 1; x++) {
        sum_h = 0;
        sum_v = 0;
        for (i = -kernelHalfSize; i <= kernelHalfSize; i++) {
          for (j = -kernelHalfSize; j <= kernelHalfSize; j++) {
              pixelBrightness = brightness(preResult.pixels[(y+j) * pRW + x + i]);
              sum_h += pixelBrightness*hKernel[i+kernelHalfSize][j+kernelHalfSize];
              sum_v += pixelBrightness*vKernel[i+kernelHalfSize][j+kernelHalfSize];
          }
        }
        sum = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
        buffer[y * pRW + x] = sum/weight;
        maxBrightness = (sum>maxBrightness) ? sum : maxBrightness;
      }
    }
    // *************************************
    
    for (y = 0; y < pRH; y++) {
      for (x = 0; x < pRW; x++) {
        if (buffer[y * pRW + x] > maxBrightness * 0.3f) {
          result.pixels[y * pRW + x] = color(255);
        } else {
          result.pixels[y * pRW + x] = color(0);
        }
      }
    }
    return result; 
  }
}
