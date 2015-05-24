package cs211;

import java.util.List;

import processing.core.*;

public class Mover {

	public static final float GRAVITY_CONSTANT = 10f;
	public static final float BALL_MASS = 1f;
	public static final float BALL_SIZE = 5f;

	// Ball positioning
	private float normalForce = 1f;
	private float mu = 1f;
	private float frictionMagnitude = normalForce * mu * BALL_MASS;

	private PVector ballPosition = new PVector(0, -BALL_SIZE
			- Game.PLATE_HEIGHT / 2, 0);
	private PVector ballVelocity = new PVector(0, 0, 0);
	private PVector ballAcceleration = new PVector(0, 0, 0);
	private PVector friction = new PVector(0, 0, 0);
	private Game applet;

	public Mover(Game applet) {
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
		if (ballPosition.x <= -Game.PLATE_WIDTH / 2 + BALL_SIZE
				|| ballPosition.x >= Game.PLATE_WIDTH / 2 - BALL_SIZE) {
			ballPosition.x = Math.signum(ballPosition.x)
					* (Game.PLATE_WIDTH / 2 - BALL_SIZE);
			ballVelocity.x *= -1;
		}
		if (ballPosition.z <= -Game.PLATE_WIDTH / 2 + BALL_SIZE
				|| ballPosition.z >= Game.PLATE_WIDTH / 2 - BALL_SIZE) {
			ballPosition.z = Math.signum(ballPosition.z)
					* (Game.PLATE_WIDTH / 2 - BALL_SIZE);
			ballVelocity.z *= -1;
		}
	}

	public void checkCylinderCollision() {
		List<PVector> bumps = applet.getBumps();
		for (PVector bump : bumps) {
			if (bump.dist(ballPosition) <= BALL_SIZE
					+ Cylinder.CYLINDER_BASE_RADIUS) {
				PVector normal = PVector.sub(ballPosition, bump);
				normal.normalize();

				Float dotProd = PVector.dot(ballVelocity, normal) * 2;
				PVector multVect = PVector.mult(normal, dotProd);
				ballVelocity = PVector.sub(ballVelocity, multVect);
			}
		}
	}

	public float x() {
		return ballPosition.x;
	}

	public float z() {
		return ballPosition.z;
	}
}
