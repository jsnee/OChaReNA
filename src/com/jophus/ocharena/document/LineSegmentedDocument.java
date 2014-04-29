package com.jophus.ocharena.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;

import com.jophus.ocharena.Ocharena;
import com.jophus.ocharena.image.ImagePixels;
import com.jophus.util.JophIOUtils;

public class LineSegmentedDocument {

	private List<DocumentLine> lines = new ArrayList<DocumentLine>();
	private ScannedDocument doc;

	public LineSegmentedDocument(ScannedDocument doc, boolean[] lines) {
		this.doc = doc;
		this.loadLines(lines);
	}
	
	public ImagePixels getSegLinePixels(int lineNum) {
		return lines.get(lineNum).getLinePixels();
	}

	private void loadLines(boolean[] lines) {

		boolean isLine = false;
		List<Integer> yLine = new ArrayList<Integer>();
		for (int i = 0; i < lines.length; i++) {
			if (isLine) {
				if (lines[i]) {
					yLine.add(i);
					continue;
				} else {
					isLine = false;
					this.lines.add(new DocumentLine(doc.getImagePixels(), ArrayUtils.toPrimitive(yLine.toArray(new Integer[0]))));
					yLine.clear();
				}

			} else if (lines[i]) {
				isLine = true;
				yLine.clear();
				yLine.add(i);
			}
		}
	}

	public void archiveLineImgs() {
		LOG.fine("Archiving Document Lines");
		File dir = new File("lineImages" + File.separator);
		dir.mkdir();
		dir.deleteOnExit();
		for (int i = 0; i < this.lines.size(); i++) {
			try {
				LOG.finest("Creating Line Image #" + i + " of " + this.lines.size());
				File tmp = new File("lineImages" + File.separator + "line_" + i + ".png");
				tmp.createNewFile();
				if (!Ocharena.DEBUGMODE) tmp.deleteOnExit();
				ImageIO.write(lines.get(i).getLineImage(), "png", tmp);
			} catch (IOException e) {
				e.printStackTrace();
				LOG.log(Level.SEVERE, null, e);
			}
		}
		LOG.finer("Writing Archives");
		doc.addDirToArchive(dir);
		JophIOUtils.deleteFiles(dir);
	}

	private static final Logger LOG = Logger.getLogger(LineSegmentedDocument.class.getName());
}
