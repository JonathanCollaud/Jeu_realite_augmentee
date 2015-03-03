import processing.core.*;

@SuppressWarnings("serial")
public class Plate extends PApplet {
	/**
	 * Global parameters
	 */
	private final static float AMBI = 120;
	private static final float BG_COLOR = 255;

	private static final float ROTATION_INCREMENT = 0.1f;
	private static final float MAX_ROTATION = radians(60);
	
	private static final float CAM_ALTITUDE = 160;
	private static final float PLATE_WIDTH = 200;

	/**
	 * Shared var
	 */	
	private float rotate_y = 0;

	/**
	 * Setup() and draw()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#setup()
	 */
	@Override
	public void setup() {
		size(800, 600, P3D);
		noStroke();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {
		// Camera and lighting
		camera(-height / 2, -CAM_ALTITUDE, 0, -PLATE_WIDTH/6, 0, 0, 0, 1, 0);
		directionalLight(10, 10, 10, 1, -1, -1);
		ambientLight(AMBI, AMBI + 20, AMBI);
		background(BG_COLOR);
				
		// Plate		
		rotateY(rotate_y);	
		
		float rotate_x = map(mouseX, 0, width, MAX_ROTATION, -MAX_ROTATION); 
		float rotate_z = map(mouseY, 0, height, MAX_ROTATION, -MAX_ROTATION);
		rotateX(rotate_x);
		rotateZ(rotate_z);
		
		box(PLATE_WIDTH, 20, PLATE_WIDTH);
		
	}

	/*
	 * Interactions
	 */
	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == LEFT) {
				rotate_y += ROTATION_INCREMENT;
			} else if (keyCode == RIGHT) {
				rotate_y -= ROTATION_INCREMENT;
			}
		}
	}
}