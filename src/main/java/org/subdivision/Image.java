package org.subdivision;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;

import org.subdivision.tool.Configuration;
import org.subdivision.tool.ImageDisplayComponent;

public class Image {

	int[] colorPixels;

	float[] energyMap;
	public int height;

	BufferedImage input;
	float maxEnergy = 0;

	// int minSizeToDivide;
	String name;
	public BufferedImage output;
	Graphics2D outputG2;

	String outputPrefix;

	// float treshold;

	Deque<ColoredRectangle> queue = new LinkedList<>();

	RenderMethod renderMethod;

	RenderProperties renderProperties;

	Random rnd = new Random();

	public int width;

	public Image(BufferedImage inputImage, RenderProperties renderProperties) {
		this.input = inputImage;
		initImage("", renderProperties);
	}

	public Image(String fileName, RenderProperties renderProperties) throws IOException {
		input = ImageIO.read(new File(fileName));

		initImage(fileName, renderProperties);

	}

	private void initImage(String fileName, RenderProperties renderProperties) {
		this.renderProperties = renderProperties;
		this.renderMethod = renderProperties.getRenderMethod();
		// this.minSizeToDivide = renderProperties.getMinAreaToDivide();
		// this.treshold = renderProperties.getTreshold();
		this.name = fileName;

		width = input.getWidth();
		height = input.getHeight();

		colorPixels = new int[width * height];
		input.getRGB(0, 0, width, height, colorPixels, 0, width);

		energyMap = getEnergyMap();
		 saveImage(getGradientImage(),
		 Configuration.getInstance().getFullOutputDir("energy.png")+"/energy.png");

		output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		reset();
	}

	public List<ColoredRectangle> divide(ColoredRectangle rectangle) {
		List<ColoredRectangle> result = new ArrayList<>();

		if (!rectangle.bounds.isValid() || rectangle.bounds.isEmpty()
				|| rectangle.bounds.area() < renderProperties.minAreaToDivide) {
			return result;
		}

		// if (rectangle.bounds.getWidth() > 10 && rectangle.bounds.getHeight() > 10) {
		// System.out.println("Divide rectangle: "+rectangle + "...");
		BalancePoint balancePoint = getBalancePoint(rectangle.bounds);
		// System.out.println("balancePoint:" + balancePoint);
		result = divideByBalancePoint(rectangle, balancePoint);
//		}

		return result;
	}

	public List<ColoredRectangle> divideByBalancePoint(ColoredRectangle coloredRectangle, BalancePoint balancePoint) {
		List<ColoredRectangle> result = new ArrayList<>();

		// System.out.println("Original rectangle: " + balancePoint.bounds);

		ColorProperties globalColorProperties = getColorAvarage(balancePoint.bounds,
				coloredRectangle.diagonalFromTopLeftToBottomRight);
		if (!globalColorProperties.hasTreshold(renderProperties.treshold)) {
			return result;
		}

		// divide by longer edge
		if (balancePoint.bounds.getWidth() > balancePoint.bounds.getHeight()) {
			// divide horizontaly
			Bounds left = new Bounds(//
					balancePoint.bounds.a.x, //
					balancePoint.bounds.a.y, //
					balancePoint.point.x, //
					balancePoint.bounds.b.y);//
			if (left.isValid()) {

				ColorProperties colorProperties = getColorAvarage(left,
						coloredRectangle.diagonalFromTopLeftToBottomRight);
				result.add(new ColoredRectangle(left, colorProperties.avarageColor,
						colorProperties.triangleAvarageUpperColor, colorProperties.triangleAvarageLowerColor,
						!coloredRectangle.diagonalFromTopLeftToBottomRight));
			}

			// top-right
			Bounds right = new Bounds(//
					balancePoint.point.x + 1, //
					balancePoint.bounds.a.y, //
					balancePoint.bounds.b.x, //
					balancePoint.bounds.b.y);//
			if (right.isValid()) {

				ColorProperties colorProperties = getColorAvarage(right,
						coloredRectangle.diagonalFromTopLeftToBottomRight);
				result.add(new ColoredRectangle(right, colorProperties.avarageColor,
						colorProperties.triangleAvarageUpperColor, colorProperties.triangleAvarageLowerColor,
						!coloredRectangle.diagonalFromTopLeftToBottomRight));
			}

		} else {
			// divide vertically
			Bounds top = new Bounds(//
					balancePoint.bounds.a.x, //
					balancePoint.bounds.a.y, //
					balancePoint.bounds.b.x, //
					balancePoint.point.y);//
			if (top.isValid()) {

				ColorProperties colorProperties = getColorAvarage(top,
						coloredRectangle.diagonalFromTopLeftToBottomRight);
				result.add(new ColoredRectangle(top, colorProperties.avarageColor,
						colorProperties.triangleAvarageUpperColor, colorProperties.triangleAvarageLowerColor,
						!coloredRectangle.diagonalFromTopLeftToBottomRight));
			}

			// top-right
			Bounds bottom = new Bounds(//
					balancePoint.bounds.a.x, //
					balancePoint.point.y + 1, //
					balancePoint.bounds.b.x, //
					balancePoint.bounds.b.y);//
			if (bottom.isValid()) {

				ColorProperties colorProperties = getColorAvarage(bottom,
						coloredRectangle.diagonalFromTopLeftToBottomRight);
				result.add(new ColoredRectangle(bottom, colorProperties.avarageColor,
						colorProperties.triangleAvarageUpperColor, colorProperties.triangleAvarageLowerColor,
						!coloredRectangle.diagonalFromTopLeftToBottomRight));
			}

		}

		var r = result//
				.stream()//
				.filter(rectangle -> coloredRectangle.bounds.area() > 0)//
				.collect(Collectors.toList());

		// System.out.println("Divisions: "+r);
		if (r.size() == 2) {
			return r;
		} else {
			return new ArrayList<>();
		}
		// return r;

	}

