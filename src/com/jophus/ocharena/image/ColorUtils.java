package com.jophus.ocharena.image;

import java.awt.Color;

public class ColorUtils {

	public enum ColorComponent {
		RED,
		GREEN,
		BLUE
	}
	
	public static void printRGBValues(int rgbColor) {
		Color color = new Color(rgbColor);
		System.out.println("RGB: [" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + "]");
	}
	
	public static double getStandardLuminance(int rgbColor) {
		Color color = new Color(rgbColor);
		return (0.2126d * color.getRed()) + (0.7152d * color.getGreen()) + (0.0722d * color.getBlue());
	}
	
	public static int invertColor(int rgbColor) {
		Color color = new Color(rgbColor);
		return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()).getRGB();
	}
	
	public static int invertColor2(int rgbColor) {
		Color color = new Color(rgbColor);
		return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()).getRGB();
	}
	
	public static double getSimplePerceivedLuminance(int rgbColor) {
		Color color = new Color(rgbColor);
		return (0.299d * color.getRed() + 0.587d * color.getGreen() + 0.114d * color.getBlue());
	}
	
	public static HSVColor rgbToHsvColor(Color color) {
		float[] hsv = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
		return new HSVColor(hsv[0], hsv[1], hsv[2]);
	}
	
	public static HSVColor hsvDifference(Color color1, Color color2) {
		HSVColor hsv1 = rgbToHsvColor(color1);
		HSVColor hsv2 = rgbToHsvColor(color2);
		int avgHue = Math.abs(hsv1.getHue() - hsv2.getHue());
		float avgSaturation = Math.abs(hsv1.getSaturation() - hsv2.getSaturation());
		float avgValue = Math.abs(hsv1.getValue() - hsv2.getValue());
		return new HSVColor(avgHue, avgSaturation, avgValue);
	}

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

	public static int getRed(int rgbValue) {
		return (new Color(rgbValue).getRed());
	}

	public static int getGreen(int rgbValue) {
		return (new Color(rgbValue).getGreen());
	}

	public static int getBlue(int rgbValue) {
		return (new Color(rgbValue).getBlue());
	}

	public static int replaceBlue(int rgbValue, Color replaceColor) {
		Color color = new Color(rgbValue);
		int avgRG = ((color.getRed() + color.getGreen()) / 2);
		return (color.getBlue() > avgRG ? replaceColor : color).getRGB();
	}

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
