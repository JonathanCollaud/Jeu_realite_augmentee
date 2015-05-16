package cs211.imageprocessing;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.min;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.ALPHA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * @author Jonathan Collaud
 * @author Raphaël Dunant
 * @author Thibault Viglino
 *
 * Groupe : AB
 */
public final class Hough {
	private static final int N_LINES = 6;
	private static final float DISC_STEPS_R = 2.5f;
	private static final float DISC_STEPS_PHI = 0.06f;
	private static final float[] TAB_SIN = new float[(int) (Math.PI / DISC_STEPS_PHI)];
	private static final float[] TAB_COS = new float[(int) (Math.PI / DISC_STEPS_PHI)];
	private static final int NEIGHBOURHOOD = 10; // size of the region we search
													// for a local maximum
	private static final int MIN_VOTES = 200; // only search around lines with
												// more that this amount
	// of votes (to be adapted to your image)
	private int[] accumulator;
	private int phiDim, rDim;
	private PApplet p;

	public Hough(PApplet p) {
		this.p = p;
	}

	public List<PVector> displayLinesAndGetCorners(PImage img) {
		// Si l’accumulator n’as pas été crée avec une autre image
		if (accumulator == null) {
			computeLines(img);
		}
		List<Integer> bestCandidates = getBestCandidates(img);
		List<PVector> lines = new ArrayList<>();

		for (int i = 0; i < min(N_LINES, bestCandidates.size()); i++) {

			// first, compute back the (r, phi) polar coordinates:
			int accPhi = (int) (bestCandidates.get(i) / (rDim + 2)) - 1;
			int accR = bestCandidates.get(i) - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * DISC_STEPS_R;
			float phi = accPhi * DISC_STEPS_PHI;

			// Create PVectors and ad it to lines
			lines.add(new PVector(r, phi));

			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)
			// compute the intersection of this line with the 4 borders of the
			// image
			int x0 = 0;
			int y0 = (int) (r / sin(phi));
			int x1 = (int) (r / cos(phi));
			int y1 = 0;
			int x2 = img.width;
			int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
			int y3 = img.width;
			int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));

			// Finally, plot the lines
			p.stroke(204, 102, 0);
			if (y0 > 0) {
				if (x1 > 0)
					p.line(x0, y0, x1, y1);
				else if (y2 > 0)
					p.line(x0, y0, x2, y2);
				else
					p.line(x0, y0, x3, y3);
			} else {
				if (x1 > 0) {
					if (y2 > 0)
						p.line(x1, y1, x2, y2);
					else
						p.line(x1, y1, x3, y3);
				} else
					p.line(x2, y2, x3, y3);
			}
		}

		return getIntersections(lines);
	}

	public PImage computeLines(PImage img) {

		phiDim = (int) (Math.PI / DISC_STEPS_PHI);
		rDim = (int) (((img.width + img.height) * 2 + 1) / DISC_STEPS_R);
		initTrigo(phiDim);

		// our accumulator (with a 1 pix margin around)
		accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < img.height; y++) {
			for (int x = 0; x < img.width; x++) {
				// Are we on an edge?
				if (p.brightness(img.pixels[y * img.width + x]) != 0) {

					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for (int accPhi = 0; accPhi < phiDim; accPhi++) {
						float accR = x * TAB_COS[accPhi] + y * TAB_SIN[accPhi];
						accumulator[(int) (accR + (rDim) / 2) + (accPhi + 1) * (rDim + 2)]++;
					}
				}
			}
		}

		System.out.println("img " + img.width + " " + img.height);
		System.out.println("hou " + rDim + " " + phiDim);
		PImage houghImg = p.createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = p.color(min(255, accumulator[i]));
		}
		
		// houghImg.updatePixels();
		return houghImg;
	}

	private List<Integer> getBestCandidates(PImage img) {
		List<Integer> bestCandidates = new ArrayList<>();

		boolean bestCandidate;

		int accR, accPhi;
		for (accR = 0; accR < rDim; accR++) {
			for (accPhi = 0; accPhi < phiDim; accPhi++) {

				// compute current index in the accumulator
				int i = (accPhi + 1) * (rDim + 2) + accR + 1;

				if (accumulator[i] > MIN_VOTES) {
					bestCandidate = true;
					// iterate over the neighbourhood

					for (int dPhi = -NEIGHBOURHOOD / 2; dPhi < NEIGHBOURHOOD / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;

						for (int dR = -NEIGHBOURHOOD / 2; dR < NEIGHBOURHOOD / 2 + 1; dR++) {
							// check we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;

							int neighbourIndex = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;

							if (accumulator[i] < accumulator[neighbourIndex]) {
								// the current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if (!bestCandidate)
							break;
					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(i);
					}
				}
			}
		}

		Comparator<Integer> c = new Comparator<Integer>() {
			@Override
			public int compare(Integer l1, Integer l2) {
				if (accumulator[l1] > accumulator[l2] || (accumulator[l1] == accumulator[l2] && l1 < l2))
					return -1;
				return 1;
			}
		};

		Collections.sort(bestCandidates, c);

		if (bestCandidates.size() > N_LINES) {
			bestCandidates = bestCandidates.subList(0, N_LINES - 1);
		}

		return bestCandidates;
	}

	private void initTrigo(int phiDim) {
		// pre-compute the sin and cos values
		float ang = 0;
		float inverseR = 1.f / DISC_STEPS_R;

		for (int accPhi = 0; accPhi < phiDim; ang += DISC_STEPS_PHI, accPhi++) {
			// we can also pre-multiply by (1/discStepsR) since we need it in
			// the Hough loop
			TAB_SIN[accPhi] = (float) (Math.sin(ang) * inverseR);
			TAB_COS[accPhi] = (float) (Math.cos(ang) * inverseR);
		}
	}

	private List<PVector> getIntersections(List<PVector> lines) {
		List<PVector> intersections = new ArrayList<PVector>();

		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);

			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);

				// compute the intersection and add it to 'intersections'
				float d = cos(line2.y) * sin(line1.y) - cos(line1.y) * sin(line2.y);
				int x = (int) ((line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d);
				int y = (int) ((-line2.x * cos(line1.y) + line1.x * cos(line2.y)) / d);

				// draw the intersection
				p.fill(255, 128, 0);
				p.ellipse(x, y, 10, 10);
			}
		}
		return intersections;
	}
}