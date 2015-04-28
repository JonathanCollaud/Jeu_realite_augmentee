PImage img;
public void setup() {
  size(1600, 600);
  img = loadImage("board1.jpg");
}

public void draw() {
  PImage result = sobel(img);
  image(img, 0, 0);
  image(result, 800, 0);
  hough(result);
}

public void hough(PImage edgeImg){
  float discretizationStepsPhi = 0.06f;
  float discretizationStepsR = 2.5f;
  
  // dimensions of the accumulator
  int phiDim = (int) (Math.PI / discretizationStepsPhi);
  int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
  
  // our accumulator (with a 1 pix margin around)
  int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
  
  // Fill the accumulator: on edge points (ie, white pixels of the edge
  // image), store all possible (r, phi) pairs describing lines going
  // through the point.
  for (int y = 0; y < edgeImg.height; y++) {
    for (int x = 0; x < edgeImg.width; x++) {
      // Are we on an edge?
      if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
        
        // ...determine here all the lines (r, phi) passing through
        // pixel (x,y), convert (r,phi) to coordinates in the
        // accumulator, and increment accordingly the accumulator.
        
        for (int accPhi = 0; accPhi < phiDim; accPhi++){
          int r = (int)Math.round((x * Math.cos(accPhi*discretizationStepsPhi) + y * Math.sin(accPhi*discretizationStepsPhi)) / discretizationStepsR);
          r += 0.5 * (rDim - 1);    
          accumulator[(accPhi + 1) * (rDim + 2) + r + 1]++;
        }
      }
    }
  }
  
  PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
  for (int i = 0; i < accumulator.length; i++) {
    houghImg.pixels[i] = color(min(255, accumulator[i]));
  }
  houghImg.resize(800, 600);
  houghImg.updatePixels();
  
  // Plot the lines
  for (int idx = 0; idx < accumulator.length; idx++) {
    if (accumulator[idx] > 300) {
      // first, compute back the (r, phi) polar coordinates:
      int accPhi = (int) (idx / (rDim + 2)) - 1;
      int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
      float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
      float phi = accPhi * discretizationStepsPhi;
      
      // Cartesian equation of a line: y = ax + b
      // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
      // => y = 0 : x = r / cos(phi)
      // => x = 0 : y = r / sin(phi)
      // compute the intersection of this line with the 4 borders of // the image
      int x0 = 0;
      int y0 = (int) (r / sin(phi));
      int x1 = (int) (r / cos(phi));
      int y1 = 0;
      int x2 = edgeImg.width;
      int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
      int y3 = edgeImg.width;
      int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
      
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

public PImage convolute(PImage img) {
  float[][] kernel = {
    { 0, 0, 0 },
    { 0, 2, 0 },
    { 0, 0, 0 }
  };
  float weight = 1.f;
  PImage result = createImage(img.width, img.height, ALPHA);
  int kernelHalfSize = kernel.length/2;
  for (int y = 1; y < img.height - 1; y++) {
    for (int x = 1; x < img.width - 1; x++) {
      int sum = 0;
      for (int i = -kernelHalfSize; i < kernelHalfSize; i++) {
        for (int j = -kernelHalfSize; j < kernelHalfSize; j++) {
            int pixel = img.pixels[(y+j) * img.width + x + i];
            sum += brightness(pixel)*kernel[i+kernelHalfSize][j+kernelHalfSize];
        }
      }
      result.pixels[y * img.width + x] = color((int)(sum/weight));
    }
  }
  return result;
}

public PImage gaussian(PImage img) {
  float[][] gaussianKernel = { 
    {9, 12, 9},
    {12, 15, 12}, 
    {9, 12, 9}
  };
  PImage result = createImage(img.width, img.height, ALPHA);
  int weight = 99;
  int kernelHalfSize = gaussianKernel.length/2;
  for (int y = 1; y < img.height - 1; y++) {
    for (int x = 1; x < img.width - 1; x++) {
      int sum = 0;
      for (int i = -kernelHalfSize; i < kernelHalfSize; i++) {
        for (int j = -kernelHalfSize; j < kernelHalfSize; j++) {
            int pixel = img.pixels[(y+j) * img.width + x + i];
            sum += brightness(pixel)*gaussianKernel[i+kernelHalfSize][j+kernelHalfSize];
        }
      }
      result.pixels[y * img.width + x] = color(sum/weight);
    }
  }
return result; 
}

public PImage sobel(PImage img) {
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
  for (int i = 0; i < img.width * img.height; i++) {
    result.pixels[i] = color(0);
  }
  float max=0;
  float[] buffer = new float[img.width * img.height];
  
  float minTreshold = 105;
  float maxTreshold = 135;
  for (int y = 0; y < img.height; y++) {
    for (int x = 0; x < img.width; x++) {
      float pixelHue = hue(img.pixels[y * img.width + x]);
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
  for (int y = 1; y < preResult.height - 1; y++) {
    for (int x = 1; x < preResult.width - 1; x++) {
      float sum_h = 0;
      float sum_v = 0;
      for (int i = -kernelHalfSize; i <= kernelHalfSize; i++) {
        for (int j = -kernelHalfSize; j <= kernelHalfSize; j++) {
            float pixelBrightness = brightness(preResult.pixels[(y+j) * preResult.width + x + i]);
            sum_h += pixelBrightness*hKernel[i+kernelHalfSize][j+kernelHalfSize];
            sum_v += pixelBrightness*vKernel[i+kernelHalfSize][j+kernelHalfSize];
        }
      }
      float sum = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
      buffer[y * preResult.width + x] = sum/weight;
      max = (sum>max) ? sum : max;
    }
  }
  // *************************************
  
  for (int y = 0; y < preResult.height; y++) {
    for (int x = 0; x < preResult.width; x++) {
      if (buffer[y * preResult.width + x] > max * 0.3f) {
        result.pixels[y * preResult.width + x] = color(255);
      } else {
        result.pixels[y * preResult.width + x] = color(0);
      }
    }
  }
return result; 
}
