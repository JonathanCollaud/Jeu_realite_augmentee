package cs211.imageprocessing;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.min;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.ALPHA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * @author Jonathan Collaud
 * @author Raphaël Dunant
 * @author Thibault Viglino
 * 
 *         Groupe : AB
 */
public final class Hough {
	private static final int N_LINES = 6;
	private static final float DISC_STEPS_R = 2.5f;
	private static final float DISC_STEPS_PHI = 0.06f;
	private static final float[] TAB_SIN = new float[(int) (Math.PI / DISC_STEPS_PHI)];
	private static final float[] TAB_COS = new float[(int) (Math.PI / DISC_STEPS_PHI)];
	private static final int NEIGHBOURHOOD = 10; // size of the region we search
													// for a local maximum
	private static final int MIN_VOTES = 180; // only search around lines with
	private static final float MIN_AREA = 100000;
	private static final float MAX_AREA = 600000;
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
		}

		/**
		 * Quad selection
		 */

		QuadGraph quads = new QuadGraph(lines, img.width, img.height);
		quads.findCycles();

		Iterator<Integer[]> it = quads.getCycles().iterator();
		while (it.hasNext()) {
			Integer[] quad = it.next();

			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);

			PVector c12 = intersection(l1, l2);
			PVector c23 = intersection(l2, l3);
			PVector c34 = intersection(l3, l4);
			PVector c41 = intersection(l4, l1);

			// On colore, pour le folklore
			// Random random = new Random();
			// p.fill(p.color(min(255, random.nextInt(300)),
			// min(255, random.nextInt(300)),
			// min(255, random.nextInt(300)), 50));
			// p.quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);

			// Tri
			if (!QuadGraph.isConvex(c12, c23, c34, c41)
					|| !QuadGraph.validArea(c12, c23, c34, c41, MAX_AREA,
							MIN_AREA)
					|| !QuadGraph.nonFlatQuad(c12, c23, c34, c41)) {
				it.remove();
			}
		}

		if (quads.getCycles().isEmpty()) {
			System.out.println("Pas de quad suffisant.");
			return (new ArrayList<>());
		} else {

			/**
			 * Display
			 */

			Integer[] quad = quads.getCycles().get(0);
			List<PVector> finalLines = new ArrayList<>();

			for (int i = 0; i < 4; ++i) {
				PVector line = lines.get(quad[i]);
				// On recr�e une collection avec juste les quatres meilleures
				// lignes
				finalLines.add(line);

				int x0 = 0;
				int y0 = (int) (line.x / sin(line.y));
				int x1 = (int) (line.x / cos(line.y));
				int y1 = 0;
				int x2 = img.width;
				int y2 = (int) (-cos(line.y) / sin(line.y) * x2 + line.x
						/ sin(line.y));
				int y3 = img.width;
				int x3 = (int) (-(y3 - line.x / sin(line.y)) * (sin(line.y) / cos(line.y)));

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

			// Prints and return intersections
			List<PVector> intersections = getIntersections(finalLines);

			if (intersections.size() >= 4) {
				return TwoDThreeD.sortCorners(getFourCorners(intersections));
			} else {
				return null;
			}
		}
	}

	private List<PVector> getFourCorners(List<PVector> intersections) {
		if (intersections.size() < 4) {
			return null;
		}

		PVector origin = new PVector(0, 0);

		// 4 intersections which are the closest to the origin
		PVector clo1 = new PVector(65536, 65536);
		PVector clo2 = new PVector(65536, 65536);
		PVector clo3 = new PVector(65536, 65536);
		PVector clo4 = new PVector(65536, 65536);

		for (Iterator<PVector> iterator = intersections.iterator(); iterator
				.hasNext();) {
			PVector corner = (PVector) iterator.next();
			if (corner.dist(origin) < clo4.dist(origin)) {
				if (corner.dist(origin) < clo3.dist(origin)) {
					if (corner.dist(origin) < clo2.dist(origin)) {
						if (corner.dist(origin) < clo1.dist(origin)) {
							clo4 = clo3;
							clo3 = clo2;
							clo2 = clo1;
							clo1 = corner;
						} else {
							clo4 = clo3;
							clo3 = clo2;
							clo2 = corner;
						}
					} else {
						clo4 = clo3;
						clo3 = corner;
					}
				} else {
					clo4 = corner;
				}
			}
		}

		List<PVector> corners = new ArrayList<PVector>();
		corners.add(clo4);
		corners.add(clo3);
		corners.add(clo2);
		corners.add(clo1);

		return corners;
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
						accumulator[(int) (accR + (rDim) / 2) + (accPhi + 1)
								* (rDim + 2)]++;
					}
				}
			}
		}

		PImage houghImg = p.createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = p.color(min(255, accumulator[i]));
		}

		houghImg.updatePixels();
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

							int neighbourIndex = (accPhi + dPhi + 1)
									* (rDim + 2) + accR + dR + 1;

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
				if (accumulator[l1] > accumulator[l2]
						|| (accumulator[l1] == accumulator[l2] && l1 < l2))
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

				PVector intersection = intersection(line1, line2);
				intersections.add(intersection);

				// draw the intersection
				p.fill(255, 128, 0);
				p.ellipse(intersection.x, intersection.y, 10, 10);
			}
		}
		return intersections;
	}

	private PVector intersection(PVector line1, PVector line2) {
		// compute the intersection and add it to 'intersections'
		float d = cos(line2.y) * sin(line1.y) - cos(line1.y) * sin(line2.y);
		int x = (int) ((line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d);
		int y = (int) ((-line2.x * cos(line1.y) + line1.x * cos(line2.y)) / d);

		return new PVector(x, y);
	}
}