package ch.epfl.cs211.imageprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import processing.core.PApplet;
import processing.core.PImage;

public class Hough extends PApplet implements Comparator<Integer> {
	private static final long serialVersionUID = 1L;
	private static final float DISC_STEPS_R = 2.5f;
	private static final float DISC_STEPS_PHI = 0.06f;
	private static final float[] TAB_SIN = new float[(int) (Math.PI / DISC_STEPS_PHI)];
	private static final float[] TAB_COS = new float[(int) (Math.PI / DISC_STEPS_PHI)];
	private static final int NEIGHBOURHOOD = 10; // size of the region we search for a local maximum
	private static final int MIN_VOTES = 200; // only search around lines with more that this amount
							// of votes (to be adapted to your image)
	private final int[] accumulator;
	private final ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
	private int phiDim, rDim;

	public Hough(PImage img) {

		phiDim = (int) (Math.PI / DISC_STEPS_PHI);
		rDim = (int) (((img.width + img.height) * 2 + 1) / DISC_STEPS_R);
		initTrigo(phiDim);

		// our accumulator (with a 1 pix margin around)
		accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		int x, y, accR, accPhi;
		for (y = 0; y < img.height; y++) {
			for (x = 0; x < img.width; x++) {
				// Are we on an edge?
				if (brightness(img.pixels[y * img.width + x]) != 0) {

					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for (accPhi = 0; accPhi < phiDim; accPhi++) {
						accR = (int) Math.round(x * TAB_COS[accPhi] + y
								* TAB_SIN[accPhi] + (rDim + 1) / 2);
						accumulator[(accPhi + 1) * (rDim + 2) + accR]++;
					}

				}
			}
		}
	}

	public ArrayList<Integer> getBestCandidates() {
		int i, dR, dPhi, neighbourIndex;
		boolean bestCandidate;

		int accR, accPhi;
		for (accR = 0; accR < rDim; accR++) {
			for (accPhi = 0; accPhi < phiDim; accPhi++) {

				// compute current index in the accumulator
				i = (accPhi + 1) * (rDim + 2) + accR + 1;

				if (accumulator[i] > MIN_VOTES) {
					bestCandidate = true;
					// iterate over the neighbourhood

					for (dPhi = -NEIGHBOURHOOD / 2; dPhi < NEIGHBOURHOOD / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;

						for (dR = -NEIGHBOURHOOD / 2; dR < NEIGHBOURHOOD / 2 + 1; dR++) {
							// check we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;

							neighbourIndex = (accPhi + dPhi + 1) * (rDim + 2)
									+ accR + dR + 1;

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
		Collections.sort(bestCandidates, this);
		return bestCandidates;
	}

	public PImage img() {
		PImage image = new PImage(rDim, phiDim);

		for (int p = 0; p < accumulator.length; p++) {
			image.pixels[p] = accumulator[p];
		}

		return image;
	}

	@Override
	public int compare(Integer l1, Integer l2) {
		if (accumulator[l1] > accumulator[l2]
				|| (accumulator[l1] == accumulator[l2] && l1 < l2))
			return -1;
		return 1;
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
}