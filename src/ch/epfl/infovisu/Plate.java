package ch.epfl.infovisu;



import processing.core.*;
import processing.event.MouseEvent;

@SuppressWarnings("serial")
public class Plate extends PApplet {
	/**
	 * Global parameters
	 */
	private final static float AMBI = 120;
	private static final float BG_COLOR = 255;

	private static final float MAX_ROTATION = radians(60);
	
	private static final float CAM_ALTITUDE = 160;
	private static final float PLATE_WIDTH = 200;

	/**
	 * Shared var
	 */	
	private float rotate_y = 0;
	private float rotation_increment = 0.1f;

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
		noStroke(); //disable the outline
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see processing.core.PApplet#draw()
	 */
	@Override
	public void draw() {	
		pushMatrix();
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
		popMatrix();
		
		textSize(15);
		text("rotation : "+Math.round(rotation_increment*100.0)/100.0, 
				500, 15); 
		//fill(0, 0, 255);
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
	       int notches = e.getCount();
	       if (notches < 0) { // mouse wheel up
	    	   if(rotation_increment <= 0.25)
	    		   rotation_increment=rotation_increment+0.01f;
	       } else { // mouse wheel down
	    	   if(rotation_increment >= 0.01)
	    		   rotation_increment=rotation_increment-0.01f;
	       }
	}
	
	
}