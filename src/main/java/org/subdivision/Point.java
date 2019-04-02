package org.subdivision;

public class Point {
	int x;
	int y;

	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", x, y);
	}

}