import java.util.ArrayList;
import java.util.List;

/**
 * Global parameters
 */
private static final float AMBI = 220;
private static final float BG_COLOR = 255;

private static final float MAX_ROTATION = radians(60);

private static final float PLATE_WIDTH = 300;
private static final float PLATE_HEIGHT = 5;

private final float PAUSE_HEIGHT = -300;

private final float BASE_CAM_ROTATION = 0;
private final float BASE_CAM_ALTITUDE = -160;
private final float BASE_CAM_POSITION = -PLATE_WIDTH;

/**
 * Shared var
 */
private boolean paused = false;
private boolean editable = false;
private float viewTransform = PAUSE_HEIGHT / 520;
private float rotate_x = 0;
private float rotate_y = 0;
private float rotate_z = 0;
private float cam_rot = BASE_CAM_ROTATION;
private float cam_pos = BASE_CAM_POSITION;
private float cam_alt = BASE_CAM_ALTITUDE;
private float rotation_increment = 0.1f;
private float tiltSpeed = 1f;
private Mover mover;
private List<PVector> bumps = new ArrayList();
private float edit_x = 0;
private float edit_z = 0;
private float cylinderHeight = 20;

public void setup() {
  size(800, 600, P3D);
  noStroke(); // disable the outline
  mover = new Mover(PLATE_WIDTH, PLATE_HEIGHT, this);
}

public void draw() {
  // Camera and lighting
  displayCamera();
  directionalLight(10, 10, 10, 1, -1, -1);
  ambientLight(AMBI, AMBI, AMBI);
  background(BG_COLOR);

  // G�re le d�placement de la balle et de la plate
  playGame();

  // Information text
  textSize(15);
  text("rotation : " + Math.round(rotation_increment * 100.0) / 100.0,
      500, 15);
  text("tilt speed : " + tiltSpeed, 500, 35);
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
        cam_pos = Package.getCloser(cam_pos, BASE_CAM_POSITION);
      }
      if (cam_alt != BASE_CAM_ALTITUDE) {
        cam_alt = Package.getCloser(cam_alt, BASE_CAM_ALTITUDE);
      }
      if (cam_rot != BASE_CAM_ROTATION) {
        cam_rot = Package.getCloser(cam_rot, BASE_CAM_ROTATION);
      }
    }
  } else {
    if (cam_pos != 0) {
      cam_pos = Package.getCloser(cam_pos, 0);
    }
    if (cam_alt != PAUSE_HEIGHT) {
      cam_alt = Package.getCloser(cam_alt, PAUSE_HEIGHT);
    }
    if (cam_rot != 1) {
      cam_rot = Package.getCloser(cam_rot, 1);
    }
  }
  // Display camera
  camera(cam_pos, cam_alt, 0, cam_rot, 0, 0, 0, 1, 0);
}

private void playGame() {
  pushMatrix();

  // Animated stuff
  if (!paused) {
    // Plate rotation
    rotateY(rotate_y);

    rotate_x = map(pmouseX * tiltSpeed, 0, width, MAX_ROTATION,
        -MAX_ROTATION);
    rotate_z = map(pmouseY * tiltSpeed, 0, height, MAX_ROTATION, -MAX_ROTATION);

    // Ball
    mover.checkEdges();
    mover.checkCylinderCollision();
    mover.update(rotate_z, rotate_x);
  } else {
    // We are in edit mode
    rotate_x = 0;
    rotate_z = 0;

    if (paused) {

      pushMatrix();

      // On va corriger la position 3D de la souris par rapport � o�
      // elle pointe avec viewTransform
      edit_x = (mouseY - height / 2) * viewTransform;
      edit_z = -(mouseX - width / 2) * viewTransform;
      translate(edit_x, 0, edit_z);

      if (collides(cylinderHeight, edit_x, edit_z)) {
        fill(color(170, 40, 40));
        editable = false;
      } else {
        fill(color(40, 170, 40));
        editable = true;
      }

      Cylinder cursorCylinder = new Cylinder(cylinderHeight, 20);
      cursorCylinder.draw();

      popMatrix();
    }
  }

  rotateX(rotate_x);
  rotateZ(rotate_z);

  // Display plate
  fill(color(167, 219, 216));
  box(PLATE_WIDTH, PLATE_HEIGHT, PLATE_WIDTH);

  // Display cylinders
  fill(color(105, 210, 231));
  for (PVector bump : bumps) {
    pushMatrix();
    translate(bump.x, 0, bump.y);
    (new Cylinder(cylinderHeight, 20)).draw();
    popMatrix();
  }

  // Display ball
  fill(color(224, 228, 204));
  mover.display();

  popMatrix();

}

public List<PVector> getBumps() {
  return bumps;
}

// V�rifie qu�on puisse poser le cylindre (pas en dehors du terrain, ou sur
// la balle)
private boolean collides(float cylinderSize, float x, float z) {

  float n = x + cylinderSize;
  float s = x - cylinderSize;
  float e = z + cylinderSize;
  float w = z - cylinderSize;

  boolean touchBall = true;
  // (e < mover.x() || w > mover.x())
  // && (n < mover.z() || s > mover.z());

  boolean outsidePlate = n > PLATE_WIDTH / 2 || s < -PLATE_WIDTH / 2
      || w < -PLATE_WIDTH / 2 || e > PLATE_WIDTH / 2;

  return touchBall && outsidePlate;
}

/*
 * Interactions
 */
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
public void mouseClicked(MouseEvent e) {
  if (editable) {
    bumps.add(new PVector(edit_x, edit_z));
  }
}


