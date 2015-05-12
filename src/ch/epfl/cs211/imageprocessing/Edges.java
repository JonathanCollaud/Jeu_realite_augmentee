package ch.epfl.cs211.imageprocessing;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

public class Edges extends PApplet{
	private static final long serialVersionUID = 1L;
	private static final int N_LINES = 4;
	private final ArrayList<PVector> bestCandidates = new ArrayList<PVector>();
	
	public Edges(ArrayList<Integer> accumulator) {
		ArrayList<PVector> result = new ArrayList<PVector>();
		int i, accR, accPhi, x0, y0, x1, y1, x2, y2, x3, y3;
		float r, phi;
		for (i = 0; i < min(N_LINES, bestCandidates.size()); i++) {
			
			// first, compute back the (r, phi) polar coordinates:
			accPhi = (int) (bestCandidates.get(i) / (rDim + 2)) - 1;
			accR = bestCandidates.get(i) - (accPhi + 1) * (rDim + 2) - 1;
			r = (accR - (rDim - 1) * 0.5f) * discStepsR;
			phi = accPhi * discStepsPhi;

			result.add(new PVector(r, phi));

			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)
			// compute the intersection of this line with the 4 borders of //
			// the image
			x0 = 0;
			y0 = (int) (r / sin(phi));
			x1 = (int) (r / cos(phi));
			y1 = 0;
			x2 = edgeImg.width;
			y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
			y3 = edgeImg.width;
			x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));

			// Finally, plot the lines
			stroke(204, 102, 0);
			if (y0 > 0) {
				if (x1 > 0)
					line(x0, y0, x1, y1);
				else if (y2 > 0)
					line(x0, y0, x2, y2);
				else
					line(x0, y0, x3, y3);
			} else {
				if (x1 > 0) {
					if (y2 > 0)
						line(x1, y1, x2, y2);
					else
						line(x1, y1, x3, y3);
				} else
					line(x2, y2, x3, y3);
			}
		}
	}

	public ArrayList<PVector> getIntersections(ArrayList<PVector> lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();

		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);

			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);

				// compute the intersection and add it to 'intersections'
				float d = cos(line2.y) * sin(line1.y) - cos(line1.y)
						* sin(line2.y);
				int x = (int) ((line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d);
				int y = (int) ((-line2.x * cos(line1.y) + line1.x
						* cos(line2.y)) / d);

				// draw the intersection
				fill(255, 128, 0);
				ellipse(x, y, 10, 10);
			}
		}
		return intersections;
	}
}
