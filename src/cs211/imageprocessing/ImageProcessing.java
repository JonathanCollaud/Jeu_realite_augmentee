package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;
import java.util.List;

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
	private static final boolean WITH_WEBCAM = false;
	
	private Capture cam;

	private Threshold thresh_hbs = new Threshold(this, Threshold.Method.HBS);
	private Threshold thresh_intensity = new Threshold(this, Threshold.Method.INTENSITY);
	private Blur blur = new Blur(this);
	private Sobel sobel = new Sobel(this);
	private Hough hough = new Hough(this);

	private PImage original = null;

	public void setup() {
		if (WITH_WEBCAM) {
			String[] cameras = Capture.list();
			if (cameras.length == 0) {
				println("There are no cameras available for capture.");
				exit();
			} else {
				println("Available cameras:");
				for (int i = 0; i < cameras.length; i++) {
					println(cameras[i]);
				}
				cam = new Capture(this, cameras[3]);
//				System.out.println(cam.width +" ; "+ cam.height);
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
//			original.resize(1 + cam.width, 1 + cam.height);
		} else {
			original = loadImage(LOAD_IMAGE_ADDRESS);
		}
		size((int)(1+2.5 * original.width), 1+original.height);
		
		PImage modifiedImg;

		// On applique les différents filtres à la suite
		modifiedImg = thresh_hbs.filter(original);
		modifiedImg = blur.filter(modifiedImg);
		modifiedImg = thresh_intensity.filter(modifiedImg);
		modifiedImg = sobel.filter(modifiedImg);
		PImage houghed = hough.computeLines(modifiedImg);

		// Original avec lignes
		image(original, 0, 0);
		List<PVector> corners = hough.displayLinesAndGetCorners(modifiedImg);

		// Hough
		houghed.resize(1 + original.width/2, original.height);
		image(houghed, 1 + original.width, 0);

		// Sobel
		image(modifiedImg, original.width + houghed.width, 0);
		
		TwoDThreeD mapping = new TwoDThreeD(original.width, original.height);
		PVector anglesInRadians = mapping.get3DRotations(corners);
		
		System.out.println(Math.toDegrees(anglesInRadians.x) + ", " +
				Math.toDegrees(anglesInRadians.y) + ", " +
				Math.toDegrees(anglesInRadians.z));
	}
}