	/**
	 * energy of pixel at column x and row y
	 */
	public double energy(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			throw new IllegalArgumentException(
					String.format("Wrong x: %d or y: %d for width: %d, height: %d", x, y, width, height));
		}

		Color left = getPixel(x - 1, y);
		Color right = getPixel(x + 1, y);
		Color top = getPixel(x, y - 1);
		Color bottom = getPixel(x, y + 1);
/*
		var redDiff = Math.sqrt(substractRed(right, left).squared() + substractRed(bottom, top).squared());
		var greenDiff = Math.sqrt(substractGreen(right, left).squared() + substractGreen(bottom, top).squared());
		var blueDiff = Math.sqrt(substractBlue(right, left).squared() + substractBlue(bottom, top).squared());
	*/	
		
		
		var redDiff = substractRed(right, left).squared() + substractRed(bottom, top).squared();
		var greenDiff = substractGreen(right, left).squared() + substractGreen(bottom, top).squared();
		var blueDiff = substractBlue(right, left).squared() + substractBlue(bottom, top).squared();
		
		return Math.sqrt(redDiff + greenDiff + blueDiff) + Math.sqrt(substract(right, left).squared() + substract(bottom, top).squared());
		
		//return Math.sqrt(substract(right, left).squared() + substract(bottom, top).squared());
	}
