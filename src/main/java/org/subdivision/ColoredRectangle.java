package org.subdivision;

public class ColoredRectangle {
	Bounds bounds;
	int avarageRectangleColor;

	/**
	 * Colors if the rectange is divided into two triangles
	 */
	int upperTriangleColor;
	int lowerTriangleColor;

	/**
	 * True, if the two triangles divides the rectangle by the diagonal running from
	 * top-left corner to the bottom right corner. If false, the diagonal is running
	 * from the top-right corner to the bottom-left corner
	 */
	boolean diagonalFromTopLeftToBottomRight;

	public ColoredRectangle(
			Bounds bounds, 
			int avarageRectangleColor, 
			int upperTriangleColor, 
			int lowerTriangleColor,
			boolean diagonalFromTopLeftToBottomRight
			) {
		super();
		this.bounds = bounds;
		this.avarageRectangleColor = avarageRectangleColor;
		this.upperTriangleColor = upperTriangleColor;
		this.lowerTriangleColor = lowerTriangleColor;
		this.diagonalFromTopLeftToBottomRight = diagonalFromTopLeftToBottomRight;
	}



}