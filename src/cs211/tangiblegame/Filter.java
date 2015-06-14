package cs211.tangiblegame;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * @author Jonathan Collaud
 * @author Raphaël Dunant
 * @author Thibault Viglino
 *
 * Groupe : AB
 */
public abstract class Filter {
	
	PApplet p;
	
	public Filter(PApplet p) {
		this.p = p;
	}
	
	// Retourne l’image modifiée
	abstract public PImage filter(final PImage img);
}
