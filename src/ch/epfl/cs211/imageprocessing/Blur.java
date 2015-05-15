package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

public final class Blur extends PApplet implements Filter {
	private static final long serialVersionUID = 1L;
	private PImage img;

	public Blur(PImage img) {
		// TODO Auto-generated constructor stub
		this.img = img;
	}

	@Override
	public PImage img() {
		// TODO Auto-generated method stub
		return img;
	}

}