/*
	public double sobelEnergy(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			throw new IllegalArgumentException(
					String.format("Wrong x: %d or y: %d for width: %d, height: %d", x, y, width, height));
		}

		
		Color p1 = getPixel(x-1, y - 1);
		Color p3 = getPixel(x+1, y - 1);
		Color p5 = getPixel(x, y);

		Color p7 = getPixel(x-1, y+1);
		Color p9 = getPixel(x+1, y+1);

		
		Color p4 = getPixel(x - 1, y);
		Color p6 = getPixel(x + 1, y);
		Color p2 = getPixel(x, y - 1);
		Color p8 = getPixel(x, y + 1);

		ColorResult a = 
		
		return Math.sqrt();
	
	}
	*/
	public BalancePoint getBalancePoint(Bounds bounds) {

		var pv = getSummedValues(bounds);
		return getBalancePoint(pv);
	}

	public BalancePoint getBalancePoint(PixelValues pixelValues) {
		int bx = getDivisonPoint(pixelValues.columnValues);
		// printBalance(pixelValues.columnValues, bx);

		int by = getDivisonPoint(pixelValues.rowValues);
		// printBalance(pixelValues.rowValues, by);

		return new BalancePoint(pixelValues, new Point(pixelValues.bounds.a.x + bx, pixelValues.bounds.a.y + by));
	}

	public ColorProperties getColorAvarage(Bounds bounds, boolean diagonalFromTopLeftToBottomRight) {

		ColorAggregator upperColor = new ColorAggregator();
		ColorAggregator lowerColor = new ColorAggregator();
		ColorAggregator avarageColor = new ColorAggregator();

		double energyAvarage = 0;

		float minEnergy = Integer.MAX_VALUE;
		float _maxEnergy = Integer.MIN_VALUE;

		float area = bounds.area();
		long energySum = 0;

		for (int x = bounds.a.x; x <= bounds.b.x; x++) {
			for (int y = bounds.a.y; y <= bounds.b.y; y++) {

				float energy = energyMap[y * width + x];
				energySum += energy;

				double fenergy = (1000 * ((double) energy)) / ((double) maxEnergy);

				energyAvarage += fenergy;

				if (energy < minEnergy) {
					minEnergy = energy;
				}
				if (energy > _maxEnergy) {
					_maxEnergy = energy;
				}

				int pixelColor = colorPixels[y * width + x];
				if (renderProperties.renderMethod == RenderMethod.TRIANGLE) {
					int ay = y - bounds.a.y;
					int ax = x - bounds.a.x;

					int dx = (ay * bounds.getWidth()) / bounds.getHeight();
					if ((ax <= dx) ^ diagonalFromTopLeftToBottomRight) {
						// add to lower half
						lowerColor.add(pixelColor);
					} else {
						// add to upper half
						upperColor.add(pixelColor);
					}
				}
					avarageColor.add(pixelColor);
				

			}
		}

		energyAvarage /= (float) area;

		ColorProperties colorProperties = new ColorProperties(avarageColor.getAvarageColor(),
				upperColor.getAvarageColor(), lowerColor.getAvarageColor(), (float) energyAvarage, energySum,
				_maxEnergy, minEnergy);

		// System.out.println(colorProperties);

		return colorProperties;
	}

	public int getDivisonPoint(SummedValues values) {

		if (values.energies.length < 2) {
			return 0;
		}

		if (renderProperties.divideStrategy == DivideStrategy.RANDOM) {

			return rnd.nextInt(values.energies.length - 1);

		}

		if (renderProperties.divideStrategy == DivideStrategy.CENTER) {
			// divides the rectangle in for equal parts

			return (int) (values.energies.length / 2 - 1);

		} else if (renderProperties.divideStrategy == DivideStrategy.BALANCED) {

			if (values.energySum == 0.0) {
				return (int) (values.energies.length / 2 - 1);
			}

			float halfEnergySum = (float) (values.energySum / 2.0);
			float rollSum = 0;
			float difference;
			float minDifference = Integer.MAX_VALUE;
			int minIndex = 0;

			if (values.energies.length > 1) {
				for (int i = 0; i < values.energies.length; i++) {
					rollSum += values.energies[i];
					difference = Math.abs(halfEnergySum - rollSum);
					if (difference < minDifference) {
						minDifference = difference;
						minIndex = i;
					}
				}

				if (minIndex == values.energies.length - 1) {
					minIndex--;
				}
			}

			return minIndex;

		} else {
			throw new IllegalArgumentException();
		}
	}

	public float[] getEnergyMap() {
		float[] result = new float[colorPixels.length];

		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {

				float energy = (float) energy(x, y);
				if (energy > maxEnergy) {
					maxEnergy = energy;
				}
				result[width * y + x] = energy;
			}
		}

		System.out.println("maxEnergy:" + maxEnergy);
		maxEnergy = 100f;
		// normalize energy map
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				result[width * y + x] /= maxEnergy;
			}
		}

		return result;
	}

	public BufferedImage getGradientImage() {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				float energy = energyMap[y * width + x];

				float out = (float) (255.0 * energy);

				if (out > 255) {
					out = 255;
				}

				int grey = (int) out & 0xff;

				int color = grey | grey << 8 | grey << 16;

				bi.setRGB(x, y, (int) color);
			}
		}

		return bi;
	}

	public BufferedImage getOutputImage() {
		while (!queue.isEmpty()) {
			render(null);
		}
		return output;

	}

	public Color getPixel(int x, int y) {
		return new Color(colorPixels[y * width + x]);
	}

	public String getPixelsStr(float[] arr, int width, int height, int balancePointX, int balancePointY) {
		StringBuilder sb = new StringBuilder();
		String marker = " ";
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x == balancePointX) {
					marker = "|";
				} else {
					marker = " ";
				}
				sb.append(String.format("%4f%s", arr[y * width + x], marker));
			}
			if (y == balancePointY) {
				sb.append("\n");
				for (int x = 0; x < width; x++) {
					sb.append(String.format("-----"));
				}

			}
			sb.append("\n");

		}
		return sb.toString();
	}

	public ColoredRectangle getSeedImage() {
		Bounds bounds = new Bounds(0, 0, width - 1, height - 1);
		ColorProperties colorProperties = getColorAvarage(bounds, true);

		return new ColoredRectangle(bounds, colorProperties.avarageColor, colorProperties.triangleAvarageUpperColor,
				colorProperties.triangleAvarageLowerColor, true);
	}

	/**
	 * Returns the sum of columns/rows in the image top left corner: (ax,ay), bottom
	 * right corner: (bx, by)
	 * 
	 * @param ax top left x
	 * @param ay top left y
	 * @param bx bottom right x
	 * @param by bottom right y
	 * @return array of column sums
	 */
	public PixelValues getSummedValues(Bounds bounds) {

		int w = bounds.getWidth();
		int h = bounds.getHeight();

		SummedValues columnValues = new SummedValues(w);

		SummedValues rowValues = new SummedValues(h);

		if (renderProperties.divideStrategy != DivideStrategy.CENTER) {

			float bwValue = 0;
			int colorValue = 0;
			int index = 0;
			float energy = 0;
			float energyAvarage = 0;

			for (int x = bounds.a.x; x < bounds.b.x; x++) {
				for (int y = bounds.a.y; y < bounds.b.y; y++) {

					index = (y - bounds.a.y) * width + (x - bounds.a.x);
					energy = energyMap[index];
					colorValue = colorPixels[index];

					// energy = (float) ((255.0 * bwValue) / (float) maxEnergy);
					energyAvarage += energy;

					/*
					 * if (bwValue == 0) { bwValue = 1; }
					 */

					columnValues.addEnergy(energy, x - bounds.a.x);
					// columnValues.addColorPixel(colorValue, x - bounds.a.x);

					rowValues.addEnergy(energy, y - bounds.a.y);
					// rowValues.addColorPixel(colorValue, y - bounds.a.y);
				}

			}

			energyAvarage /= (float) bounds.area();
		}
		return new PixelValues(bounds, columnValues, rowValues);
	}

	public void printBalance(SummedValues sv, int index) {
		float left = 0;
		for (int i = 0; i <= index; i++) {
			left += sv.energies[i];
		}
		float right = 0;
		for (int i = index + 1; i < sv.energies.length; i++) {
			right += sv.energies[i];
		}
		System.out.format("size: %d, index: %d lvalue:%f, rvalue: %f diff: %f\n", sv.energies.length, index, left,
				right, right - left);
	}

	public BufferedImage render() {
		while (render(null))
			;
		return output;
	}

	public boolean render(ImageDisplayComponent id) {
		var r = queue.pop();

		var rectangles = divide(r);
		queue.addAll(rectangles);

		if (renderProperties.overwrite || rectangles.size() == 0) {

			outputG2.setColor(new Color(r.avarageRectangleColor));
			int bwidth = r.bounds.getWidth();
			int bheight = r.bounds.getHeight();

			if (renderProperties.isUseAlpha()) {
				double value = (float) ((10 * bwidth) / (float) width);
				float alpha = (float) Math.min(1.0, Math.max(0.0, value));
				alpha = Math.max(1.0f - alpha, 0.5f);
				outputG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			}

			switch (renderMethod) {
			case TRIANGLE:
				if (r.bounds.getWidth()<4||r.bounds.getHeight()<4) {
					outputG2.setColor(new Color(r.avarageRectangleColor));
					outputG2.fillRect(r.bounds.a.x, r.bounds.a.y, r.bounds.getWidth(), r.bounds.getHeight());
				} else {
					if (!r.diagonalFromTopLeftToBottomRight) {

						outputG2.setColor(new Color(r.upperTriangleColor));
						r.bounds.b.x++;
						r.bounds.b.y++;
						outputG2.fillPolygon(new int[] { r.bounds.a.x, r.bounds.b.x, r.bounds.b.x },
								new int[] { r.bounds.a.y, r.bounds.a.y, r.bounds.b.y }, 3);

						outputG2.setColor(new Color(r.lowerTriangleColor));
						outputG2.fillPolygon(new int[] { r.bounds.a.x, r.bounds.b.x, r.bounds.a.x },
								new int[] { r.bounds.a.y, r.bounds.b.y, r.bounds.b.y }, 3);
					} else {
						outputG2.setColor(new Color(r.upperTriangleColor));
						r.bounds.b.x++;
						r.bounds.b.y++;
						outputG2.fillPolygon(new int[] { r.bounds.a.x, r.bounds.b.x, r.bounds.a.x },
								new int[] { r.bounds.a.y, r.bounds.a.y, r.bounds.b.y }, 3);

						outputG2.setColor(new Color(r.lowerTriangleColor));
						outputG2.fillPolygon(new int[] { r.bounds.b.x, r.bounds.b.x, r.bounds.a.x },
								new int[] { r.bounds.a.y, r.bounds.b.y, r.bounds.b.y }, 3);

					}
				}
				break;

			case RECTANGLE:
				outputG2.drawRect(r.bounds.a.x, r.bounds.a.y, r.bounds.getWidth() - 1, r.bounds.getHeight() - 1);
				break;
			case FILLED_ELLIPSE:
				outputG2.fillOval(r.bounds.a.x + 1, r.bounds.a.y + 1, bwidth, bheight);
				break;
			case FILLED_OUTLINED_RECTANGLE:
				if (r.bounds.area() < 8) {
					outputG2.fillRect(r.bounds.a.x, r.bounds.a.y, r.bounds.getWidth(), r.bounds.getHeight());
				} else {
					outputG2.fillRect(r.bounds.a.x + 2, r.bounds.a.y + 2, r.bounds.getWidth() - 1,
							r.bounds.getHeight() - 1);
				}
				break;
			case FILLED_RECTANGLE:
				outputG2.fillRect(r.bounds.a.x, r.bounds.a.y, r.bounds.getWidth(), r.bounds.getHeight());
				break;
			case STROKE:

				if (r.bounds.area() < 8) {
					outputG2.fillRect(r.bounds.a.x, r.bounds.a.y, r.bounds.getWidth(), r.bounds.getHeight());
				} else {
					float strokeWidth = (float) Math.sqrt(bwidth * bwidth + bheight * bheight);
					outputG2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					// if (!r.diagonalFromTopLeftToBottomRight) {
					outputG2.drawLine(r.bounds.a.x, r.bounds.a.y, r.bounds.b.x, r.bounds.b.y);
					/*
					 * } else { outputG2.drawLine(r.bounds.b.x, r.bounds.a.y, r.bounds.a.x,
					 * r.bounds.b.y); }
					 */
				}
				break;
			default:
				throw new IllegalArgumentException();
			}

			if (id != null) {
				id.repaint(r.bounds.a.x, r.bounds.a.y, bwidth + 1, bheight + 1);
			}

		}
		return !queue.isEmpty();
	}

	public void reset() {
		outputG2 = (Graphics2D) output.getGraphics();
		outputG2.setColor(Color.BLACK);
		outputG2.fillRect(0, 0, width, height);
		if (renderProperties.renderMethod != RenderMethod.TRIANGLE) {
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			outputG2.setRenderingHints(rh);
		}
		ColoredRectangle original = getSeedImage();
		queue.add(original);

	}

	public void saveImage(BufferedImage bufferedImage, String fullFileName) {

		try {

			ImageIO.write(bufferedImage, "png", new File(fullFileName));

		} catch (IOException e) {
			System.out.println("Exception occured :" + e.getMessage());
		}
		System.out.format("Image '%s' were written succesfully.", fullFileName);
	}

	public void saveImage(String fullFileName) {

		try {

			ImageIO.write(this.output, "png", new File(fullFileName));

		} catch (IOException e) {
			System.out.println("Exception occured :" + e.getMessage());
		}
		System.out.format("Image '%s' were written succesfully.", fullFileName);
	}

	public void setRenderProperties(RenderProperties renderProperties) {
		this.renderProperties = renderProperties;
	}

	/**
	 * Returns a {@link ColorResult} containing the substraction of each color
	 * component (R, G, B). {@link Color} does not support negative values, so the
	 * {@link ColorResult} value object is used instead.
	 *
	 * @param first  the first {@link Color}
	 * @param second the second {@link Color}
	 * @return the result of the substraction of the second color from the first
	 *         color
	 */
	private ColorResult substract(Color first, Color second) {
		return new ColorResult(first.getRed() - second.getRed(), first.getGreen() - second.getGreen(),
				first.getBlue() - second.getBlue());
	}
	
	private ColorResult add(Color first, Color second) {
		return new ColorResult(first.getRed() + second.getRed(), first.getGreen() + second.getGreen(),
				first.getBlue() + second.getBlue());
	}
	
	private ColorResult mul(double value, Color first) {
		return new ColorResult((int)(value * first.getRed()), (int)(value * first.getGreen()), (int)(value * first.getBlue()));
	}

	
	private ColorResult substractRed(Color first, Color second) {
		return new ColorResult(first.getRed() - second.getRed(), 0,0);
	}
	
	private ColorResult substractGreen(Color first, Color second) {
		return new ColorResult(0, first.getGreen() - second.getGreen(),	0);
	}
	
	private ColorResult substractBlue(Color first, Color second) {
		return new ColorResult(0,0,	first.getBlue() - second.getBlue());
	}



	@Override
	public String toString() {
		return String.format("%s(%dx%d)\n%s", this.name, width, height,
				getPixelsStr(this.energyMap, width, height, -1, -1));
	}

	public String toString(int balancePointX, int balancePointY) {
		return String.format("%s(%dx%d)\n%s", this.name, width, height,
				getPixelsStr(this.energyMap, width, height, balancePointX, balancePointY));
	}

}
