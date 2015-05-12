package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

public class Sobel extends PApplet {
	private static final long serialVersionUID = 1L;

	public PImage sobel(PImage img) {
		int x, y, i, j;
		int imgW = img.width;
		int imgH = img.height;
		float[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		float weight = 1.f;
		float[] buffer = new float[img.width * img.height];

		// *************************************
		// Implement here the double convolution
		PImage result = createImage(imgW, imgH, ALPHA);
		int kernelHalfSize = hKernel.length / 2;
		float pixelBrightness;
		float maxBrightness = 0;
		float sum_h, sum_v, sum;
		for (y = 1; y < imgH - 1; y++) {
			for (x = 1; x < imgW - 1; x++) {
				sum_h = 0;
				sum_v = 0;
				for (i = -kernelHalfSize; i <= kernelHalfSize; i++) {
					for (j = -kernelHalfSize; j <= kernelHalfSize; j++) {
						pixelBrightness = brightness(img.pixels[(y + j) * imgW + x + i]);
						sum_h += pixelBrightness
								* hKernel[i + kernelHalfSize][j
										+ kernelHalfSize];
						sum_v += pixelBrightness
								* vKernel[i + kernelHalfSize][j
										+ kernelHalfSize];
					}
				}
				sum = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
				buffer[y * imgW + x] = sum / weight;
				maxBrightness = (sum > maxBrightness) ? sum : maxBrightness;
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
}
