package cs211.tangiblegame;

public class Package {

	// Fonction qui rapproche une variable d�une cible en divisant la distance
	// par deux.
	public static float getCloser(float var, float target, double seuil) {
		float newVar;
		float delta = var - target;
		if (Math.abs(delta) <= seuil) {
			return target;
		} else {
			newVar = var - delta / 2;
			return newVar;
		}
	}
}
