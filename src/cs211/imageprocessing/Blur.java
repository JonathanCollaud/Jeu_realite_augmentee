package cs211.imageprocessing;

import static processing.core.PConstants.ALPHA;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * @author Jonathan Collaud
 * @author Raphaël Dunant
 * @author Thibault Viglino
 *
 * Groupe : AB
 */
public final class Blur extends Filter {
	private float[][] KERNEL = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };
	private final int weight = 500;
	
	public Blur(PApplet p) {
		super(p);
	}

	@Override
	public PImage filter(final PImage img) {
		PImage result = p.createImage(img.width, img.height, ALPHA);
		
		int kernelHalfSize = KERNEL.length / 2;
		
		for (int y = 1; y < img.height - 1; y++) {
			for (int x = 1; x < img.width - 1; x++) {
				int sum = 0;
				for (int i = -kernelHalfSize; i < kernelHalfSize; i++) {
					for (int j = -kernelHalfSize; j < kernelHalfSize; j++) {
						int pixel = img.pixels[(y + j) * img.width + x + i];
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
