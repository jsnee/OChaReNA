package com.jophus.houghtransformation;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Helper class containing various methods.
 * 
 * @author SUN2K
 *
 */
final class HTUtil {

	/**
	 * Get a (deep) copy of the given image.
	 * @param srcImage source image to get 1:1 copy
	 * @return copy of given image
	 */
	public static BufferedImage getCopy(BufferedImage srcImage) 
	{
		ColorModel cm = srcImage.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = srcImage.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	/**
	 * Clips the given value into range [0, 255].
	 * @param i value to clip
	 * @return
	 */
	public static short clipByte(int i)
	{
		if (i >= 255) return (short)255;
		else if (i <= 0) return (short)0;
		else return (short)i;
	}
	
	public static short clipByte(double i)
	{
		if (i >= 255) return (short)255;
		else if (i <= 0) return (short)0;
		else return (short)i;
	}
	
}
