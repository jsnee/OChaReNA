package com.jophus.ocharena.plugins;

import java.util.ArrayList;

import com.jophus.ocharena.document.OCHDocument;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.ocharena.image.path.PathManager;
import com.jophus.ocharena.image.path.PixelPath;
import com.jophus.ocharena.image.path.RectPixelPath;

public class BasicCharTracer {
	/**
	 * The maximum fraction a row's height can be of the previous row's height,
	 * in order for the new (short) row to be merged in with the previous (tall)
	 * row to form a single row.
	 */
	protected float shortRowFraction = 0.125f;
	/**
	 * The minimum fraction of pixels in an area which must be white in order for the area to
	 * be considered whitespace when the liberal whitespace policy is in effect.
	 */
	protected float liberalPolicyAreaWhitespaceFraction = 0.95f;
	/**
	 * The minimum width of a space, expressed as a fraction of the height of a row of text.
	 */
	protected float minSpaceWidthAsFractionOfRowHeight = 0.6f;
	/**
	 * The minimum width of a character, expressed as a fraction of the height of a row of text.
	 */
	protected float minCharWidthAsFractionOfRowHeight = 0.15f;
	/**
	 * The minimum width of a character break (a vertical column of whitespace that separates
	 * two characters on a row of text), expressed as a fraction of the height of a row of text.
	 */
	protected float minCharBreakWidthAsFractionOfRowHeight = 0.05f;
	/**
	 * The white threshold.  Any pixel value that is greater than or equal to this value,
	 * will be considered to be white space for the purpose of separating rows of text
	 * and characters within each row.
	 */
	protected int whiteThreshold = 128;

	private OCHDocument ochDoc;

	public BasicCharTracer(OCHDocument ochDoc) {
		this.ochDoc = ochDoc;
	}

	public PathManager detectChars() {
		ImagePixels imagePixels = new ImagePixels(ochDoc.getImagePixels().getImageAsBufferedImage());
		PathManager result = new PathManager(imagePixels.getImageWidth(), imagePixels.getImageHeight());
		imagePixels.toGrayScale(true);
		imagePixels.filter();
		PathManager linePaths = ochDoc.loadImageHeader();
		for (int i = 0; i < linePaths.size(); i++) {
			PathManager charPaths = detectChars(imagePixels, linePaths.getPath(i));
			for (int j = 0; j < charPaths.size(); j++) {
				result.addPath(charPaths.getPath(j));
			}
		}
		return result;
	}

