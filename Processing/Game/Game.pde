import java.util.ArrayList;
import java.util.List;

/**
 * Global parameters
 */
private static final int WINDOW_WIDTH = 800;
private static final int WINDOW_HEIGHT = 600;
private static final int BOTTOM_RECT_HEIGHT = 100;

private static final float AMBI = 220;     //luminosité (sur 255)
private static final float BG_COLOR = 255; //coleur du fond (nuance de gris, sur 255)

private static final float MAX_ROTATION = radians(60);

private static final float PLATE_WIDTH = 300;
private static final float PLATE_HEIGHT = 5;

private final float PAUSE_HEIGHT = -300;

private final float BASE_CAM_ROTATION = 0;
private final float BASE_CAM_ALTITUDE = -256;
private final float BASE_CAM_POSITION = -2*PLATE_WIDTH;

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

private float tiltSpeed = 1f;

private Mover mover;
private List<PVector> bumps = new ArrayList();

private float edit_x = 0;
private float edit_z = 0;

private float cylinderHeight = 20;

// Graphics
private PGraphics gameWindow;
private PGraphics bottomRect;
private PGraphics topView;

public void setup() {
  size(WINDOW_WIDTH, WINDOW_HEIGHT, P3D);
  noStroke(); // désactive l'affichage des lignes extérieures
  mover = new Mover(PLATE_WIDTH, PLATE_HEIGHT);
  gameWindow = createGraphics(WINDOW_WIDTH, WINDOW_HEIGHT-BOTTOM_RECT_HEIGHT, P2D);
  bottomRect = createGraphics(WINDOW_WIDTH, BOTTOM_RECT_HEIGHT, P2D);
  topView = createGraphics(BOTTOM_RECT_HEIGHT-10, BOTTOM_RECT_HEIGHT-10, P2D);
}

public void draw() {
  // Caméra et éclairage
  displayCamera();
  directionalLight(10, 10, 10, 1, -1, -1);
  ambientLight(AMBI, AMBI, AMBI);
  background(BG_COLOR);
  
  drawBottomRect();
  image(bottomRect, 0, WINDOW_HEIGHT-BOTTOM_RECT_HEIGHT);
  drawTopView();
  image(topView, 5, WINDOW_HEIGHT-BOTTOM_RECT_HEIGHT+5);

  // Gère le déplacement de la balle et de la plaque
  playGame();

  // Texte d'information
  textSize(15);
  text("tilt speed : " + tiltSpeed, 500, 35);
}

private void displayCamera() {
  if (paused) {
    //si la caméra est en position d'édition => on peut poser les cylindres
    if (cam_pos == 0 && cam_alt == PAUSE_HEIGHT && cam_rot == 1)
      editable = true;
    //sinon on s'en approche
    else {
      cam_pos = Package.getCloser(cam_pos, 0);
      cam_alt = Package.getCloser(cam_alt, PAUSE_HEIGHT);
      cam_rot = Package.getCloser(cam_rot, 1);
    }
  }
  //si pas en pause, on se met en position de base
  else {
    cam_pos = Package.getCloser(cam_pos, BASE_CAM_POSITION);
    cam_alt = Package.getCloser(cam_alt, BASE_CAM_ALTITUDE);
    cam_rot = Package.getCloser(cam_rot, BASE_CAM_ROTATION);
  }
  // positionne la caméra
  camera(cam_pos, cam_alt, 0, cam_rot, 0, 0, 0, 1, 0);
}

