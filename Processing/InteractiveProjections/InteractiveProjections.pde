final int WINDOW_WIDTH = 800;

void setup() {
  size (WINDOW_WIDTH, WINDOW_WIDTH, P2D);
}

int scaleX = 8;
int scaleY = 8;
float angleX = PI/8;
float angleY = 0;

void draw() {
  background(255, 255, 255);
  My3DPoint eye = new My3DPoint(-WINDOW_WIDTH/2, -WINDOW_WIDTH/2, -5000);
  float cubeWidth = 100;
  float cubeHeight = 150;
  float cubeDepth = 300;
  My3DPoint cubeOrigin = new My3DPoint(-cubeWidth/2, -cubeHeight/2, -cubeDepth/2);
  My3DBox input3DBox = new My3DBox(cubeOrigin, cubeWidth, cubeHeight, cubeDepth);
  
  float[][] rotateX = rotateXMatrix(angleX);
  float[][] rotateY = rotateYMatrix(angleY);
  float[][] scale = scaleMatrix(scaleX/4, scaleY/4, 0.25);
  input3DBox = transformBox(input3DBox, rotateX);
  input3DBox = transformBox(input3DBox, rotateY);
  input3DBox = transformBox(input3DBox, scale);
  projectBox(eye, input3DBox).render();
}

void mouseDragged() {
  scaleX += mouseX-pmouseX;
  scaleY += mouseY-pmouseY;
}

void keyPressed() {
  float delta = PI/16;
  switch(keyCode){
    case UP:
      angleX += delta;
      break;
    case DOWN:
      angleX -= delta;
      break;
    case RIGHT:
      angleY += delta;
      break;
    case LEFT:
      angleY -= delta;
      break;
  }
}

class My2DPoint {
  float x;
  float y;
  My2DPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }
}

class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}

My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
  float[]point = {p.x,p.y,p.z,1};
  float[][] T = {{1,0,0,-eye.x}, {0,1,0,-eye.y}, {0,0,1,-eye.z}, {0,0,0,1}};
  float[][] P = {{1,0,0,0}, {0,1,0,0}, {0,0,1,0}, {0,0,-1/eye.z,0}};
  float[] PTp = matrixProduct(P, matrixProduct(T, point));
  return new My2DPoint(PTp[0]/PTp[3], PTp[1]/PTp[3]);
}

class My2DBox {
  My2DPoint[] s;
  
  My2DBox(My2DPoint[] s) {
    this.s = s;
  }
  
  void render(){
    for(int i=0; i<=3; i++){
      line(s[i].x, s[i].y, s[(i+1)%4].x, s[(i+1)%4].y);
      line(s[i+4].x, s[i+4].y, s[(i+1)%4+4].x, s[(i+1)%4+4].y);
      line(s[i].x, s[i].y, s[i+4].x, s[i+4].y);
    }
  }
}

class My3DBox {
  My3DPoint[] p;
  
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ) {
    float x = origin.x;
    float y = origin.y;
    float z = origin.z;
    this.p = new My3DPoint[]{
      new My3DPoint(x,y+dimY,z+dimZ),
      new My3DPoint(x,y,z+dimZ),
      new My3DPoint(x+dimX,y,z+dimZ),
      new My3DPoint(x+dimX,y+dimY,z+dimZ),
      new My3DPoint(x,y+dimY,z),origin,
      new My3DPoint(x+dimX,y,z),
      new My3DPoint(x+dimX,y+dimY,z)
    };
  }
  
  My3DBox(My3DPoint[] p) {
    this.p = p;
  }
}

My2DBox projectBox(My3DPoint eye, My3DBox box) {
  My2DBox b = new My2DBox(new My2DPoint[box.p.length]);
  for (int i=0; i < box.p.length; i++){
    b.s[i] = projectPoint(eye, box.p[i]);
  }
  return b;
}

float[] homogeneous3DPoint (My3DPoint p) {
  float[] result = {p.x, p.y, p.z , 1};
  return result;
}

float[][] rotateXMatrix(float angle) {
  return(new float[][] {{1, 0 , 0 , 0},
                        {0, cos(angle), sin(angle) , 0},
                        {0, -sin(angle) , cos(angle) , 0},
                        {0, 0 , 0 , 1}});
}

float[][] rotateYMatrix(float angle) {
   return(new float[][] {{cos(angle), 0 , sin(angle) , 0},
                        {0, 1, 0 , 0},
                        {-sin(angle), 0 , cos(angle) , 0},
                        {0, 0 , 0 , 1}});
}

float[][] rotateZMatrix(float angle) {
   return(new float[][] {{cos(angle), sin(angle), 0 , 0},
                        {-sin(angle), cos(angle), 0 , 0},
                        {0, 0 , 1 , 0},
                        {0, 0 , 0 , 1}});
}

float[][] scaleMatrix(float x, float y, float z) {
  return(new float[][] {{x, 0, 0 , 0},
                        {0, y, 0 , 0},
                        {0, 0 , z , 0},
                        {0, 0 , 0 , 1}});
}

float[][] translationMatrix(float x, float y, float z) {
  return(new float[][] {{1, 0, 0 , x},
                        {0, 1, 0 , y},
                        {0, 0 , 1 , z},
                        {0, 0 , 0 , 1}});
}

float[] matrixProduct(float[][] a, float[] b) {
  float[] prod = new float[a.length];
  for (int row = 0; row < a.length; row++) {
    prod[row] = 0.0;
    for (int i = 0; i < b.length; i++) {
      prod[row] += a[row][i] * b[i];
    }
  }
  return prod;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  My3DBox b = new My3DBox(new My3DPoint[box.p.length]);
  for (int i=0; i < box.p.length; i++){
    b.p[i] = euclidian3DPoint(matrixProduct(transformMatrix, new float[] {box.p[i].x, box.p[i].y, box.p[i].z, 1}));
  }
  return b;
}

My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}
