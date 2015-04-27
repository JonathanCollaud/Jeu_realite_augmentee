import java.util.List;

class Mover {

  private static final float GRAVITY_CONSTANT = 4;
  private static final float BALL_MASS = 1;
  private static final float BALL_RADIUS = 8;

  // Ball positioning
  private float normalForce = 1f;
  private float mu = 0.1f;
  private float elasticity = 0.7f;
  private float frictionMagnitude = normalForce * mu * BALL_MASS;
  private float plate_width = 1;
  private float plate_height = 1;

  private PVector ballPosition = new PVector(0, -BALL_RADIUS - 2*plate_height, 0);
  private PVector ballVelocity = new PVector(0, 0, 0);
  private PVector ballAcceleration = new PVector(0, 0, 0);
  private PVector friction = new PVector(0, 0, 0);
  private Game applet;

  public Mover(float plate_width, float plate_height) {
    this.plate_width = plate_width;
    this.plate_height = plate_height;
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

  public void display(PGraphics pg) {
    pg.translate(ballPosition.x, ballPosition.y, ballPosition.z);
    pg.sphere(BALL_RADIUS);
  }

  public void checkEdges() {
    if (ballPosition.x <= -plate_width / 2 + BALL_RADIUS
      || ballPosition.x >= plate_width / 2 - BALL_RADIUS) {
      ballPosition.x = Math.signum(ballPosition.x)
        * (plate_width / 2 - BALL_RADIUS);
      ballVelocity.x *= -elasticity;
    }
    if (ballPosition.z <= -plate_width / 2 + BALL_RADIUS
      || ballPosition.z >= plate_width / 2 - BALL_RADIUS) {
      ballPosition.z = Math.signum(ballPosition.z)
        * (plate_width / 2 - BALL_RADIUS);
      ballVelocity.z *= -elasticity;
    }
  }

  // Parcours une liste d’objets pour voir si la balle entre en collision avec un
  public void checkCylinderCollision(List<PVector> bumps, float objectSize) {  
    for (PVector bump : bumps) {
      // Création d’un vecteur représantant la distance entre la balle et le bump
      float deltaX = ballPosition.x - bump.x;
      float deltaZ = ballPosition.z - bump.z;
      PVector distance = new PVector(deltaX, 0, deltaZ); // On ne s’intéresse qu’aux coordonnées x et z

      if (checkCollision(distance, objectSize)) {
        bounce(distance, bump, objectSize);
      }
    }
  }

  // Créer un rebond pour la balle en fonction d’un objet
  public void bounce(PVector normal, PVector object, float objectSize) {
    normal.normalize(); // Le vecteur de distance va faire office de vecteur normal

    float dotProd = PVector.dot(ballVelocity, normal)*2;
    PVector multVect = PVector.mult(normal, dotProd);
    ballVelocity.sub(ballVelocity, multVect);
    ballVelocity.mult(elasticity);
    
    // Actualisation de la position
    ballPosition.x = object.x + (objectSize + BALL_RADIUS) * normal.x;
    ballPosition.z = object.z + (objectSize + BALL_RADIUS) * normal.z;
  }

  // Vérifie s’il y a collision entre la balle et un objet
  public boolean checkCollision(PVector distance, float objectSize) {
    // On compare les carrés pour de meilleurs performances (mag à besoin de sqrt())
    return distance.magSq() <= (objectSize + BALL_RADIUS) * (objectSize + BALL_RADIUS);
  }

  public PVector position() {
    return ballPosition;
  }
}

