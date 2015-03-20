package ch.epfl.infovisu;

import processing.core.*;

public class Mover {

	private static final float GRAVITY_CONSTANT = 9.81f;
	private static final float BALL_MASS = 1;
	private static final float BALL_SIZE = 4;

	// Ball positioning
	private float normalForce = 1;
	private float mu = 0.01f;
	private float frictionMagnitude = normalForce * mu * BALL_MASS;
	private float plate_width = 50;
	private float plate_height = 50;

	private PVector ballPosition = new PVector(0,
			-(plate_height / 2 + BALL_SIZE), 0);
	private PVector ballVelocity = new PVector(0, 0, 0);
	private PVector ballAcceleration = new PVector(0, 0, 0);
	private PVector friction = new PVector(0, 0, 0);
	private PApplet applet;

	Mover(float plate_width, float plate_height, PApplet applet) {
		this.plate_width = plate_width;
		this.plate_height = plate_height;
		this.applet = applet;
	}

	void update(float rotate_z, float rotate_x) {
		ballPosition.add(ballVelocity);

		PVector forces = new PVector(Math.round(Math.sin(rotate_z) * GRAVITY_CONSTANT
				* BALL_MASS + friction.x), 0, Math.round(-Math.sin(rotate_x)
				* GRAVITY_CONSTANT * BALL_MASS + friction.z));
		forces.div(BALL_MASS);
		ballAcceleration.set(forces);
		ballVelocity.add(ballAcceleration);
		ballPosition.add(ballVelocity);

		// Friction
		friction = ballVelocity.get();
		friction.mult(-1);
		friction.normalize();
		friction.mult(frictionMagnitude);
	}

	void display() {
		applet.translate(ballPosition.x, ballPosition.y, ballPosition.z);
		applet.sphere(BALL_SIZE);		
	}

	void checkEdges() {
		if (ballPosition.x <= -plate_width / 2
				|| ballPosition.x >= plate_width / 2) {
			ballPosition.x = Math.signum(ballPosition.x) * plate_width / 2;
			ballVelocity.x *= -1;
		}
		if (ballPosition.z <= -plate_width / 2
				|| ballPosition.z >= plate_width / 2) {
			ballPosition.z = Math.signum(ballPosition.z) * plate_width / 2;
			ballVelocity.z *= -1;
		}
	}
}
