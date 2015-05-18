package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

/**
 * @author Jonathan Collaud
 * @author Raphael Dunant
 * @author Thibault Viglino
 *
 *         Groupe : AB
 */
public final class ImageProcessing extends PApplet {
	private static final long serialVersionUID = -1L;
	private static final String LOAD_IMAGE_ADDRESS = "board1.jpg";
	private static final boolean WITH_WEBCAM = true;
	
	private Capture cam;

	private Threshold thresh_hbs = new Threshold(this, Threshold.Method.HBS);
	private Threshold thresh_intensity = new Threshold(this, Threshold.Method.INTENSITY);
	private Blur blur = new Blur(this);
	private Sobel sobel = new Sobel(this);
	private Hough hough = new Hough(this);

	PImage original = null;

	public void setup() {
		size(2000, 600);

		if (WITH_WEBCAM) {
			String[] cameras = Capture.list();
			if (cameras.length == 0) {
				println("There are no cameras available for capture.");
				exit();
			} else {
				println("Available cameras:");
				for (int i = 0; i < cameras.length; i++) {
					println(cameras[i]);
					println("dhsajl");
				}
				cam = new Capture(this, cameras[3]);
				System.out.println(cam.width +" ; "+ cam.height);
				cam.start();
			}
		}

	}

	public void draw() {

		if (WITH_WEBCAM) {
			if (cam.available() == true) {
				cam.read();
			}
			original = cam.get();
			original.resize(cam.width, cam.height);
		} else {
			original = loadImage(LOAD_IMAGE_ADDRESS);
		}

		PImage modifiedImg;

		// On applique les différents filtres à la suite
		modifiedImg = thresh_hbs.filter(original);
		modifiedImg = blur.filter(modifiedImg);
		modifiedImg = thresh_intensity.filter(modifiedImg);
		modifiedImg = sobel.filter(modifiedImg);
		PImage houghed = hough.computeLines(modifiedImg);

		// Original avec lignes
		image(original, 0, 0);
		hough.displayLinesAndGetCorners(modifiedImg);

		// Hough
		System.out.println(original.width +" ; "+ original.height);
		houghed.resize(original.width, original.height);
		image(houghed, original.width, 0);

		// Sobel
		image(modifiedImg, original.width + houghed.width, 0);

		// getIntersections(h.getBestCandidates());
	}
}