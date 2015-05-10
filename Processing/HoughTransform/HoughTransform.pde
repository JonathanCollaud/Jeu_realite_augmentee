import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;
import java.util.Comparator;
import java.util.Collections;

public class HoughTransform extends PApplet implements Comparator<Integer> {
  Capture cam;
  int[] accumulator;
  
  public void setup() {
    size(640, 360);
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
      cam = new Capture(this, cameras[3]);
      cam.start();
    }
  }

  public void draw() {
    if (cam.available() == true) {
      cam.read();
    }
    PImage img = cam.get();
    //PImage img = loadImage("board1.jpg");
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
    accumulator = new int[(phiDim + 2) * (rDim + 2)];
    
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
            accR = (int)Math.round((x * Math.cos(accPhi*discStepsPhi) + y * Math.sin(accPhi*discStepsPhi))/discStepsR + (rDim + 1)/2);
            accumulator[(accPhi + 1) * (rDim + 2) + accR]++;
          }
          
        }
      }
    }
    
    int nLines = 4;
    int neighbourhood = 10; // size of the region we search for a local maximum
    int minVotes = 200; // only search around lines with more that this amount of votes // (to be adapted to your image)
    ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
    int i, dR, dPhi, neighbourIndex;
    boolean bestCandidate;
    
    for (accR = 0; accR < rDim; accR++) {
      for (accPhi = 0; accPhi < phiDim; accPhi++) {
        // compute current index in the accumulator
        i = (accPhi + 1) * (rDim + 2) + accR + 1;
        if (accumulator[i] > minVotes) {
          bestCandidate=true;
          // iterate over the neighbourhood
          for(dPhi=-neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++) {
            // check we are not outside the image
            if(accPhi+dPhi < 0 || accPhi+dPhi >= phiDim) continue;
            for(dR=-neighbourhood/2; dR < neighbourhood/2 +1; dR++) {
              // check we are not outside the image
              if(accR+dR < 0 || accR+dR >= rDim) continue;
              neighbourIndex = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
              if(accumulator[i] < accumulator[neighbourIndex]) {
                // the current idx is not a local maximum!
                bestCandidate=false;
                break;
              }
            }
            if(!bestCandidate) break;
          }
          if(bestCandidate) {
            // the current idx *is* a local maximum
            bestCandidates.add(i);
          }
        }
      }
    }

   Collections.sort(bestCandidates, this);
    
    // Plot the lines
    int x0, y0, x1, y1, x2, y2, x3, y3;
    float r, phi; 
    for (i = 0; i < min(nLines, bestCandidates.size()); i++) {
      // first, compute back the (r, phi) polar coordinates:
      accPhi = (int) (bestCandidates.get(i) / (rDim + 2)) - 1;
      accR = bestCandidates.get(i) - (accPhi + 1) * (rDim + 2) - 1;
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
  
  public PImage sobel(PImage img) {
    int x, y, i, j;
    int[] acc = new int[img.width * img.height];
    int imgW = img.width;
    int imgH = img.height;
    
    // Hue detection
    float pixelHue;
    float pixelBrightness;
    float minHueTreshold = 100;
    float maxHueTreshold = 115;
    float minBrightnessTreshold = 25;
    float maxBrightnessTreshold = 230;
    for (y = 0; y < imgH; y++) {
      for (x = 0; x < imgW; x++) {
        pixelHue = hue(img.pixels[y * imgW + x]);
        pixelBrightness = brightness(img.pixels[y * imgW + x]);
        if (minHueTreshold<pixelHue && pixelHue<maxHueTreshold && minBrightnessTreshold<pixelBrightness && pixelBrightness<maxBrightnessTreshold) {
          acc[y * imgW + x] = color(255);
        } else {
          acc[y * imgW + x] = color(0);
        }
      }
    } 
    
    // Sobel
    float[][] hKernel = { 
      {0, 1, 0},
      {0, 0, 0}, 
      {0, -1, 0}};
    float[][] vKernel = { 
      {0, 0, 0},
      {1, 0, -1}, 
      {0, 0, 0}};
    float weight = 1.f;
    float[] buffer = new float[img.width * img.height];
    
    // *************************************
    // Implement here the double convolution
    PImage result = createImage(imgW, imgH, ALPHA);
    
    int kernelHalfSize = hKernel.length/2;
    float maxBrightness=0;
    float sum_h, sum_v, sum;
    for (y = 1; y < imgH - 1; y++) {
      for (x = 1; x < imgW - 1; x++) {
        sum_h = 0;
        sum_v = 0;
        for (i = -kernelHalfSize; i <= kernelHalfSize; i++) {
          for (j = -kernelHalfSize; j <= kernelHalfSize; j++) {
              pixelBrightness = brightness(acc[(y+j) * imgW+ x + i]);
              sum_h += pixelBrightness*hKernel[i+kernelHalfSize][j+kernelHalfSize];
              sum_v += pixelBrightness*vKernel[i+kernelHalfSize][j+kernelHalfSize];
          }
        }
        sum = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
        buffer[y * imgW + x] = sum/weight;
        maxBrightness = (sum>maxBrightness) ? sum : maxBrightness;
      }
    }
    // *************************************
    
    for (y = 0; y < imgH; y++) {
      for (x = 0; x < imgW; x++) {
        if (buffer[y * imgW + x] > maxBrightness * 0.3f) {
          result.pixels[y * imgW + x] = color(255);
        } else {
          result.pixels[y * imgW + x] = color(0);
        }
      }
    }
    return result; 
  }
  
  @Override
  public int compare(Integer l1, Integer l2) { 
    if (accumulator[l1] > accumulator[l2] || (accumulator[l1] == accumulator[l2] && l1 < l2)) return -1; 
    return 1;
  }
}
