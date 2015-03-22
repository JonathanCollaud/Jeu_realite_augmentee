package ch.epfl.infovisu;

import processing.core.*;

public class Mover {

	private static final float GRAVITY_CONSTANT = 10f;
	private static final float BALL_MASS = 1f;
	private static final float BALL_SIZE = 8f;

	// Ball positioning
	private float normalForce = 1f;
	private float mu = 1f;
	private float frictionMagnitude = normalForce * mu * BALL_MASS;
	private float plate_width = 1;
	private float plate_height = 1;

	private PVector ballPosition = new PVector(0, -BALL_SIZE - plate_height, 0);
	private PVector ballVelocity = new PVector(0, 0, 0);
	private PVector ballAcceleration = new PVector(0, 0, 0);
	private PVector friction = new PVector(0, 0, 0);
	private PApplet applet;

	public Mover(float plate_width, float plate_height, PApplet applet) {
		this.plate_width = plate_width;
		this.plate_height = plate_height;
		this.applet = applet;
	}

	public void update(float rotate_z, float rotate_x) {
		// Friction
		friction = ballVelocity.get();
		friction.mult(-1);
		friction.normalize();
		friction.mult(frictionMagnitude);

		// Newton
		PVector forces = new PVector(Math.round(Math.sin(rotate_z)
				* GRAVITY_CONSTANT * BALL_MASS + friction.x), 0,
				Math.round(-Math.sin(rotate_x) * GRAVITY_CONSTANT * BALL_MASS
						+ friction.z));
		forces.div(BALL_MASS);
		ballAcceleration.set(forces);
		ballVelocity.add(ballAcceleration);
		ballPosition.add(ballVelocity);
	}

	public void display() {
		applet.translate(ballPosition.x, ballPosition.y, ballPosition.z);		
		applet.sphere(BALL_SIZE);
	}

	public void checkEdges() {
		if (ballPosition.x <= -plate_width / 2 + BALL_SIZE
				|| ballPosition.x >= plate_width / 2 - BALL_SIZE) {
			ballPosition.x = Math.signum(ballPosition.x) * (plate_width / 2 - BALL_SIZE);
			ballVelocity.x *= -1;
		}
		if (ballPosition.z <= -plate_width / 2 + BALL_SIZE
				|| ballPosition.z >= plate_width / 2 - BALL_SIZE) {
			ballPosition.z = Math.signum(ballPosition.z) * (plate_width / 2 - BALL_SIZE);
			ballVelocity.z *= -1;
		}
	}
}
