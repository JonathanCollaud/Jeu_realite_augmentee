package ch.epfl.infovisu;

import processing.core.*;

public class Cylinder {

	private final static float CYLINDER_BASE_SIZE = 50;
	private final static float CYLINDER_HEIGHT = 50;
	private final static int CYLINDER_RESOLUTION = 40;
	
	private PShape openCylinder = new PShape();
	private PApplet applet;

	public Cylinder(PApplet applet) {
		this(CYLINDER_HEIGHT, CYLINDER_BASE_SIZE, applet);
	}
	
	public Cylinder(float height, float baseSize, PApplet applet) {	
		this.applet = applet;
		
		float angle;
		float[] x = new float[CYLINDER_RESOLUTION + 1];
		float[] y = new float[CYLINDER_RESOLUTION + 1];
		
		// get the x and y position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = ((float)Math.PI * 2 / CYLINDER_RESOLUTION) * i;
			x[i] = (float)Math.sin(angle) * baseSize;
			y[i] = (float)Math.cos(angle) * baseSize;
		}
		openCylinder = applet.createShape();
		openCylinder.beginShape(applet.QUAD_STRIP);
		// draw the border of the cylinder
		for (int i = 0; i < x.length; i++) {
			openCylinder.vertex(x[i], y[i], 0);
			openCylinder.vertex(x[i], y[i], height);
		}
		openCylinder.endShape();
	}

	void draw() {		
		applet.translate(applet.mouseX, applet.mouseY, 0);
		applet.shape(openCylinder);
	}
}
