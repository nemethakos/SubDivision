package org.subdivision;
public class PixelValues {
	Bounds bounds;
	SummedValues columnValues;
	SummedValues rowValues;

	public PixelValues(Bounds bounds, SummedValues columnValues, SummedValues rowValues) {

		super();
		this.bounds = bounds;
		this.columnValues = columnValues;
		this.rowValues = rowValues;
	}

	@Override
	public String toString() {
		return String.format("PixelValues [bounds: %s,\n columnValues=%s,\n rowValues=%s]\n", bounds, columnValues,
				rowValues);
	}

}