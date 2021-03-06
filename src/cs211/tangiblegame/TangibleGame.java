package cs211.tangiblegame;

import java.util.ArrayList;

import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.video.Capture;
import processing.video.Movie;

/*
            _                             
   _____  _| |_ _ __ ___ _ __ ___   ___   
  / _ \ \/ / __| '__/ _ \ '_ ` _ \ / _ \  
 |  __/>  <| |_| | |  __/ | | | | |  __/  
  \___/_/\_\\__|_|  \___|_| |_| |_|\___|  
  _                 _                     
 | |__   __ _ _   _| |__   __ _ _   _ ___ 
 | '_ \ / _` | | | | '_ \ / _` | | | / __|
 | |_) | (_| | |_| | | | | (_| | |_| \__ \
 |_.__/ \__,_|\__,_|_| |_|\__,_|\__,_|___/

                     ~~~

  A minimal-art oriented video game with astonishing 
  simplicity and sleek interactions.

  @author Jonathan Collaud
  @author Raphaël Dunant
  @author Thibault Viglino

  Groupe AB

 */

@SuppressWarnings("serial")
public class TangibleGame extends PApplet {

	/**
	 * R�glages important pour les tests
	 */
	private static final String LOAD_VIDEO_ADDRESS = "/Applications/Eclipse/Workspace/ProjetInfoVisuelle/src/cs211/ressources/testvideo.mp4";
	private static final boolean GAME_MODE_TANGIBLE = true;
	private static final boolean WITH_WEBCAM = false;

	/**
	 * R�glages webcam
	 */
	private PVector rotation = new PVector(0, 0, 0);

	private Capture webcam;
	private Movie fakecam;

	/**
	 * Global parameters
	 */
	private static final int WINDOW_WIDTH = 1000;
	private static final int GAME_WINDOW_HEIGHT = 700;
	private static final int BOTTOM_RECT_HEIGHT = 100;
	private static final int SMALL_VIDEO_WIDTH = 160;
	private static final int SMALL_VIDEO_HEIGHT = 120;

	public static final float AMBI = 220f;
	public static final float BG_COLOR = 255f;

	public static final float MAX_ROTATION = radians(60);

	public static final float PLATE_WIDTH = 600f;
	public static final float PLATE_HEIGHT = 10f;

	public final float PAUSE_HEIGHT = -PLATE_WIDTH;

	public final float BASE_CAM_ROTATION = 0;
	public final float BASE_CAM_ALTITUDE = -PLATE_WIDTH;
	public final float BASE_CAM_POSITION = -PLATE_WIDTH;

	public final ImageProcessing IMAGE = new ImageProcessing();

	/**
	 * Filtres
	 */

	private Threshold thresh_hbs = new Threshold(this, Threshold.Method.HBS);
	private Threshold thresh_intensity = new Threshold(this,
			Threshold.Method.INTENSITY);
	private Blur blur = new Blur(this);
	private Sobel sobel = new Sobel(this);
	private Hough hough = new Hough(this);

	private PImage original = null;

	/**
	 * Shared var
	 */
	private boolean paused = false;
	private boolean editable = false;
	private float viewTransform = PAUSE_HEIGHT / 650;

	private float rotate_x = 0;
	private float rotate_y = 0;
	private float rotate_z = 0;

	private float cam_rot = BASE_CAM_ROTATION;
	private float cam_pos = BASE_CAM_POSITION;
	private float cam_alt = BASE_CAM_ALTITUDE;

	private float rotation_increment = 0.01f;
	private float tiltSpeed = 1f;

	private final int COLOR_PLATE = color(200, 199, 195);
	private final int COLOR_BUMPS = color(227, 42, 21);
	private final int COLOR_BALL = color(10, 15, 13);

	private Mover mover;
	private List<PVector> bumps = new ArrayList<>();

	private float edit_x = 0;
	private float edit_z = 0;

	private static float score = 0;

