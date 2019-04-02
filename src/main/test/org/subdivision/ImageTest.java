package org.subdivision;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class ImageTest {

	//@Test
	void testImage() throws IOException {
	/*	Image img = new Image("C:\\workspace\\SubDivision\\src\\4x4.png", 1);
		System.out.println(img);*/
	}

	@Test
	void testMakeGrayScale() {
		// fail("Not yet implemented");
	}

	//@Test
/*	void testGetColumnValues() throws IOException {
		Image img = new Image("C:\\workspace\\SubDivision\\src\\4x4bw.png");
		var cv = img.getColumnValues(img.grayPixels, img.width, img.height, 0, 0, img.width - 1, img.height - 1);
		System.out.println(cv);
	}
*/
	private long sum(int values[]) {
		long sum = 0;
		for (int value : values) {
			sum += value;
		}
		return sum;
	}

	long getSubSum(int[] array, int index, boolean left) {
		long sum = 0;
		int start;
		int end;

		if (left) {
			start = 0;
			end = index;
		} else {
			start = index + 1;
			end = array.length - 1;
		}

		for (int i = start; i <= end; i++) {
			sum += array[i];
		}

		return sum;
	}

	String getArrayStr(int[] array, int index) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			if (index == i) {
				sb.append("*");
			}
			sb.append(", ");
		}
		return sb.substring(0, sb.length() - 2).toString();
	}

	void printBalanceIndex(int... values) {
/*		SummedValues summedValues = new SummedValues(values.length);
		summedValues.energies = values;
		summedValues.energySum = sum(values);
		
		int index = Image.getDivisonPoint(summedValues);
		long left = getSubSum(values, index, true);
		long right = getSubSum(values, index, false);
		System.out.format("%s(%d:%d => %d)\n", getArrayStr(values, index), left, right, left - right);
*/
	}

	//@Test
	void testGetBalanceIndex() {
		int[] values = { 1, 2, 3, 4, 5, 6 };

		printBalanceIndex(1, 1, 1, 1, 1, 1);
		printBalanceIndex(1, 2, 3, 4, 5, 6);
		printBalanceIndex(0, 0, 0, 0, 0, 9);
		printBalanceIndex(0, 0, 0, 0, 0, 0);
		printBalanceIndex(1, 1, 1, 1, 1, 9);
		printBalanceIndex(1, 0, 1, 0, 1, 0);
		printBalanceIndex(100, 90, 80, 70, 60, 50);

	}
/*
	@Test
	void testGetBalancePoint() throws IOException {
		Image img = new Image("C:\\workspace\\SubDivision\\src\\circle.png");
		
		var cv = img.getColumnValues(img.grayPixels, img.width, img.height, 0, 0, img.width - 1, img.height - 1);
		printBalanceIndex(cv.columnValues);
		printBalanceIndex(cv.rowValues);
		var bp = img.getBalancePoint(cv);
		System.out.println(bp);
		System.out.println(img.toString(bp.balancePointX, bp.balancePointY));
	}
	*/
	@Test
	void drawImage() {
		
	}

}
