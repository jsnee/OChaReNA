package com.jophus.ocharena.image;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.RendererException;
import org.ghost4j.renderer.SimpleRenderer;

import com.jophus.houghtransformation.EHTProcessStep;
import com.jophus.houghtransformation.HTEngine;
import com.jophus.ocharena.image.path.PixelPath;

/**
 * ImagePixels class. Contains a loaded image for manipulation.
 * @author Joe Snee
 *
 */
public class ImagePixels {

	private static final Logger LOG = Logger.getLogger(ImagePixels.class.getName());

	// Cemer-Whitney algorithm coefficients
	protected static final float[] FILTER_FIR_COEFFS =
		{
		0.05001757311983922f,
		-0.06430830829693616f,
		-0.0900316316157106f,
		0.1500527193595177f,
		0.45015815807855303f,
		0.45015815807855303f,
		0.1500527193595177f,
		-0.0900316316157106f,
		-0.06430830829693616f,
		0.05001757311983922f,
		};

	private int imgWidth;
	private int imgHeight;
	private int maxPixelValue = 255;
	private int[] pixels;
	private int npix;
	private Color averageColor = new Color(0, 0, 0);
	private int brightestColor = new Color(0, 0, 0).getRGB();
	private DocumentMetadata metadata = new DocumentMetadata();

	/**
	 * Constructor.
	 * @param width
	 * @param height
	 */
	public ImagePixels(int width, int height) {
		this.pixels = new int[height * width];
		this.imgWidth = width;
		this.imgHeight = height;
		npix = width * height;
	}

