static class Package {

  // Fonction qui rapproche une variable d'une cible en divisant la distance
  // par deux.
  public static float getCloser(float var, float target) {
    float delta = var - target;
    if (Math.abs(delta) <= 10) {
      return target;
    } else {
      var -= delta / 2;
      return var;
    }
  }

  // «Abaisse» un vecteur à 0 en y pour faire des calculs dans le plan
  public static float heightDrop(PVector vector) {    
    float heightValue = vector.y; // Récupère la hauteur du vecteur
    vector.y = 0;     // Abaisse le vecteur
    return heightValue;
  }

  // Après calculs, restaure la hauteur du vecteur
  public void heightRestore(PVector vector, float previousValue) {
    vector.y = previousValue;
  }
}

