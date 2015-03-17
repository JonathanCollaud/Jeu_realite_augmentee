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
	private static final float PLATE_HEIGTH = 5;

	private static final float BALL_SIZE = 4;
	private static final float BALL_MASS = 1;

	/**
	 * Shared var
	 */
	private float rotate_y = 0;
	private float rotation_increment = 0.1f;
	private PVector ballPosition = new PVector(0,
			-(PLATE_HEIGTH / 2 + BALL_SIZE), 0);
	private PVector ballVelocity = new PVector(0, 0, 0);
	private PVector ballAcceleration = new PVector(0, 0, 0);

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

		// Ball positioning
		float normalForce = 1;
		float mu = (float) 0.01;
		float frictionMagnitude = normalForce * mu;

		PVector friction = ballVelocity.get();
		friction.mult(-1);
		friction.normalize();
		friction.mult(frictionMagnitude);

		PVector forces = new PVector(sin(rotate_z) * GRAVITY_CONSTANT
				+ friction.x, 0, -sin(rotate_x) * GRAVITY_CONSTANT + friction.z);
		forces.div(BALL_MASS);
		ballAcceleration.set(forces);
		ballVelocity.add(ballAcceleration);
		ballPosition.add(ballVelocity);

		// Border bouncing
		if (ballPosition.x <= -PLATE_WIDTH / 2
				|| ballPosition.x >= PLATE_WIDTH / 2) {
			ballVelocity.x = -ballVelocity.x;
		}
		if (ballPosition.z <= -PLATE_WIDTH / 2
				|| ballPosition.z >= PLATE_WIDTH / 2) {
			ballVelocity.z = -ballVelocity.z;
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