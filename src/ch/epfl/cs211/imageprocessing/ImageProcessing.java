package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public class ImageProcessing extends PApplet {
	private static final long serialVersionUID = -1L;
	private final Capture cam;

	public void setup() {
		size(1280, 360);
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

	public void draw() {
		if (cam.available() == true) {
			cam.read();
		}
		PImage img = cam.get();
		// PImage img = loadImage("board1.jpg");
		PImage sobel = sobel(treshold(img));
		image(img, 0, 0);
		image(sobel, 640, 0);
		getIntersections(hough(sobel));
	}
}