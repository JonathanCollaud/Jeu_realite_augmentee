package ch.epfl.infovisu;

import processing.core.*;
import processing.event.MouseEvent;

@SuppressWarnings("serial")
public class Game extends PApplet {
	/**
	 * Global parameters
	 */
	private static final float AMBI = 120;
	private static final float BG_COLOR = 255;

	private static final float MAX_ROTATION = radians(60);

	private static final float CAM_ALTITUDE = 160;
	private static final float PLATE_WIDTH = 200;
	private static final float PLATE_HEIGHT = 5;

	/**
	 * Shared var
	 */
	private float rotate_y = 0;
	private float rotation_increment = 0.1f;
	private float tiltSpeed = 1f;
	private Mover mover;

	@Override
	public void setup() {
		size(800, 600, P3D);
		noStroke(); // disable the outline
		mover = new Mover(PLATE_WIDTH, PLATE_HEIGHT, this);
	}

	@Override
	public void draw() {
		pushMatrix();

		// Camera and lighting
		camera(-height / 2, -CAM_ALTITUDE, 0, -PLATE_WIDTH / 6, 0, 0, 0, 1, 0);
		directionalLight(10, 10, 10, 1, -1, -1);
		ambientLight(AMBI, AMBI + 20, AMBI);
		background(BG_COLOR);

		// Plate rotation
		rotateY(rotate_y);

		float rotate_x = map(mouseX, 0, width, MAX_ROTATION, -MAX_ROTATION);
		float rotate_z = map(mouseY, 0, height, MAX_ROTATION, -MAX_ROTATION);
		rotateX(rotate_x);
		rotateZ(rotate_z);

		box(PLATE_WIDTH, PLATE_HEIGHT, PLATE_WIDTH);

		// Ball
		mover.update(rotate_z, rotate_x);
		mover.display();
		mover.checkEdges();

		popMatrix();

		// Printing text
		textSize(15);
		text("rotation : " + Math.round(rotation_increment * 100.0) / 100.0,
				500, 15);
		text("tilt speed : " + tiltSpeed, 500, 35);
	}

	/*
	 * Interactions
	 */
	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == LEFT) {
				rotate_y += rotation_increment;
			} else if (keyCode == RIGHT) {
				rotate_y -= rotation_increment;
			}
		}
	}

	@Override
	public void mouseWheel(MouseEvent e) {
		if (e.getCount() < 0) { // mouse wheel up
			if (rotation_increment <= 0.25)
				rotation_increment = rotation_increment + 0.01f;
			if (tiltSpeed <= 1.5f)
				tiltSpeed = tiltSpeed + 0.1f;
		} else { // mouse wheel down
			if (rotation_increment >= 0.2)
				rotation_increment = rotation_increment - 0.01f;
			if (tiltSpeed >= 0.2f)
				tiltSpeed = tiltSpeed - 0.1f;
		}
	}
}
