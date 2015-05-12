package com.jophus.ocharena.image;

import java.awt.Color;

/**
 * Utility class for manipulation of color channels
 * @author Joe Snee
 *
 */
public class ColorUtils {

	public enum ColorComponent {
		RED,
		GREEN,
		BLUE
	}
	
	/**
	 * Debugging method. Prints to console, RGB values
	 * @param rgbColor
	 */
	public static void printRGBValues(int rgbColor) {
		Color color = new Color(rgbColor);
		System.out.println("RGB: [" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + "]");
	}
	
	/**
	 * Get the standard luminance of the pixel based on its color
	 * @param rgbColor
	 * @return the luminance
	 */
	public static double getStandardLuminance(int rgbColor) {
		Color color = new Color(rgbColor);
		return (0.2126d * color.getRed()) + (0.7152d * color.getGreen()) + (0.0722d * color.getBlue());
	}
	
	/**
	 * Inverts the color
	 * @param rgbColor
	 * @return the inverted value
	 */
	public static int invertColor(int rgbColor) {
		Color color = new Color(rgbColor);
		return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()).getRGB();
	}
	
	/**
	 * Gets the simple perceived luminance
	 * @param rgbColor
	 * @return the luminance value
	 */
	public static double getSimplePerceivedLuminance(int rgbColor) {
		Color color = new Color(rgbColor);
		return (0.299d * color.getRed() + 0.587d * color.getGreen() + 0.114d * color.getBlue());
	}
	
	/**
	 * Converts RGB color space to HSV color space
	 * @param color
	 * @return the HSV color
	 */
	public static HSVColor rgbToHsvColor(Color color) {
		float[] hsv = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
		return new HSVColor(hsv[0], hsv[1], hsv[2]);
	}
	
	/**
	 * Calculate the color difference in HSV color space
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static HSVColor hsvDifference(Color color1, Color color2) {
		HSVColor hsv1 = rgbToHsvColor(color1);
		HSVColor hsv2 = rgbToHsvColor(color2);
		int avgHue = Math.abs(hsv1.getHue() - hsv2.getHue());
		float avgSaturation = Math.abs(hsv1.getSaturation() - hsv2.getSaturation());
		float avgValue = Math.abs(hsv1.getValue() - hsv2.getValue());
		return new HSVColor(avgHue, avgSaturation, avgValue);
	}

	/**
	 * Converts HSV color space to RGB color space
	 * @param hsvColor
	 * @return the RGB color
	 */
	public static Color hsvToRgbColor(HSVColor hsvColor) {
		int h = hsvColor.getHue();
		float s = hsvColor.getSaturation();
		float v = hsvColor.getValue();
		
		int ndx = hsvColor.getHue() / 60;
		float f = h/60f - h/60;
		float p = v*(1-s);
		float q = v*(1-f*s);
		float t = v*(1-(1-f)*s);
		switch (ndx) {
			case 0:
				return new Color(v, t, p);
			case 1:
				return new Color(q, v, p);
			case 2:
				return new Color(p, v, t);
			case 3:
				return new Color(p, q, v);
			case 4:
				return new Color(t, p, v);
			case 5:
				return new Color(v, p, q);
			default:
				return new Color(0, 0, 0);
		}
	}

	/**
	 * Get the red component of an RGB color
	 * @param rgbValue
	 * @return
	 */
	public static int getRed(int rgbValue) {
		return (new Color(rgbValue).getRed());
	}

	/**
	 * Get the green component of an RGB color
	 * @param rgbValue
	 * @return
	 */
	public static int getGreen(int rgbValue) {
		return (new Color(rgbValue).getGreen());
	}

	/**
	 * Get the blue component of an RGB color
	 * @param rgbValue
	 * @return
	 */
	public static int getBlue(int rgbValue) {
		return (new Color(rgbValue).getBlue());
	}

	/**
	 * Replace blue with another color
	 * @param rgbValue
	 * @param replaceColor
	 * @return
	 */
	public static int replaceBlue(int rgbValue, Color replaceColor) {
		Color color = new Color(rgbValue);
		int avgRG = ((color.getRed() + color.getGreen()) / 2);
		return (color.getBlue() > avgRG ? replaceColor : color).getRGB();
	}

	/**
	 * Generic color replacement algorithm
	 * @param rgbValue
	 * @param replaceColor
	 * @param targetColor
	 * @return
	 */
	public static int replaceColor(int rgbValue, Color replaceColor, ColorComponent targetColor) {
		Color color = new Color(rgbValue);
		switch (targetColor) {
		case RED:
			int avgGB = (color.getGreen() + color.getBlue());
			return (color.getRed() > avgGB ? replaceColor : color).getRGB();
		case GREEN:
			int avgRB = ((color.getRed() + color.getBlue()) / 2);
			return (color.getGreen() > avgRB ? replaceColor : color).getRGB();
		case BLUE:
			int avgRG = ((color.getRed() + color.getGreen()) / 2);
			return (color.getBlue() > avgRG ? replaceColor : color).getRGB();
		default:
			return 0;
		}
	}

}
