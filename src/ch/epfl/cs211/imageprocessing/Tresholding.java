package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

public class Tresholding extends PApplet{
	private static final long serialVersionUID = 1L;
	private static float MIN_HUE_TRESHOLD = 100;
	private static float MAX_HUE_TRESHOLD = 115;
	private static float MIN_BRIGHTNESS_TRESHOLD = 25;
	private static float MAX_BRIGHTNESS_TRESHOLD = 230;
	
	
	public int[] treshold(PImage img){
		int x, y;
		int[] acc = new int[img.width * img.height];
		int imgW = img.width;
		int imgH = img.height;
		
		float pixelHue;
		float pixelBrightness;
		for (y = 0; y < imgH; y++) {
			for (x = 0; x < imgW; x++) {
				pixelHue = hue(img.pixels[y * imgW + x]);
				pixelBrightness = brightness(img.pixels[y * imgW + x]);
				if (MIN_HUE_TRESHOLD < pixelHue && pixelHue < MAX_HUE_TRESHOLD
						&& MIN_BRIGHTNESS_TRESHOLD < pixelBrightness
						&& pixelBrightness < MAX_BRIGHTNESS_TRESHOLD) {
					acc[y * imgW + x] = color(255);
				} else {
					acc[y * imgW + x] = color(0);
				}
			}
		}
		
		return acc;
	}
}
