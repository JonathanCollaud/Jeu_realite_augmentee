package ch.epfl.cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;

public abstract class Filter {
	
	PApplet p;
	
	public Filter(PApplet p) {
		this.p = p;
	}
	
	// Retourne l’image modifiée
	abstract public PImage filter(PImage img);
}