private void playGame() {
  pushMatrix();

  // Animations
  if (!paused) {
    // rotation de la plaque
    rotateY(rotate_y);

    rotate_x = map(pmouseX * tiltSpeed, 0, width, MAX_ROTATION, 
    -MAX_ROTATION);
    rotate_z = map(pmouseY * tiltSpeed, 0, height, MAX_ROTATION, -MAX_ROTATION);

    // Balle
    mover.checkEdges();
    mover.checkCylinderCollision();
    mover.update(rotate_z, rotate_x);
  } else {
    // mode d'édition
    rotate_x = 0;
    rotate_z = 0;

    if (paused) {

      pushMatrix();

      // On va corriger la position 3D de la souris par rapport à où
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

  // Affichage de la plaque
  fill(color(167, 219, 216));
  box(PLATE_WIDTH, PLATE_HEIGHT, PLATE_WIDTH);

  // Affichage des cylindres
  fill(color(105, 210, 231));
  for (PVector bump : bumps) {
    pushMatrix();
    translate(bump.x, 0, bump.y);
    (new Cylinder(cylinderHeight, 20)).draw();
    popMatrix();
  }

  // Affichage de la balle
  fill(color(224, 228, 204));
  mover.display();

  popMatrix();
}

public List<PVector> getBumps() {
  return bumps;
}

// Vérifie qu'on puisse poser le cylindre (pas en dehors du terrain, ou sur
// la balle)
private boolean collides(float cylinderRadius, float x, float z) {

  float n = x + cylinderRadius;
  float s = x - cylinderRadius;
  float e = z + cylinderRadius;
  float w = z - cylinderRadius;

  boolean touchBall = true;
  // (e < mover.x() || w > mover.x())
  // && (n < mover.z() || s > mover.z());

  boolean outsidePlate =  n > PLATE_WIDTH / 2 ||
    s < -PLATE_WIDTH / 2 ||
    w < -PLATE_WIDTH / 2 ||
    e > PLATE_WIDTH / 2;

  return touchBall && outsidePlate;
}


// shift => mode d'insertion de cylindres
public void keyPressed() {
  if (key == CODED && keyCode == SHIFT) {
    paused = true;
  }
}

// arrêt de mode édition en cas de touche shift relachée
public void keyReleased() {
  if (key == CODED && keyCode == SHIFT) {
    paused = false;
    editable = false;
  }
}

// modification de la vitesse de rotation en cas de rotation de la molette
public void mouseWheel(MouseEvent e) {
  if (e.getCount() < 0) { // rotation de la molette en haut
    if (tiltSpeed <= 1.5f)
      tiltSpeed = tiltSpeed + 0.1f;  //vitesse de rotation
  } 
  else // rotation de la molette en bas
    if (tiltSpeed >= 0.5f)
      tiltSpeed = tiltSpeed - 0.1f;
}

// Ajout des cylindres en cas de clic de souris
public void mouseClicked(MouseEvent e) {
  if (editable) {
    bumps.add(new PVector(edit_x, edit_z, 0));
  }
}

// Graphics drawing
void drawBottomRect() {
  bottomRect.beginDraw();
  background(255, 255, 200);
  bottomRect.rect(0, WINDOW_HEIGHT-BOTTOM_RECT_HEIGHT, WINDOW_WIDTH, BOTTOM_RECT_HEIGHT);
  bottomRect.endDraw();
}

void drawTopView() {
  topView.beginDraw();
  topView.noStroke();
  
  // Draw the plate
  topView.fill(0, 0, 255);
  topView.rect(0, 0, BOTTOM_RECT_HEIGHT-10, BOTTOM_RECT_HEIGHT-10);
  
  //Draw the ball
  topView.fill(255, 0, 0);
  topView.ellipse(
    (BOTTOM_RECT_HEIGHT-10)/2 + mover.ballX()*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH,
    (BOTTOM_RECT_HEIGHT-10)/2 + mover.ballZ()*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH,
    mover.BALL_SIZE*2*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH,
    mover.BALL_SIZE*2*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH);
    
  //Draw the bumps
  topView.fill(0, 255, 0);
  for(PVector bump : bumps) {
    topView.ellipse(
      (BOTTOM_RECT_HEIGHT-10)/2 + bump.x*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH,
      (BOTTOM_RECT_HEIGHT-10)/2 + bump.z*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH,
      50*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH,
      50*(BOTTOM_RECT_HEIGHT-10)/PLATE_WIDTH);
  }
  
  topView.endDraw();
}

