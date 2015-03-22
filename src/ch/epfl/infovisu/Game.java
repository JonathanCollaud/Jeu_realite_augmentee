package ch.epfl.infovisu;

import processing.core.*;
import processing.event.MouseEvent;

@SuppressWarnings("serial")
public class Game extends PApplet {
	/**
	 * Global parameters
	 */
	private static final float AMBI = 220;
	private static final float BG_COLOR = 255;

	private static final float MAX_ROTATION = radians(60);

	private static final float PLATE_WIDTH = 300;
	private static final float PLATE_HEIGHT = 5;

	private final float BASE_CAM_ROTATION = -height / 2;
	private final float BASE_CAM_ALTITUDE = -160;
	private final float BASE_CAM_POSITION = -PLATE_WIDTH;

	/**
	 * Shared var
	 */
	private boolean paused = false;
	private float rotate_x = 0;
	private float rotate_y = 0;
	private float rotate_z = 0;
	private float cam_rot = BASE_CAM_ROTATION;
	private float cam_pos = BASE_CAM_POSITION;
	private float cam_alt = BASE_CAM_ALTITUDE;
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
		// Camera and lighting
		displayCamera();
		directionalLight(10, 10, 10, 1, -1, -1);
		ambientLight(AMBI, AMBI, AMBI);
		background(BG_COLOR);

		// Gère le déplacement de la balle et de la plate
		playGame();

		// Information text
		textSize(15);
		text("rotation : " + Math.round(rotation_increment * 100.0) / 100.0,
				500, 15);
		text("tilt speed : " + tiltSpeed, 500, 35);
	}

	// Déplace la caméra si le jeu est mis sur pause avec shift
	private void displayCamera() {
		if (!paused) {
			if (cam_pos != BASE_CAM_POSITION) {
				cam_pos = getCloser(cam_pos, BASE_CAM_POSITION);
			}
			if (cam_alt != BASE_CAM_ALTITUDE) {
				cam_alt = getCloser(cam_alt, BASE_CAM_ALTITUDE);
			}
			if (cam_rot != BASE_CAM_ROTATION) {
				cam_rot = getCloser(cam_rot, BASE_CAM_ROTATION);
			}
		} else {
			if (cam_pos != 0) {
				cam_pos = getCloser(cam_pos, 0);
			}
			if (cam_alt != -height / 2) {
				cam_alt = getCloser(cam_alt, - height / 2);
			}
			if (cam_rot != 1) {
				cam_rot = getCloser(cam_rot, 1);
			}
		}
		// Display camera
		camera(cam_pos, cam_alt, 0, cam_rot, 0, 0, 0, 1, 0);
	}

	// Fonction qui rapproche une variable d’une cible en divisant la distance
	// par deux.
	private float getCloser(float var, float target) {
		float delta = var - target;
		if (abs(delta) <= 10) {
			return target;
		} else {
			var -= delta / 2;
			return var;
		}
	}

	private void playGame() {
		pushMatrix();

		// Animated stuff
		if (!paused) {
			// Plate rotation
			rotateY(rotate_y);

			rotate_x = map(mouseX, 0, width, MAX_ROTATION, -MAX_ROTATION);
			rotate_z = map(mouseY, 0, height, MAX_ROTATION, -MAX_ROTATION);

			// Ball
			mover.checkEdges();
			mover.update(rotate_z, rotate_x);
		} else {
			rotate_x = 0;
			rotate_z = 0;
		}

		rotateX(rotate_x);
		rotateZ(rotate_z);

		// Display plate
		fill(color(167, 219, 216));
		box(PLATE_WIDTH, PLATE_HEIGHT, PLATE_WIDTH);

		// Display ball
		fill(color(224, 228, 204));
		mover.display();

		popMatrix();
	}

	/*
	 * Interactions
	 */
	@Override
	public void keyPressed() {
		if (key == CODED) {
			// Plate rotation
			if (keyCode == LEFT) {
				rotate_y += rotation_increment;
			} else if (keyCode == RIGHT) {
				rotate_y -= rotation_increment;
			}
			// Insert cylinder mode
			else if (keyCode == SHIFT) {
				paused = true;
			}
		}
	}

	// Quit insert cylinder mode
	@Override
	public void keyReleased() {
		if (key == CODED && keyCode == SHIFT) {
			paused = false;
		}
	}

	// Plate tilt
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
