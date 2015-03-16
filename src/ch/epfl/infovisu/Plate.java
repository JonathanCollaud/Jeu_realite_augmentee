package ch.epfl.infovisu;

import processing.core.*;
import processing.event.MouseEvent;

@SuppressWarnings("serial")
public class Plate extends PApplet {
	/**
	 * Global parameters
	 */
	private static final float AMBI = 120;
	private static final float BG_COLOR = 255;

	private static final float GRAVITY_CONSTANT = (float) 9.81;

	private static final float MAX_ROTATION = radians(60);

	private static final float CAM_ALTITUDE = 160;
	private static final float PLATE_WIDTH = 200;
	private static final float PLATE_HEIGTH = 20;

	private static final float BALL_SIZE = 16;
	private static final float BALL_MASS = 2;

	/**
	 * Shared var
	 */
	private float rotate_y = 0;
	private float rotation_increment = 0.1f;
	private PVector ballPosition = new PVector(0,
			-(PLATE_HEIGTH / 2 + BALL_SIZE), 0);
	private PVector velocity = new PVector(0, 0, 0);
	private PVector gravityForce = new PVector(0, 0, 0);

	@Override
	public void setup() {
		size(800, 600, P3D);
		noStroke(); // disable the outline
	}

	@Override
	public void draw() {
		pushMatrix();
		// Camera and lighting
		camera(-height / 2, -CAM_ALTITUDE, 0, -PLATE_WIDTH / 6, 0, 0, 0, 1, 0);
		directionalLight(10, 10, 10, 1, -1, -1);
		ambientLight(AMBI, AMBI + 20, AMBI);
		background(BG_COLOR);

		// Plate
		rotateY(rotate_y);

		float rotate_x = map(mouseX, 0, width, MAX_ROTATION, -MAX_ROTATION);
		float rotate_z = map(mouseY, 0, height, MAX_ROTATION, -MAX_ROTATION);
		rotateX(rotate_x);
		rotateZ(rotate_z);

		box(PLATE_WIDTH, PLATE_HEIGTH, PLATE_WIDTH);

		// Gravity
		gravityForce.x = sin(rotate_x) * GRAVITY_CONSTANT;
		gravityForce.y = 0;
		gravityForce.z = sin(rotate_z) * GRAVITY_CONSTANT;

		float normalForce = 1;
		float mu = (float) 0.01;
		float frictionMagnitude = normalForce * mu;

		PVector friction = velocity.get();
		friction.mult(-1);
		friction.normalize();
		friction.mult(frictionMagnitude);

		ballPosition.x += 0.5 * (gravityForce.x + frictionMagnitude) / BALL_MASS
				+ velocity.x;
		ballPosition.y += 0;
		ballPosition.z += 0.5 * (gravityForce.z + frictionMagnitude)
				/ BALL_MASS + velocity.z;

		if (ballPosition.x < -PLATE_WIDTH / 2) {
			ballPosition.x = -PLATE_WIDTH / 2;
		}
		if (ballPosition.x > PLATE_WIDTH / 2) {
			ballPosition.x = PLATE_WIDTH / 2;
		}
		if (ballPosition.z < -PLATE_WIDTH / 2) {
			ballPosition.z = -PLATE_WIDTH / 2;
		}
		if (ballPosition.z > PLATE_WIDTH / 2) {
			ballPosition.z = PLATE_WIDTH / 2;
		}

		// Ball
		translate(ballPosition.x, ballPosition.y, ballPosition.z);
		sphere(BALL_SIZE);
		popMatrix();

		// Printing text
		textSize(15);
		text("rotation : " + Math.round(rotation_increment * 100.0) / 100.0,
				500, 15);
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
		} else { // mouse wheel down
			if (rotation_increment >= 0.02)
				rotation_increment = rotation_increment - 0.01f;
		}
	}
}