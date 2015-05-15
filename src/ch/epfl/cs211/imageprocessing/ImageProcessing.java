package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public final class ImageProcessing extends PApplet {
	private static final long serialVersionUID = -1L;

	private Capture cam;

	private boolean withCam = false;
	private Threshold thresh_hbs = new Threshold(this, Threshold.Method.HBS);
	private Threshold thresh_intensity = new Threshold(this,
			Threshold.Method.INTENSITY);
	private Blur blur = new Blur(this);
	private Sobel sobel = new Sobel(this);
	private Hough hough = new Hough(this);

	PImage original = null;
	PImage img = null;

	public void setup() {
		size(1400, 600);

		if (withCam) {
			String[] cameras = Capture.list();
			println(cameras.length);
			if (cameras.length == 0) {
				println("There are no cameras available for capture.");
				exit();
			} else {
				println("Available cameras:");
				for (int i = 0; i < cameras.length; i++) {
					println(cameras[i]);
				}
				cam = new Capture(this, cameras[3]);
				cam.start();
			}
		}

	}

	public void draw() {

		if (withCam) {
			if (cam.available() == true) {
				cam.read();
			}
			original = cam.get();
		} else {
			original = loadImage("D:/Workspace/Info_visuelle/Jeu_realite_augmentee/src/ch/epfl/cs211/ressources/board1.jpg");
		}
		try {
			img = (PImage) original.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}

		// On applique les différents filtres à la suite
		img = thresh_hbs.filter(img);
		img = blur.filter(img);
		img = thresh_intensity.filter(img);
		img = sobel.filter(img);
		hough.displayLines(img);

		image(original, 0, 0);
		image(img, width / 2, 0);
		// getIntersections(h.getBestCandidates());
	}
}