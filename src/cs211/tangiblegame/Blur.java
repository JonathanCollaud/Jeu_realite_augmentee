package cs211.tangiblegame;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * @author Jonathan Collaud
 * @author Raphaël Dunant
 * @author Thibault Viglino
 * 
 *         Groupe : AB
 */
public final class Blur extends Filter {
	private float[][] KERNEL = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };

	private final float weight = 99f;

	public Blur(PApplet p) {
		super(p);
	}

	@Override
	public PImage filter(final PImage img) {
		int x, y, i, j, pixel, sum;
		int imgW = img.width;
		int imgH = img.height;

		PImage result = new PImage(imgW, imgH);

		int kernelHalfSize = KERNEL.length / 2;

		for (y = 1; y < imgH - 1; y++) {
			for (x = 1; x < imgW - 1; x++) {

				sum = 0;
				pixel = img.pixels[y * imgW + x];

				for (i = -kernelHalfSize; i <= kernelHalfSize; i++) {
					for (j = -kernelHalfSize; j <= kernelHalfSize; j++) {
						pixel = img.pixels[(y + j) * img.width + x + i];
						sum += p.brightness(pixel)
								* KERNEL[i + kernelHalfSize][j + kernelHalfSize];
					}
				}
				result.pixels[y * img.width + x] = p.color(sum / weight);
			}
		}

		return result;
	}

}
