package com.jophus.ocharena.image;

public class HSVColor {
	private int hue;
	private float saturation;
	private float value;

	public HSVColor(int hue, float saturation, float value) {
		this.hue = hue;
		this.saturation = saturation;
		this.value = value;
	}
	
	public HSVColor(float hue, float saturation, float value) {
		this.hue = (int)(hue * 360);
		this.saturation = saturation;
		this.value = value;
	}

	public int getHue() {
		return hue;
	}
	
	public void setHue(int hue) {
		this.hue = hue;
	}

	public float getSaturation() {
		return saturation;
	}
	
	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public float getValue() {
		return value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
}
