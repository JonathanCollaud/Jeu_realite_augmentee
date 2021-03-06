import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;
import java.util.Comparator;
import java.util.Collections;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class HoughTransform extends PApplet implements Comparator<Integer> {
	int[] accumulator;

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

	public ArrayList<PVector> hough(PImage edgeImg) {
		float discStepsPhi = 0.06f;
		float discStepsR = 2.5f;

		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / discStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discStepsR);

		// pre-compute the sin and cos values
		float[] tabSin = new float[phiDim];
		float[] tabCos = new float[phiDim];
		float ang = 0;
		float inverseR = 1.f / discStepsR;
		for (int accPhi = 0; accPhi < phiDim; ang += discStepsPhi, accPhi++) {
			// we can also pre-multiply by (1/discStepsR) since we need it in
			// the Hough loop
			tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
			tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
		}

		// our accumulator (with a 1 pix margin around)
		accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		int x, y, accR, accPhi;
		for (y = 0; y < edgeImg.height; y++) {
			for (x = 0; x < edgeImg.width; x++) {
				// Are we on an edge?
				if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {

					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for (accPhi = 0; accPhi < phiDim; accPhi++) {
						accR = (int) Math.round(x * tabCos[accPhi] + y
								* tabSin[accPhi] + (rDim + 1) / 2);
						accumulator[(accPhi + 1) * (rDim + 2) + accR]++;
					}

				}
			}
		}

		int nLines = 4;
		int neighbourhood = 10; // size of the region we search for a local
								// maximum
		int minVotes = 200; // only search around lines with more that this
							// amount of votes // (to be adapted to your image)
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
		int i, dR, dPhi, neighbourIndex;
		boolean bestCandidate;

		for (accR = 0; accR < rDim; accR++) {
			for (accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index in the accumulator
				i = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[i] > minVotes) {
					bestCandidate = true;
					// iterate over the neighbourhood
					for (dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;
						for (dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
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

		// Save/plot the lines
		ArrayList<PVector> result = new ArrayList<PVector>();
		int x0, y0, x1, y1, x2, y2, x3, y3;
		float r, phi;
		for (i = 0; i < min(nLines, bestCandidates.size()); i++) {
			
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

		return result;
	}

	@Override
	public int compare(Integer l1, Integer l2) {
		if (accumulator[l1] > accumulator[l2]
				|| (accumulator[l1] == accumulator[l2] && l1 < l2))
			return -1;
		return 1;
	}
}
