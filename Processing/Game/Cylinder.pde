class Cylinder {

  private final static float CYLINDER_BASE_RADIUS = 100;
  private final static float CYLINDER_HEIGHT = 100;
  private final static int CYLINDER_RESOLUTION = 20;

  private float height;
  private float baseRadius;
  private PShape cylinder = new PShape();

// Enlevé car pas de PGraphics par défaut!
//  public Cylinder() {
//    this(CYLINDER_HEIGHT, CYLINDER_BASE_RADIUS);
//  }

  public Cylinder(float height, float baseRadius, PGraphics pg) {
    this.baseRadius = baseRadius;
    this.height = -height;

    float angle;
    float[] x = new float[CYLINDER_RESOLUTION + 1];
    float[] z = new float[CYLINDER_RESOLUTION + 1];

    // get the x and z position on a circle for all the sides
    for (int i = 0; i < x.length; i++) {
      angle = ((float) Math.PI * 2 / CYLINDER_RESOLUTION) * i;
      x[i] = (float) Math.sin(angle) * baseRadius;
      z[i] = (float) Math.cos(angle) * baseRadius;
    }


    drawCap(x, z, 0, pg);
    drawSides(x, z, pg);
    drawCap(x, z, -height, pg);

  }

  // draw the top of the cylinder
  private void drawCap(float[] x, float[] z, float height, PGraphics pg) {
    pg.beginShape(TRIANGLE_FAN);
    
    // point central
    pg.vertex(0, height, 0);
    
    // pourtour
    for (int i = 0; i < x.length; i++) {
      pg.vertex(x[i], height, z[i]);
    }
    
    pg.endShape(CLOSE);
  }

  // draw the border of the cylinder
  private void drawSides(float[] x, float[] z, PGraphics pg) {
    pg.beginShape(QUAD_STRIP);
    
    for (int i = 0; i < x.length; i++) {
      pg.vertex(x[i], 0, z[i]);
      pg.vertex(x[i], height, z[i]);
    }
    
    pg.endShape(CLOSE);
  }

  public void draw() {
    shape(cylinder);
  }

  public float getSize() {
    return baseRadius;
  }  
}

