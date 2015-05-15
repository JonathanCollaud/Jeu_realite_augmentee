package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public final class ImageProcessing extends PApplet {
	private static final long serialVersionUID = -1L;

	private Capture cam;

	private boolean withCam = false;

	public void setup() {
		size(1920, 360);

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
		PImage img;

		if (withCam) {
			if (cam.available() == true) {
				cam.read();
			}
			img = cam.get();
		} else {
			img = loadImage("D:/Workspace/Info_visuelle/Jeu_realite_augmentee/src/ch/epfl/cs211/ressources/board1.jpg");
		}

		Filter t_hbs = new Thresholding(img, Thresholding.Method.HBS);
		Filter b = new Blur(t_hbs.img());
		Filter t_int = new Thresholding(b.img(), Thresholding.Method.INTENSITY);
		Filter s = new Sobel(t_int.img());
		Hough h = new Hough(s.img());
		Edges e = new Edges(h.getBestCandidates());

		image(img, 0, 0);
		image(s.img(), 1280, 0);
		// getIntersections(h.getBestCandidates());
	}
}