package cs211;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;

@SuppressWarnings("serial")
public class Cylinder extends PApplet {

	public final static float CYLINDER_BASE_RADIUS = 10;
	public final static float CYLINDER_HEIGHT = 20;
	public final static int CYLINDER_RESOLUTION = 20;

	private PGraphics graphic;
	private PShape cylinder = new PShape();

	public Cylinder(PGraphics gameWindow) {
		this.graphic = gameWindow;

		float angle;
		float[] x = new float[CYLINDER_RESOLUTION + 1];
		float[] z = new float[CYLINDER_RESOLUTION + 1];

		// get the x and z position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = ((float) Math.PI * 2 / CYLINDER_RESOLUTION) * i;
			x[i] = (float) Math.sin(angle) * CYLINDER_BASE_RADIUS;
			z[i] = (float) Math.cos(angle) * CYLINDER_BASE_RADIUS;
		}
		cylinder = graphic.createShape();

		drawCap(-Game.PLATE_HEIGHT / 2);
		drawSides(-Game.PLATE_HEIGHT / 2, -Game.PLATE_HEIGHT / 2
				- CYLINDER_HEIGHT);
		drawCap(-Game.PLATE_HEIGHT / 2 - CYLINDER_HEIGHT);
	}

	// draw the top of the cylinder
	private void drawCap(float height) {

		int angle = 360 / CYLINDER_RESOLUTION;

		cylinder.beginShape(TRIANGLE_STRIP);
		// pourtour
		for (int i = 0; i <= CYLINDER_RESOLUTION; i++) {
			float px = cos(radians((int) (i * angle))) * CYLINDER_BASE_RADIUS;
			float pz = sin(radians(i * angle)) * CYLINDER_BASE_RADIUS;
			cylinder.vertex(px, height, pz);
			cylinder.vertex(0, height, 0);
		}
		cylinder.endShape();

	}

	// draw the border of the cylinder
	private void drawSides(float bottomHeight, float topHeight) {
		cylinder.beginShape(TRIANGLE_STRIP);
		int angle = 360 / CYLINDER_RESOLUTION;

		for (int i = 0; i <= CYLINDER_RESOLUTION; i++) {
			float px = cos(radians((int) (i * angle))) * CYLINDER_BASE_RADIUS;
			float pz = sin(radians(i * angle)) * CYLINDER_BASE_RADIUS;

			cylinder.vertex(px, bottomHeight, pz);
			cylinder.vertex(px, topHeight, pz);
		}
		cylinder.endShape();
	}

	public void draw() {
		graphic.shape(cylinder);
	}
}