	// Graphics
	private PGraphics gameWindow;
	private PGraphics smallVideo;
	private PGraphics bottomRect;
	private PGraphics topView;

	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "TangibleGame" });
	}

	@Override
	public void setup() {
		size(WINDOW_WIDTH, GAME_WINDOW_HEIGHT + BOTTOM_RECT_HEIGHT, P3D);
		mover = new Mover(this);
		gameWindow = createGraphics(WINDOW_WIDTH, GAME_WINDOW_HEIGHT, P3D);
		smallVideo = createGraphics(SMALL_VIDEO_WIDTH, SMALL_VIDEO_HEIGHT, P2D);
		bottomRect = createGraphics(WINDOW_WIDTH, BOTTOM_RECT_HEIGHT, P2D);
		topView = createGraphics(BOTTOM_RECT_HEIGHT - 10,
				BOTTOM_RECT_HEIGHT - 10, P2D);

		// / Gestion webcam / vid�o

		if (GAME_MODE_TANGIBLE) {
			if (WITH_WEBCAM) {
				String[] cameras = Capture.list();
				if (cameras.length == 0) {
					println("There are no cameras available for capture.");
					exit();
				} else {
					println("Available cameras:");
					for (int i = 0; i < cameras.length; i++) {
						println(cameras[i]);
					}
					webcam = new Capture(this, cameras[3]);
					webcam.start();
				}
			} else {
				fakecam = new Movie(this, LOAD_VIDEO_ADDRESS);
				fakecam.loop();
			}
		}
	}

	@Override
	public void draw() {
		/**
		 * Gestion du stream vid�o
		 */

		if (GAME_MODE_TANGIBLE) {
			if (WITH_WEBCAM) {
				if (webcam.available() == true) {
					webcam.read();
				}
				original = webcam.get();
				// original.resize(1 + cam.width, 1 + cam.height);
			} else {
				fakecam.read();
				original = fakecam.get();
			}

			PImage modifiedImg;

			// On applique les différents filtres à la suite
			modifiedImg = thresh_hbs.filter(original);
			modifiedImg = blur.filter(modifiedImg);
			modifiedImg = thresh_intensity.filter(modifiedImg);
			modifiedImg = sobel.filter(modifiedImg);
			hough.computeLines(modifiedImg);

			List<PVector> corners = hough.displayLinesAndGetCorners(
					modifiedImg, false);

			if (corners.size() == 4) {
				TwoDThreeD mapping = new TwoDThreeD(original.width,
						original.height);

				rotation = PVector.mult(mapping.get3DRotations(corners),
						(float) (90 * 1E12 / Math.PI));
				System.out.println(rotation);
			}
		}

		/**
		 * D�place la balle et la plaque
		 */
		drawGameWindow();
		image(gameWindow, 0, 0);
		drawSmallVideo();
		image(smallVideo, 5, 5);
		drawBottomRect();
		image(bottomRect, 0, GAME_WINDOW_HEIGHT);
		drawTopView();
		image(topView, 5, GAME_WINDOW_HEIGHT + 5);
	}

	// D�place la cam�ra si le jeu est mis sur pause avec shift
	private void displayCamera() {
		if (!paused) {
			if (cam_pos == BASE_CAM_POSITION && cam_alt == BASE_CAM_ALTITUDE
					&& cam_rot == BASE_CAM_ROTATION) {
				// si tout est en place on peut ajouter des cylindres
				editable = true;
			} else {
				if (cam_pos != BASE_CAM_POSITION) {
					cam_pos = Package.getCloser(cam_pos, BASE_CAM_POSITION, 10);
				}
				if (cam_alt != BASE_CAM_ALTITUDE) {
					cam_alt = Package.getCloser(cam_alt, BASE_CAM_ALTITUDE, 10);
				}
				if (cam_rot != BASE_CAM_ROTATION) {
					cam_rot = Package.getCloser(cam_rot, BASE_CAM_ROTATION, 10);
				}
			}
		} else {
			if (cam_pos != 0) {
				cam_pos = Package.getCloser(cam_pos, 0, 10);
			}
			if (cam_alt != PAUSE_HEIGHT) {
				cam_alt = Package.getCloser(cam_alt, PAUSE_HEIGHT, 10);
			}
			if (cam_rot != 1) {
				cam_rot = Package.getCloser(cam_rot, 1, 10);
			}
		}
		// Display camera
		gameWindow.camera(cam_pos, cam_alt, 0, cam_rot, 0, 0, 0, 1, 0);
	}

	private void playGame() {
		gameWindow.pushMatrix();

		// Animated stuff
		if (!paused) {
			// Plate rotation
			gameWindow.rotateY(rotate_y);

			if (GAME_MODE_TANGIBLE) {
				// We are in tangible mode, get the calculated angle
				// rotate_x = -rotation.y;
				// rotate_z = -rotation.x;
				// rotate_z = rotation.x;
				// rotate_z = Package.getCloser(rotate_z, rotation.y, 1);

				float delta = 2f;
				
				float deltax = rotate_x - (-rotation.z);
				if (deltax > delta) {
					rotate_x += rotation_increment;
				} else if (deltax > -delta) {
					rotate_x = -rotation.z;
				} else {
					rotate_x -= rotation_increment;
				}

				float deltaz = rotate_z - (-rotation.y);
				System.out.println(deltaz);
				if (deltaz > delta) {
					rotate_z += rotation_increment;
				} else if (deltaz > -delta) {
					rotate_z = -rotation.y;
				} else {
					rotate_z -= rotation_increment;
				}

				// Rotation vector update
				rotation.z = -rotate_x;
				rotation.y = -rotate_z;
			} else {
				// We are in virtual mode

				rotate_x = map(pmouseX * tiltSpeed, 0, width, MAX_ROTATION,
						-MAX_ROTATION);
				rotate_z = map(pmouseY * tiltSpeed, 0, height, MAX_ROTATION,
						-MAX_ROTATION);
			}

			// Ball
			mover.checkEdges();
			mover.checkCylinderCollision();
			mover.update(rotate_z, rotate_x);
		} else {
			// We are in edit mode
			rotate_x = 0;
			rotate_z = 0;

			if (paused) {

				gameWindow.pushMatrix();

				// On va corriger la position 3D de la souris par rapport �
				// o�
				// elle pointe avec viewTransform
				edit_x = (mouseY - height / 2) * viewTransform;
				edit_z = -(mouseX - width / 2) * viewTransform;
				gameWindow.translate(edit_x, 0, edit_z);

				if (collides(Cylinder.CYLINDER_BASE_RADIUS, edit_x, edit_z)) {
					gameWindow.fill(color(COLOR_BUMPS, 50));
					editable = false;
				} else {
					gameWindow.fill(COLOR_BUMPS);
					editable = true;
				}

				Cylinder cursorCylinder = new Cylinder(gameWindow);
				cursorCylinder.draw();

				gameWindow.popMatrix();
			}
		}

		gameWindow.rotateX(rotate_x);
		gameWindow.rotateZ(rotate_z);

		// Display plate
		gameWindow.fill(COLOR_PLATE);
		gameWindow.box(PLATE_WIDTH, PLATE_HEIGHT, PLATE_WIDTH);

		// Display cylinders
		gameWindow.fill(COLOR_BUMPS);
		for (PVector bump : bumps) {
			gameWindow.pushMatrix();
			gameWindow.translate(bump.x, 0, bump.z);
			(new Cylinder(gameWindow)).draw();
			gameWindow.popMatrix();
		}

		// Display ball
		gameWindow.fill(COLOR_BALL);
		mover.display(gameWindow);

		gameWindow.popMatrix();

	}

	public List<PVector> getBumps() {
		return bumps;
	}

	// V�rifie qu�on puisse poser le cylindre (pas en dehors du terrain, ou
	// sur
	// la balle)
	private boolean collides(float cylinderRadius, float x, float z) {

		float n = x + cylinderRadius;
		float s = x - cylinderRadius;
		float e = z + cylinderRadius;
		float w = z - cylinderRadius;

		float dx = x - mover.x();
		float dz = z - mover.z();

		boolean touchBall = sqrt(dx * dx + dz * dz) < cylinderRadius
				+ Mover.BALL_SIZE;

		boolean outsidePlate = n > PLATE_WIDTH / 2 || s < -PLATE_WIDTH / 2
				|| w < -PLATE_WIDTH / 2 || e > PLATE_WIDTH / 2;

		return touchBall || outsidePlate;
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
			editable = false;
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
			if (tiltSpeed >= 0.5f)
				tiltSpeed = tiltSpeed - 0.1f;
		}
	}

	// Add cylinders
	@Override
	public void mouseClicked(MouseEvent e) {
		if (editable) {
			bumps.add(new PVector(edit_x, 0, edit_z));
		}
	}

	// Graphics drawing
	void drawGameWindow() {
		// Caméra et éclairage
		gameWindow.beginDraw();

		gameWindow.noStroke(); // désactive l'affichage des lignes extérieures
		gameWindow.directionalLight(100, 100, 100, 1, 1, -1);
		gameWindow.ambientLight(AMBI, AMBI, AMBI);
		gameWindow.background(BG_COLOR);

		pushMatrix();
		gameWindow.translate(-10, -10, 10);
		playGame();
		popMatrix();
		displayCamera();

		gameWindow.endDraw();
	}

	void drawSmallVideo() {
		smallVideo.beginDraw();
		if (GAME_MODE_TANGIBLE) {
			smallVideo.image(original, 0, 0, 160, 120);
		}
		smallVideo.endDraw();
	}

	void drawBottomRect() {
		bottomRect.beginDraw();
		bottomRect.background(255, 255, 200);
		bottomRect
				.rect(0, GAME_WINDOW_HEIGHT, WINDOW_WIDTH, BOTTOM_RECT_HEIGHT);
		bottomRect.endDraw();

		// Texte d'information
		bottomRect.beginDraw();

		bottomRect.fill(0);
		bottomRect.textSize(15);
		bottomRect.text("Score : " + Math.round(score * 100) / 100.0,
				BOTTOM_RECT_HEIGHT + 10, 20);
		bottomRect.text("Velocity : " + mover.ballVelocity(),
				BOTTOM_RECT_HEIGHT + 10, 40);
		bottomRect.text("Tilt speed : " + Math.round(tiltSpeed * 100) / 100.0,
				BOTTOM_RECT_HEIGHT + 10, 60);

		bottomRect.endDraw();
	}

	void drawTopView() {
		topView.beginDraw();
		topView.noStroke();

		// Draw the plate
		topView.fill(75, 151, 74);
		topView.rect(0, 0, BOTTOM_RECT_HEIGHT - 10, BOTTOM_RECT_HEIGHT - 10);

		// Draw the ball
		topView.fill(0, 0, 255);
		topView.ellipse((BOTTOM_RECT_HEIGHT - 10) / 2 + mover.z()
				* (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH,
				(BOTTOM_RECT_HEIGHT - 10) / 2 - mover.x()
						* (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH,
				Mover.BALL_SIZE * 2 * (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH,
				Mover.BALL_SIZE * 2 * (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH);

		// Draw the bumps
		topView.fill(255, 0, 0);
		for (PVector bump : bumps) {
			topView.ellipse((BOTTOM_RECT_HEIGHT - 10) / 2 + bump.z
					* (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH,
					(BOTTOM_RECT_HEIGHT - 10) / 2 - bump.x
							* (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH, 50
							* (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH, 50
							* (BOTTOM_RECT_HEIGHT - 10) / PLATE_WIDTH);
		}

		topView.endDraw();
	}

	public static void addScore(float diff) {
		score += diff;
	}
}
