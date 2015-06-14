package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * @author Jonathan Collaud
 * @author Raphaël Dunant
 * @author Thibault Viglino
 * 
 *         Groupe : AB
 */
public final class Threshold extends Filter {
	// Lego plate official color: H = 119, B = 59, S = 51
	// <<<<<<< Updated upstream
	// private static final float MIN_HUE = 105;
	// private static final float MAX_HUE = 133;
	// private static final float MIN_BRIGHTNESS = 75;
	// private static final float MAX_BRIGHTNESS = 155;
	// private static final float MIN_SATURATION = 80;
	// private static final float MAX_SATURATION = 255;
	// =======
	private static final float MIN_HUE = 80;
	private static final float MAX_HUE = 160;
	private static final float MIN_BRIGHTNESS = 60;
	private static final float MAX_BRIGHTNESS = 150;
	private static final float MIN_SATURATION = 100;
	private static final float MAX_SATURATION = 255;

	private static final float INTENSITY_THRESHOLD = 128;

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
					if (pixBri < INTENSITY_THRESHOLD) {
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