	/**
	 * Constructor.
	 * @param filename
	 */
	public ImagePixels(String filename) {
		File img = new File(filename);
		try {
			loadPixelsFromFile(img);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Constructor.
	 * @param filename
	 * @param muteBlue
	 */
	public ImagePixels(String filename, boolean muteBlue) {
		File img = new File(filename);
		metadata.setBlueLinedPaper(muteBlue);
		try {
			loadPixelsFromFile(img);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Constructor.
	 * @param filename
	 * @param metadata
	 */
	public ImagePixels(String filename, DocumentMetadata metadata) {
		File img = new File(filename);
		this.metadata = metadata;
		try {
			loadPixelsFromFile(img);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Constructor.
	 * @param file
	 */
	public ImagePixels(File file) {
		try {
			loadPixelsFromFile(file);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Constructor.
	 * @param bimg
	 */
	public ImagePixels(BufferedImage bimg) {
		loadPixelsFromBufferedImage(bimg);
	}

	/**
	 * Constructor.
	 * @param bimg
	 * @param metadata
	 */
	public ImagePixels(BufferedImage bimg, DocumentMetadata metadata) {
		this.metadata = metadata;
		loadPixelsFromBufferedImage(bimg);
	}

	/**
	 * Get the ImagePixels contained within a PixelPath
	 * @param path The PixelPath
	 * @return The pixels contained within
	 */
	public ImagePixels getPixelsFromPixelPath(PixelPath path) {
		Rectangle bounds = path.getBounds();
		ImagePixels result = new ImagePixels(bounds.width, bounds.height);
		for (int i = bounds.x; i < bounds.x + bounds.width; i++) {
			for (int j = bounds.y; j < bounds.y + bounds.height; j++) {
				result.setPixel(i - bounds.x, j - bounds.y, getPixel(i, j));
			}
		}
		return result;
	}

	/**
	 * Set the document metadata
	 * @param metadata
	 */
	public void setMetadata(DocumentMetadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * Mute the blue component
	 * @param rgb
	 * @return
	 */
	public static int muteBlue(int rgb) {
		Color color = new Color(rgb);
		int avgRG = ((color.getRed() + color.getGreen()) / 2);
		return (color.getBlue() > avgRG ? Color.white : color).getRGB();
	}

	/**
	 * Mute the red component
	 * @param rgb
	 * @return
	 */
	public static int muteRed(int rgb) {
		Color color = new Color(rgb);
		int avgBG = ((color.getBlue() + color.getGreen()) / 2);
		return (color.getRed() > avgBG ? Color.white : color).getRGB();
	}

	/**
	 * Loads ImagePixels from a file
	 * @param img The image or PDF file
	 * @throws IOException
	 */
	public void loadPixelsFromFile(File img) throws IOException {
		String filename = img.getAbsolutePath();
		if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".PNG") || filename.endsWith(".JPG") || filename.endsWith(".JPEG")) {
			// Load the image
			loadPixelsFromBufferedImage(ImageIO.read(new File(filename)));
		} else if (filename.endsWith(".pdf") || filename.endsWith(".PDF")) {
			// Load the PDF
			try {
				PDFDocument pdf = new PDFDocument();
				pdf.load(img);

				SimpleRenderer renderer = new SimpleRenderer();
				renderer.setResolution(300);
				List<Image> pages = renderer.render(pdf);

				BufferedImage eachPage = ((BufferedImage)pages.get(0));
				// Set the image dimensions
				this.imgWidth = eachPage.getWidth();
				this.imgHeight = eachPage.getHeight() * pages.size();
				npix = imgWidth * imgHeight;
				// Initialize the pixel array
				pixels = new int[npix];
				// Load each page into the pixel array
				for (int i = 0; i < pages.size(); i++) {
					if (i != 0) {
						eachPage = ((BufferedImage)pages.get(i));
					}
					for (int y = eachPage.getMinY(); y < eachPage.getMinY() + eachPage.getHeight(); y++) {
						for (int x = eachPage.getMinX(); x < eachPage.getMinX() + this.imgWidth; x++) {
							pixels[i * this.imgWidth + y * this.imgWidth + x] = eachPage.getRGB(x, y);
							adjustAverageColor(eachPage.getRGB(x, y));
							brightestColor = Math.max(brightestColor, eachPage.getRGB(x, y));
						}
					}
				}
			} catch (DocumentException | RendererException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads ImagePixels from a BufferedImage
	 * @param bimg The BufferedImage to load
	 */
	private void loadPixelsFromBufferedImage(BufferedImage bimg) {
		// Set the image dimensions
		this.imgWidth = bimg.getWidth();
		this.imgHeight = bimg.getHeight();
		npix = imgWidth * imgHeight;
		// Initialize the pixel array
		pixels = new int[this.imgWidth * this.imgHeight];
		// Load the pixels into the array
		for (int y = bimg.getMinY(); y < bimg.getMinY() + this.imgHeight; y++) {
			for (int x = bimg.getMinX(); x < bimg.getMinX() + this.imgWidth; x++) {
				pixels[y * this.imgWidth + x] = bimg.getRGB(x, y);
				adjustAverageColor(bimg.getRGB(x, y));
				brightestColor = Math.max(brightestColor, bimg.getRGB(x, y));
			}
		}
	}

	/**
	 * Updates the stored average color by including the specified value in the average
	 * @param rgbValue The pixel value to add to the average
	 */
	private void adjustAverageColor(int rgbValue) {
		Color color = new Color(rgbValue);
		int r = (averageColor.getRed() + color.getRed()) / 2;
		int g = (averageColor.getGreen() + color.getGreen()) / 2;
		int b = (averageColor.getBlue() + color.getBlue()) / 2;
		averageColor = new Color(r, g, b);
	}

	/**
	 * Performs a RGB filter pass on a pixel value
	 * @param rgbValue The pixel value to filter
	 * @return The filtered pixel value
	 */
	private int filterOnRGB(int rgbValue) {
		Color color = new Color(rgbValue);
		double avgRG = Math.floor((double)averageColor.getRed() / (double)averageColor.getGreen() * 100) / 100;
		double avgRB = Math.floor((double)averageColor.getRed() / (double)averageColor.getBlue() * 100) / 100;
		double avgGB = Math.floor((double)averageColor.getGreen() / (double)averageColor.getBlue() * 100) / 100;

		double ratioRG = Math.floor((double)color.getRed() / (double)color.getGreen() * 100) / 100;
		double ratioRB = Math.floor((double)color.getRed() / (double)color.getBlue() * 100) / 100;
		double ratioGB = Math.floor((double)color.getGreen() / (double)color.getBlue() * 100) / 100;

		if (avgRG == ratioRG || avgRB == ratioRB || avgGB == ratioGB) {
			return Color.white.getRGB();
		}
		return rgbValue;
	}

	/**
	 * Performs an HSV filter pass on a pixel value
	 * @param rgbValue The pixel value to filter
	 * @return The filtered pixel value
	 */
	private int filterOnHSV(int rgbValue) {
		HSVColor difference = ColorUtils.hsvDifference(averageColor, new Color(rgbValue));
		if (difference.getHue() < 10 || difference.getSaturation() < 0.1f || difference.getValue() < 0.1f) {
			return new Color(rgbValue).brighter().getRGB();
		}
		return rgbValue;
	}

	/**
	 * Inverts the image's colors
	 */
	public void invertColors() {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = ColorUtils.invertColor(pixels[i]); 
		}
	}

	/**
	 * Get the brightest color
	 * @return The brightest color
	 */
	public int getWhitestColor() {
		return brightestColor;
	}

	/**
	 * Perform the specified Hough transformation steps
	 * @param steps
	 * @return
	 */
	public BufferedImage performHoughTransform(ArrayList<EHTProcessStep> steps) {
		HTEngine htEngine = new HTEngine();
		htEngine.setSourceImage(getImageAsBufferedImage());
		return htEngine.getHTProcessSteps(steps).getImage();
	}



	/**
	 * Preprocessor filters
	 */
	public void prepareImage() {
		if (brightestColor != -1) {
			ArrayList<EHTProcessStep> steps = new ArrayList<EHTProcessStep>();
			steps.add(EHTProcessStep.STEP_GRAYSCALE);
			steps.add(EHTProcessStep.STEP_EDGE_DETECTION);
			//steps.add(EHTProcessStep.STEP_EDGE_TRESHOLD);
			loadPixelsFromBufferedImage(performHoughTransform(steps));
			invertColors();
		} else {
			//filterPixelValues();
		}
	}
	
	/**
	 * Clean up lined paper and nonideal lighting
	 * NEVER FULLY IMPLEMENTED 
	 */
	public void filterPixelValues() {
		for (int i = 0; i < pixels.length; i++) {
			if (!metadata.isUniformBackgroundColor()) {
				//pixels[i] = filterOnHSV(pixels[i]);
				//pixels[i] = filterOnRGB(pixels[i]);
			}
			if (metadata.isRedMarginPaper()) {
				pixels[i] = muteRed(pixels[i]);
			}
			if (metadata.isBlueLinedPaper()) {
				pixels[i] = muteBlue(pixels[i]);
			}
		}
	}

	/**
	 * Get the pixel values
	 * @return The pixel array
	 */
	public int[] getPixelValues() {
		return this.pixels;
	}

	/**
	 * Get the image width
	 * @return The image width
	 */
	public int getImageWidth() {
		return this.imgWidth;
	}

	/**
	 * Get the image height
	 * @return The image height
	 */
	public int getImageHeight() {
		return this.imgHeight;
	}

	/**
	 * Get the maximum pixel value
	 * @return The maximum pixel value
	 */
	public int getMaxPixelValue() {
		return this.maxPixelValue;
	}

	/**
	 * Get an entire row of pixels from the image
	 * @param yIndex The y position
	 * @return The row at position y
	 */
	public int[] getPixelRow(int yIndex) {
		int[] result = new int[this.imgWidth];
		for (int i = 0; i < this.imgWidth; i++) {
			result[i] = pixels[yIndex * this.imgWidth + i];
		}
		return result;
	}

	/**
	 * Get the pixel by index position in the array
	 * @param index The pixel's index
	 * @return the value of the pixel
	 */
	public int getPixelByIndex(int index) {
		return pixels[index];
	}

	/**
	 * Get the index of a pixel at a specific x,y position.
	 * @param x The pixel's x position.
	 * @param y The pixel's y position.
	 * @return The pixel index (the index into the <code>pixels</code> array)
	 * of the pixel.
	 */
	public final int getPixelIndex(int x, int y)
	{
		return (y * imgWidth) + x;
	}

	/**
	 * Get the value of a pixel at a specific x,y position.
	 * @param x The pixel's x position.
	 * @param y The pixel's y position.
	 * @return The value of the pixel.
	 */
	public final int getPixel(int x, int y)
	{
		return pixels[(y * imgWidth) + x];
	}

	/**
	 * Sets the pixel value based on coordinate
	 * @param x The pixel's x position.
	 * @param y The pixel's y position.
	 * @param val
	 */
	public void setPixel(int x, int y, int val) {
		pixels[y * this.imgWidth + x] = val;
	}

	/**
	 * Sets the pixel color based on coordinate
	 * @param x The pixel's x position.
	 * @param y The pixel's y position.
	 * @param color
	 */
	public void setPixel(int x, int y, Color color) {
		pixels[y * this.imgWidth + x] = color.getRGB();
	}

	/**
	 * Sets the pixel value based on index
	 * @param index
	 * @param rgbValue
	 */
	public void setPixel(int index, int rgbValue) {
		pixels[index] = rgbValue;
	}

	/**
	 * Sets the pixel values for the entire row
	 * @param yIndex
	 * @param rowPixelValues
	 */
	public void setRowPixelValues(int yIndex, int[] rowPixelValues) {
		for (int x = 0; x < rowPixelValues.length; x++) {
			pixels[yIndex * this.imgWidth + x] = rowPixelValues[x];
		}
	}

	@Deprecated
	/**
	 * Sets all of the pixels in a row to a certain color
	 * @param yIndex
	 * @param rgbValue
	 */
	public void setRowPixelColor(int yIndex, int rgbValue) {
		for (int x = 0; x < this.imgWidth; x++) {
			pixels[yIndex * this.imgWidth + x] = rgbValue;
		}
	}

	/**
	 * Returns a BufferedImage representation of the ImagePixels
	 * @return
	 */
	public BufferedImage getImageAsBufferedImage() {
		BufferedImage result = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < this.imgHeight; y++) {
			for (int x = 0; x < this.imgWidth; x++) {
				result.setRGB(x, y, pixels[y * this.imgWidth + x]);
			}
		}
		return result;
	}

	/**
	 * Extracts rows of an image as a new image 
	 * @param yIndex a list of the y values to extract
	 * @return
	 */
	public ImagePixels getRowsAsSubimage(int[] yIndex) {
		ImagePixels result = new ImagePixels(this.imgWidth, yIndex.length);
		for (int i = 0; i < yIndex.length; i++) {
			result.setRowPixelValues(i, this.getPixelRow(yIndex[i]));
		}
		return result;
	}

	/**
	 * A different grayscale converter
	 */
	public void convertPixelsToGrayscale() {
		for (int y = 0; y < this.imgHeight; y++) {
			for (int x = 0; x < this.imgWidth; x++) {
				Color color = new Color(pixels[y * this.imgWidth + x]);
				int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
				pixels[y * this.imgWidth + x] = (new Color(avg, avg, avg)).getRGB();
			}
		}
	}

	/**
	 * Convert all pixels to grayscale from RGB or RGBA.
	 * Do not call this method if the pixels are not currently RGB or RGBA.
	 * @param normalize true to normalize the image after converting to
	 * grayscale, such that the darkest pixel in the image is all black and the lightest
	 * pixel in the image is all white.
	 */
	public final void toGrayScale(boolean normalize)
	{
		if (npix == 0)
		{
			return;
		}
		if (!normalize)
		{
			for (int i = 0; i < npix; i++)
			{
				pixels[i] = rgbToGrayScale(pixels[i]);
			}
		}
		else
		{
			int pix;
			pixels[0] = pix = rgbToGrayScale(pixels[0]);
			int min = pix, max = pix;
			for (int i = 1; i < npix; i++)
			{
				pixels[i] = pix = rgbToGrayScale(pixels[i]);
				min = Math.min(min, pix);
				max = Math.max(max, pix);
			}
			int range = max - min;
			if (range < 1)
			{
				for (int i = 0; i < npix; i++)
				{
					pixels[i] = 255;
				}
			}
			else
			{
				for (int i = 0; i < npix; i++)
				{
					pixels[i] =
							Math.min(255,
									Math.max(0,
											((pixels[i]
													- min) * 255) / range));
				}
			}
		}
	}

	/**
	 * Convert a single pixel to grayscale
	 * @param pix
	 * @return
	 */
	private static int rgbToGrayScale(int pix)
	{
		int r = (pix >> 16) & 0xff;
		int g = (pix >> 8) & 0xff;
		int b = pix & 0xff;
		int Y = ((r * 306) + (g * 601) + (b * 117)) >> 10;
			if (Y < 0)
			{
				Y = 0;
			}
			else if (Y > 255)
			{
				Y = 255;
			}
			return Y;
	}

	/**
	 * Character identification preprocessing
	 */
	public final void filter()
	{
		filter(pixels, imgWidth, imgHeight);
	}

	/**
	 * Cemer-Whitney algorithm preprocessing
	 * @param pixels
	 * @param width
	 * @param height
	 */
	private final void filter(int[] pixels, int width, int height)
	{
		float[] firSamples = new float[FILTER_FIR_COEFFS.length];
		float c;
		int lastPos = firSamples.length - 1;
		// Filter horizontally.
		for (int y = 0; y < height; y++)
		{
			for (int i = 0; i < firSamples.length; i++)
			{
				firSamples[i] = 255.0f;
			}
			int outX = -(firSamples.length / 2);
			for (int x = 0; x < width; x++, outX++)
			{
				c = 0.0f;
				for (int j = 0; j < lastPos; j++)
				{
					c += (firSamples[j] * FILTER_FIR_COEFFS[j]);
					firSamples[j] = firSamples[j + 1];
				}
				c += (firSamples[lastPos] * FILTER_FIR_COEFFS[lastPos]);
				firSamples[lastPos] = getPixel(x, y);
				if (c < 0.0f)
				{
					c = 0.0f;
				}
				else if (c > 255.0f)
				{
					c = 255.0f;
				}
				if (outX >= 0)
				{
					pixels[getPixelIndex(outX, y)] = (int) c;
				}
			}
			while (outX < width)
			{
				c = 0.0f;
				for (int j = 0; j < lastPos; j++)
				{
					c += (firSamples[j] * FILTER_FIR_COEFFS[j]);
					firSamples[j] = firSamples[j + 1];
				}
				c += (firSamples[lastPos] * FILTER_FIR_COEFFS[lastPos]);
				firSamples[lastPos] = 255.0f;
				if (c < 0.0f)
				{
					c = 0.0f;
				}
				else if (c > 255.0f)
				{
					c = 255.0f;
				}
				pixels[getPixelIndex(outX, y)] = (int) c;
				outX++;
			}
		}
		// Filter vertically.
		for (int x = 0; x < width; x++)
		{
			for (int i = 0; i < firSamples.length; i++)
			{
				firSamples[i] = 255.0f;
			}
			int outY = -(firSamples.length / 2);
			for (int y = 0; y < height; y++, outY++)
			{
				c = 0.0f;
				for (int j = 0; j < lastPos; j++)
				{
					c += (firSamples[j] * FILTER_FIR_COEFFS[j]);
					firSamples[j] = firSamples[j + 1];
				}
				c += (firSamples[lastPos] * FILTER_FIR_COEFFS[lastPos]);
				firSamples[lastPos] = getPixel(x, y);
				if (c < 0.0f)
				{
					c = 0.0f;
				}
				else if (c > 255.0f)
				{
					c = 255.0f;
				}
				if (outY >= 0)
				{
					pixels[getPixelIndex(x, outY)] = (int) c;
				}
			}
			while (outY < height)
			{
				c = 0.0f;
				for (int j = 0; j < lastPos; j++)
				{
					c += (firSamples[j] * FILTER_FIR_COEFFS[j]);
					firSamples[j] = firSamples[j + 1];
				}
				c += (firSamples[lastPos] * FILTER_FIR_COEFFS[lastPos]);
				firSamples[lastPos] = 255.0f;
				if (c < 0.0f)
				{
					c = 0.0f;
				}
				else if (c > 255.0f)
				{
					c = 255.0f;
				}
				pixels[getPixelIndex(x, outY)] = (int) c;
				outY++;
			}
		}
	}
}
