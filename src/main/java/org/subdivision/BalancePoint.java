package org.subdivision;

public class BalancePoint {
	Bounds bounds;
	PixelValues pixelValues;
	Point point;

	public BalancePoint(PixelValues pixelValues, Point point) {
		super();
		this.bounds = pixelValues.bounds;
		this.pixelValues = pixelValues;
		this.point = point;
	}

	@Override
	public String toString() {
		return String.format("BalancePoint [point=%s, bounds=%s, pixelValues=%s]", point, bounds, pixelValues);
	}

}