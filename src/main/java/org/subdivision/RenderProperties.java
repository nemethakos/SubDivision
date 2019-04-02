package org.subdivision;

 public class RenderProperties {
	public float treshold;
	public int minAreaToDivide;
	RenderMethod renderMethod;
	boolean useAlpha;
	boolean overwrite;
	boolean saveImage;
	DivideStrategy divideStrategy;
	
	
	public RenderProperties() {
		this(0.1f, 10, RenderMethod.FILLED_OUTLINED_RECTANGLE, DivideStrategy.CENTER, false, false, true);
	}


	public RenderProperties(float treshold, 
			int minAreaToDivide, 
			RenderMethod renderMethod, 
			DivideStrategy divideStrategy,
			boolean useAlpha, 
			boolean overwrite,
			boolean saveImage) {
		super();
		this.treshold = treshold;
		this.minAreaToDivide = minAreaToDivide;
		this.renderMethod = renderMethod;
		this.divideStrategy = divideStrategy;
		this.useAlpha = useAlpha;
		this.overwrite = overwrite;
		this.saveImage = saveImage;
	}


	public float getTreshold() {
		return treshold;
	}


	public int getMinAreaToDivide() {
		return minAreaToDivide;
	}


	public RenderMethod getRenderMethod() {
		return renderMethod;
	}


	public boolean isUseAlpha() {
		return useAlpha;
	}


	@Override
	public String toString() {
		return String.format("RenderProperties [treshold=%s, minAreaToDivide=%s, renderMethod=%s, useAlpha=%s]",
				treshold, minAreaToDivide, renderMethod, useAlpha);
	}
	
	
	
	
	
}