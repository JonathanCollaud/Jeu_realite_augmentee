class Cylinder {

  private final static float CYLINDER_BASE_RADIUS = 100;
  private final static float CYLINDER_HEIGHT = 100;
  private final static int CYLINDER_RESOLUTION = 20;

  private float height;
  private float baseRadius;
  private PShape cylinder = new PShape();

  public Cylinder() {
    this(CYLINDER_HEIGHT, CYLINDER_BASE_RADIUS);
  }

  public Cylinder(float height, float baseRadius) {
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

    drawSides(x, z);
    drawCap(x, z, -height);

  }

  // draw the top of the cylinder
  private void drawCap(float[] x, float[] z, float height) {
    beginShape(TRIANGLE_FAN);
    
    // point central
    vertex(0, height, 0);
    
    // pourtour
    for (int i = 0; i < x.length; i++) {
      vertex(x[i], height, z[i]);
    }
    
    endShape(CLOSE);
  }

  // draw the border of the cylinder
  private void drawSides(float[] x, float[] z) {
    beginShape(QUAD_STRIP);
    
    for (int i = 0; i < x.length; i++) {
      vertex(x[i], 0, z[i]);
      vertex(x[i], height, z[i]);
    }
    
    endShape(CLOSE);
  }

  public void draw() {
    shape(cylinder);
  }

  public float getSize() {
    return baseRadius;
  }  
}

