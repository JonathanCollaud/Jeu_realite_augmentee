package cs211.tangiblegame;

public class Package {

	// Fonction qui rapproche une variable d�une cible en divisant la distance
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
}
