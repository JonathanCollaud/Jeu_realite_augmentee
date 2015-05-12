package ch.epfl.cs211.imageprocessing;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

public class ImageProcessing extends PApplet {
	private static final long serialVersionUID = -1L;
	
	private Capture cam;

	public void setup() {
		size(1920, 360);
		
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
		// PImage img = loadImage("src.Processing.ImageProcessing.board1.jpg");
		
		Tresholding t = new Tresholding(img);
		Sobel s = new Sobel(t.img());
		Hough h = new Hough(s.img());
		Edges e = new Edges(h.getBestCandidates());
		
		image(img, 0, 0);
		image(s.img(), 1280, 0);
		//getIntersections(h.getBestCandidates());
	}	
}