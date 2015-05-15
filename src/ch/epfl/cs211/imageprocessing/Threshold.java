package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

public final class Threshold extends Filter {
	private static final float MIN_HUE_TRESHOLD = 100;
	private static final float MAX_HUE_TRESHOLD = 115;
	private static final float MIN_BRIGHTNESS_TRESHOLD = 25;
	private static final float MAX_BRIGHTNESS_TRESHOLD = 230;
	private static final float MIN_SATURATION_TRESHOLD = 25;
	private static final float MAX_SATURATION_TRESHOLD = 230;
	
	private final Method method;
	
	public enum Method {
		HBS, INTENSITY
	}


	public Threshold(PApplet p, Method method) {
		super(p);
		this.method = method;
	}
	
	@Override
	public PImage filter(PImage img){
		int x, y, pixel;
		int imgW = img.width;
		int imgH = img.height;

		PImage image = new PImage(imgW, imgH);

		float pixHue, pixBri, pixSat;

		for (y = 0; y < imgH; y++) {
			for (x = 0; x < imgW; x++) {

				pixel = img.pixels[y * imgW + x];

				pixHue = p.hue(pixel);
				pixBri = p.brightness(pixel);
				pixSat = p.saturation(pixel);

				if (MIN_HUE_TRESHOLD < pixHue && pixHue < MAX_HUE_TRESHOLD
						&& MIN_BRIGHTNESS_TRESHOLD < pixBri
						&& pixBri < MAX_BRIGHTNESS_TRESHOLD
						&& MIN_SATURATION_TRESHOLD < pixSat
						&& pixSat < MAX_SATURATION_TRESHOLD) {
					image.pixels[y * imgW + x] = p.color(255);
				} else {
					image.pixels[y * imgW + x] = p.color(0);
				}
			}
		}
		
		return image;
	}
}
