package org.subdivision;

public class ColorAggregator {
	long blue;
	long count;
	long green;
	long red;

	public void add(int color) {
		red += (color >> 16) & 0xFF;
		green += (color >> 8) & 0xFF;
		blue += (color & 0xFF);
		count++;
	}

	public int getAvarageColor() {
		if (count > 0) {
			red = red / count;
			green = green / count;
			blue = blue / count;
		}

		return (int) ((red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF);
	}

}