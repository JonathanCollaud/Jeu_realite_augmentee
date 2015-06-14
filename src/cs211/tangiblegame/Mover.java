package cs211.tangiblegame;

import java.util.List;

import processing.core.PGraphics;
import processing.core.PVector;

public class Mover {

	public static final float GRAVITY_CONSTANT = 4f;
	public static final float BALL_MASS = 10f;
	public static final float BALL_SIZE = 5f;

	// Ball positioning
	private float normalForce = 1f;
	private float mu = 5f;
	private float frictionMagnitude = normalForce * mu;

	private PVector ballPosition = new PVector(0, -BALL_SIZE
			- TangibleGame.PLATE_HEIGHT / 2, 0);
	private PVector ballVelocity = new PVector(0, 0, 0);
	private PVector ballAcceleration = new PVector(0, 0, 0);
	private PVector friction = new PVector(0, 0, 0);
	private TangibleGame applet;

	public Mover(TangibleGame applet) {
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

	public void display(PGraphics gameWindow) {
		gameWindow.translate(ballPosition.x, ballPosition.y, ballPosition.z);
		gameWindow.sphere(BALL_SIZE);
	}

	public void checkEdges() {
		if (ballPosition.x <= -TangibleGame.PLATE_WIDTH / 2 + BALL_SIZE
				|| ballPosition.x >= TangibleGame.PLATE_WIDTH / 2 - BALL_SIZE) {
			ballPosition.x = Math.signum(ballPosition.x)
					* (TangibleGame.PLATE_WIDTH / 2 - BALL_SIZE);
			ballVelocity.x *= -1;
			TangibleGame.addScore(-100 * ballVelocity());
		}
		if (ballPosition.z <= -TangibleGame.PLATE_WIDTH / 2 + BALL_SIZE
				|| ballPosition.z >= TangibleGame.PLATE_WIDTH / 2 - BALL_SIZE) {
			ballPosition.z = Math.signum(ballPosition.z)
					* (TangibleGame.PLATE_WIDTH / 2 - BALL_SIZE);
			ballVelocity.z *= -1;
			TangibleGame.addScore(-1 * ballVelocity());
		}
	}

	public void checkCylinderCollision() {
		List<PVector> bumps = applet.getBumps();
		PVector normal, multVect;
		PVector touched = null;
		float dx, dz;
		float dotProd;
		for (PVector bump : bumps) {
			dx = ballPosition.x - bump.x;
			dz = ballPosition.z - bump.z;
			if (Math.sqrt(dx * dx + dz * dz) <= BALL_SIZE
					+ Cylinder.CYLINDER_BASE_RADIUS) {
				normal = PVector.sub(ballPosition, bump);
				normal.y += BALL_SIZE * 2;
				normal.normalize();

				dotProd = PVector.dot(ballVelocity, normal);
				multVect = PVector.mult(normal, 2 * dotProd);

				ballVelocity = PVector.sub(ballVelocity, multVect);
				touched = bump;
				TangibleGame.addScore(100 * ballVelocity());
			}
		}
		bumps.remove(touched);
	}

	public float x() {
		return ballPosition.x;
	}

	public float z() {
		return ballPosition.z;
	}

	public float ballVelocity() {
		return (float) Math.round(Math.sqrt(ballVelocity.x * ballVelocity.x
				+ ballVelocity.y * ballVelocity.y + ballVelocity.z
				* ballVelocity.z) * 100) / 100;
	}
}