	private PathManager detectChars(ImagePixels imagePixels, PixelPath pixelPath) {
		int[] pixels = imagePixels.getPixelValues();
		int w = imagePixels.getImageWidth();
		int h = imagePixels.getImageHeight();
		PathManager pathManager = new PathManager(w, h);

		int x1 = pixelPath.getBounds().x;
		int y1 = pixelPath.getBounds().y;
		int x2 = pixelPath.getBounds().x + pixelPath.getBounds().width;
		int y2 = pixelPath.getBounds().y + pixelPath.getBounds().height;

		if (x1 < 0)
		{
			x1 = 0;
		}
		else if (x1 >= w)
		{
			x1 = w - 1;
		}
		if (y1 < 0)
		{
			y1 = 0;
		}
		else if (y1 >= h)
		{
			y1 = h - 1;
		}
		if ((x2 <= 0) || (x2 >= w))
		{
			x2 = w - 1;
		}
		if ((y2 <= 0) || (y2 >= h))
		{
			y2 = h - 1;
		}


		int rowHeight = y2 - y1;
		int minCharBreakWidth = Math.max(
				1,
				(int) ((float) rowHeight * minCharBreakWidthAsFractionOfRowHeight));
		int liberalWhitspaceMinWhitePixelsPerColumn =
				(int) ((float) rowHeight * liberalPolicyAreaWhitespaceFraction);
		// First store beginning and ending character
		// X positions and calculate average character spacing.
		ArrayList<Integer> al = new ArrayList<Integer>();
		boolean inCharSeparator = true;
		int charX1 = 0, prevCharX1 = -1;
		boolean liberalWhitespacePolicy = false;
		int numConsecutiveWhite = 0;
		for (int x = x1 + 1; x < (x2 - 1); x++)
		{
			if ((!liberalWhitespacePolicy)
					&& (numConsecutiveWhite == 0)
					&& ((x - charX1) >= rowHeight))
			{
				// Something's amiss.  No whitespace.
				// Try again but do it with the liberal whitespace
				// detection algorithm.
				x = charX1;
				liberalWhitespacePolicy = true;
			}
			int numWhitePixelsThisColumn = 0;
			boolean isWhiteSpace = true;
			for (int y = y1, idx = (y1 * w) + x; y < y2; y++, idx += w)
			{
				if (pixels[idx] >= whiteThreshold)
				{
					numWhitePixelsThisColumn++;
				}
				else
				{
					if (!liberalWhitespacePolicy)
					{
						isWhiteSpace = false;
						break;
					}
				}
			}
			if ((liberalWhitespacePolicy)
					&& (numWhitePixelsThisColumn
							< liberalWhitspaceMinWhitePixelsPerColumn))
			{
				isWhiteSpace = false;
			}
			if (isWhiteSpace)
			{
				numConsecutiveWhite++;
				if (numConsecutiveWhite >= minCharBreakWidth)
				{
					if (!inCharSeparator)
					{
						inCharSeparator = true;
						al.add(new Integer(charX1));
						al.add(new Integer(x - (numConsecutiveWhite - 1)));
					}
				}
			}
			else
			{
				numConsecutiveWhite = 0;
				if (inCharSeparator)
				{
					inCharSeparator = false;
					prevCharX1 = charX1;
					charX1 = x;
					liberalWhitespacePolicy = false;
				}
			}
		}
		if (numConsecutiveWhite == 0)
		{
			al.add(new Integer(charX1));
			al.add(new Integer(x2));
		}
		int minSpaceWidth =
				(int) ((float) rowHeight * minSpaceWidthAsFractionOfRowHeight);
		// Next combine concecutive supposed character cells where their
		// leftmost X positions are too close together.
		int minCharWidth =
				(int) ((float) rowHeight * minCharWidthAsFractionOfRowHeight);
		if (minCharWidth < 1)
		{
			minCharWidth = 1;
		}
		for (int i = 0; (i + 4) < al.size(); i += 2)
		{
			int thisCharWidth =
					(al.get(i + 2)).intValue()
					- (al.get(i)).intValue();
			if ((thisCharWidth < minCharWidth) || (thisCharWidth < 6))
			{
				al.remove(i + 2);
				al.remove(i + 1);
				i -= 2;
			}
		}
		// Process the remaining character cells.
		for (int i = 0; (i + 1) < al.size(); i += 2)
		{
			if (i >= 2)
			{
				int cx1 = (al.get(i - 1)).intValue();
				int cx2 = (al.get(i)).intValue();
				while ((cx2 - cx1) >= minSpaceWidth)
				{
					int sx2 = Math.min(cx1 + minSpaceWidth, cx2);
					RectPixelPath spacePixelPath = new RectPixelPath(cx1, y1, sx2 - cx1, y2 - y1);
					spacePixelPath.setIsSpace(true);
					pathManager.addPath(spacePixelPath);
					cx1 += minSpaceWidth;
				}
			}
			int cx1 = (al.get(i)).intValue();
			int cx2 = (al.get(i + 1)).intValue();
			int cy1 = y1;
			// Adjust cy1 down to point to the the top line which is not all white.
			while (cy1 < y2)
			{
				boolean isWhiteSpace = true;
				for (int x = cx1, idx = (cy1 * w) + cx1; x < cx2;
						x++, idx++)
				{
					if (pixels[idx] < whiteThreshold)
					{
						isWhiteSpace = false;
						break;
					}
				}
				if (!isWhiteSpace)
				{
					break;
				}
				cy1++;
			}
			int cy2 = y2;
			// Adjust cy2 up to point to the the line after the last line
			// which is not all white.
			while (cy2 > cy1)
			{
				boolean isWhiteSpace = true;
				for (int x = cx1, idx = ((cy2 - 1) * w) + cx1; x < cx2;
						x++, idx++)
				{
					if (pixels[idx] < whiteThreshold)
					{
						isWhiteSpace = false;
						break;
					}
				}
				if (!isWhiteSpace)
				{
					break;
				}
				cy2--;
			}
			if (cy1 >= cy2)
			{
				// Everything is white in this cell.  Make it a space.
				RectPixelPath spacePixelPath = new RectPixelPath(cx1, y1, cx2 - cx1, y2 - y1);
				spacePixelPath.setIsSpace(true);
				pathManager.addPath(spacePixelPath);
			}
			else
			{
				pathManager.addPath(new RectPixelPath(cx1, y1, cx2 - cx1, y2 - y1));
			}
		}
		return pathManager;
	}

}
