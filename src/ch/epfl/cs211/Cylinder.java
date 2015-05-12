package ch.epfl.cs211;

import processing.core.*;

public class Cylinder {

	private final static float CYLINDER_BASE_RADIUS = 100;
	private final static float CYLINDER_HEIGHT = 100;
	private final static int CYLINDER_RESOLUTION = 20;

	private float height;
	private PApplet applet;
	private PShape cylinder = new PShape();
	private float baseRadius;

	public Cylinder(PApplet applet) {
		this(CYLINDER_HEIGHT, CYLINDER_BASE_RADIUS, applet);
	}

	public Cylinder(float height, float baseRadius, PApplet applet) {
		this.baseRadius = baseRadius;
		this.applet = applet;
		this.height = -height;

		float angle;
		float[] x = new float[CYLINDER_RESOLUTION + 1];
		float[] z = new float[CYLINDER_RESOLUTION + 1];

		// get the x and z position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = ((float) Math.PI * 2 / CYLINDER_RESOLUTION) * i;
			x[i] = (float) Math.sin(angle) * baseRadius;
			z[i] = (float) Math.cos(angle) * baseRadius;
		}
		cylinder = applet.createShape();

		//drawCap(x, z, 0);
		drawSides(x, z);
		drawCap(x, z, -height);

	}

	// draw the top of the cylinder
	@SuppressWarnings("static-access")
	private void drawCap(float[] x, float[] z, float height) {
		cylinder.beginShape(applet.TRIANGLE_FAN);
		
		// point central
		cylinder.vertex(0, height, 0);
		
		// pourtour
		for (int i = 0; i < x.length; i++) {
			cylinder.vertex(x[i], height, z[i]);
		}
		
		cylinder.endShape(applet.CLOSE);
	}

	// draw the border of the cylinder
	@SuppressWarnings("static-access")
	private void drawSides(float[] x, float[] z) {
		cylinder.beginShape(applet.QUAD_STRIP);
		
		for (int i = 0; i < x.length; i++) {
			cylinder.vertex(x[i], 0, z[i]);
			cylinder.vertex(x[i], height, z[i]);
		}
		
		cylinder.endShape(applet.CLOSE);
	}

	public void draw() {
		applet.shape(cylinder);
	}

	public float getSize() {
		return baseRadius;
	}	
}
