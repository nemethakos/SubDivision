package org.subdivision;

public class Bounds {
	Point a;
	Point b;

	public Bounds(int ax, int ay, int bx, int by) {
		super();
		this.a = new Point(ax, ay);
		this.b = new Point(bx, by);

	}

	public int area() {
		if (isEmpty()) {
			return 0;
		} else {
			return getWidth() * getHeight();
		}
	}

	public int getHeight() {
		return b.y - a.y + 1;

	}

	public int getWidth() {
		return b.x - a.x + 1;
	}

	public boolean isEmpty() {
		return (getWidth() == 0 || getHeight() == 0);
	}

	public boolean isValid() {

		return (a.x <= b.x && a.y <= b.y);

	}

	@Override
	public String toString() {
		return String.format("%s-%s", a, b);
	}

}