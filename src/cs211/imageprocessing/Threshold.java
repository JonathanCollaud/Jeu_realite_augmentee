package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * @author Jonathan Collaud
 * @author Raphaël Dunant
 * @author Thibault Viglino
 *
 * Groupe : AB
 */
public final class Threshold extends Filter {
	private static final float MIN_HUE = 30;
	private static final float MAX_HUE = 140;
	private static final float MIN_BRIGHTNESS = 20;
	private static final float MAX_BRIGHTNESS = 1000;
	private static final float MIN_SATURATION = 80;
	private static final float MAX_SATURATION = 300;
	private static final float MIN_INTENSITY = 50;
	private static final float MAX_INTENSITY = 150;

	private final Method method;

	public enum Method {
		HBS, INTENSITY
	}

	public Threshold(PApplet p, Method method) {
		super(p);
		this.method = method;
	}

	@Override
	public PImage filter(PImage img) {
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

				if (method.equals(Method.HBS)) {
					if (MIN_HUE < pixHue && pixHue < MAX_HUE
							&& MIN_BRIGHTNESS < pixBri
							&& pixBri < MAX_BRIGHTNESS
							&& MIN_SATURATION < pixSat
							&& pixSat < MAX_SATURATION) {
						image.pixels[y * imgW + x] = p.color(255);
					} else {
						image.pixels[y * imgW + x] = p.color(0);
					}
				} else if (method.equals(Method.INTENSITY)) {
					if (MIN_INTENSITY < pixBri && pixBri < MAX_INTENSITY) {
						image.pixels[y * imgW + x] = p.color(255);
					} else {
						image.pixels[y * imgW + x] = p.color(0);
					}
				}
			}
		}

		return image;
	}
}
