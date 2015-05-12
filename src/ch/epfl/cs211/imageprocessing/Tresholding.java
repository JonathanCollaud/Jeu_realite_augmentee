package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

public class Tresholding extends PApplet {
	private static final long serialVersionUID = 1L;
	private static float MIN_HUE_TRESHOLD = 100;
	private static float MAX_HUE_TRESHOLD = 115;
	private static float MIN_BRIGHTNESS_TRESHOLD = 25;
	private static float MAX_BRIGHTNESS_TRESHOLD = 230;
	private static float MIN_SATURATION_TRESHOLD = 25;
	private static float MAX_SATURATION_TRESHOLD = 230;
	PImage image;

	public Tresholding(PImage img) {
		int x, y, pixel;
		int imgW = img.width;
		int imgH = img.height;

		image = new PImage(imgW, imgH);

		float pixHue, pixBri, pixSat;

		for (y = 0; y < imgH; y++) {
			for (x = 0; x < imgW; x++) {

				pixel = img.pixels[y * imgW + x];
				pixHue = hue(pixel);
				pixBri = brightness(pixel);
				pixSat = saturation(pixel);

				if (MIN_HUE_TRESHOLD < pixHue && pixHue < MAX_HUE_TRESHOLD
						&& MIN_BRIGHTNESS_TRESHOLD < pixBri
						&& pixBri < MAX_BRIGHTNESS_TRESHOLD
						&& MIN_SATURATION_TRESHOLD < pixSat
						&& pixSat < MAX_SATURATION_TRESHOLD) {
					image.pixels[y * imgW + x] = color(255);
				} else {
					image.pixels[y * imgW + x] = color(0);
				}
			}
		}
	}
	
	public PImage img(){
		return image;
	}
}
