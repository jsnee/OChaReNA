package com.jophus.houghtransformation;

/**
 * Class to hold actual hough transformation process data.
 * 
 * @author SUN2K
 */
class HTProcessData {

	/* precalculated sin and cos lookup tables */
	private static final double[] cosLookupTable = new double[180];
	private static final double[] sinLookupTable = new double[180];
	
	static {
		/* precalculate lookup table for sin() and cos() required later to calculate hough transformation */
		for (int angle = 0; angle < 180; angle++)
		{
			sinLookupTable[angle] = Math.sin(Math.toRadians(angle));
			cosLookupTable[angle] = Math.cos(Math.toRadians(angle));
		}
	}
	
	/**
	 * Array for voting array of hough transformation.
	 * First dimension is the angle from 0 to 180 degrees.
	 * Second dimension is absolute distance with range 0 to Sqrt(imagewidth^2 + imageheight^2)
	 * assuming that the origin is at a left-top corner.
	 */
	private int[][] houghspaceTopOrigin;
	
	/**
	 * Array for voting array of hough transformation.
	 * First dimension is the angle from 0 to 180 degrees.
	 * Second dimension is signed distance with range 0 to 2 * Sqrt(imagewidth^2 + imageheight^2)
	 * assuming that the origin is at the center of the image.
	 */
	private int[][] houghspaceCenterOrigin;
	
	/**
	 * relative threshold value to filter houghspace image to build maxima hough image.
	 * This value can be indirectly changed from outside by setting the relative maximum value.
	 */
	private int houghLineThreshold;

	/**
	 * Threshold used for filtering the grayscale edge image to the black-white image.
	 */
	private int edgeTreshold;

	/**
	 * Factor to multiply the value of the hough maxima with to get a brighter image of the hough space.
	 */
	private double houghSpaceBrightFactor;
	
	/**
	 * relative maximum value to calculate threshold online used to filter hough space image.
	 */
	private int houghRelativeMaxima;
	
	/**
	 * dimension of r of the hough space data when assuming the origin at the top left corner.
	 */
	private int rDimTop;

	/**
	 * dimension of r of the hough space data when assuming the origin at the center.
	 */
	private int rDimCenter;
	
	/**
	 * the offset into the r-dimension to get to the origin (distance r = 0).
	 */
	private int rDimCenterOffset;

	/**
	 * Constructor.
	 */
	public HTProcessData()
	{
		// set default data
		setEdgeTreshold(200);
		setHoughSpaceBrightFactor(2.0);
		setHoughRelativeMaxima(80);
	}
	
	/**
	 * Get the cosinus value for requested angle.
	 * @param angle angle in degress to get cosinus value. Must be in range [0,180).
	 * @return cosinus value.
	 * @throws HTException if angle is out of range.
	 */
	public double getCosVal(int angle)
	{
		if (angle < 0 || angle >= 180)
		{
			throw new HTException("Angle for requested cosinus value out of range: " + angle);
		}
		return cosLookupTable[angle];
	}
	
	/**
	 * Get the sinus value for requested angle.
	 * @param angle angle in degress to get sinus value. Must be in range [0,180).
	 * @return sinus value.
	 * @throws HTException if angle is out of range.
	 */
	public double getSinVal(int angle)
	{
		if (angle < 0 || angle >= 180)
		{
			throw new HTException("Angle for requested sinus value out of range: " + angle);
		}
		return sinLookupTable[angle];
	}
	
	/**
	 * Get an array of cosinus values for range [0,180).
	 * @return 180-dim array of cosinus values.
	 */
	public double[] getCosTable()
	{
		return cosLookupTable;
	}
	
	/**
	 * Get an array of sinus values for range [0,180).
	 * @return 180-dim array of sinus values.
	 */
	public double[] getSinTable()
	{
		return sinLookupTable;
	}

	/**
	 * Based on the filtered edge image, calculate the hough space data. 
	 * @param filteredEdgeImage filtered / thresholded edge image.
	 */
	public void calcluateHoughSpaceArray(HTImage filteredEdgeImage) {
		
		if (!(filteredEdgeImage instanceof HTGrayImage))
		{
			throw new HTException("Given image is no grayscale image.");
		}
		
		HTGrayImage edgeTresholdImage = (HTGrayImage)filteredEdgeImage;
		
		/* using the middle of the top border as origin and using only the absolute value of the distance
		 * ignoring the sign, the maximum distance is calculated by the image dimension using pythagoras 
		 */
		rDimTop = (int)Math.ceil(Math.sqrt(edgeTresholdImage.getWidth() * edgeTresholdImage.getWidth() +
				edgeTresholdImage.getHeight() *  edgeTresholdImage.getHeight()));
		houghspaceTopOrigin = new int[180][rDimTop/*+1*/];
		
		/* build the hough space with the origin at the center of the image.
		 * The distance can also be negative and because we do not use the absolute value here,
		 * the dimension of distance has to be doubled. */
		rDimCenter = rDimTop*2;
		houghspaceCenterOrigin = new int[180][rDimCenter];
		rDimCenterOffset = rDimCenter >> 1;
				
		// for each angle, get all "edge" pixel and the parameter r
		for (int x = 0; x < edgeTresholdImage.getWidth(); x++)
		{
			for (int y = 0; y < edgeTresholdImage.getHeight(); y++)
			{
				if (edgeTresholdImage.getPixel(x, y) > 0)
				{		
					for (int angle = 0; angle < 180; angle++)
					{
						int r = (int)(x * cosLookupTable[angle] + y * sinLookupTable[angle]);
												
						// increase voting for this parameter pair
						houghspaceTopOrigin[angle][Math.abs(r)]++;
						
						if ((r + rDimCenterOffset) >= rDimCenter)
							throw new IllegalStateException("r outside range: " + r);
						houghspaceCenterOrigin[angle][r+rDimCenterOffset]++;
					}
				}
			}
		}
	}
	
	
	/**
	 * Get the hough space data with origin located at top left corner.
	 * @return
	 */
	public int getHoughspaceTopOrigin(int angle, int r)
	{
		if (angle < 0 || angle >= 180 || r < 0 || r > rDimTop)
			throw new HTException("Requested value out of hough space range: angle=" + angle + ", r =" + r);
		return houghspaceTopOrigin[angle][r];
	}
	
	/**
	 * Get the hough space data with origin located at the center.
	 * @return
	 */	
	public int getHoughspaceCenterOrigin(int angle, int r) {
		if (angle < 0 || angle >= 180 || r < 0 || r > rDimCenter)
			throw new HTException("Requested value out of hough space range: angle=" + angle + ", r =" + r);
		return houghspaceCenterOrigin[angle][r];
	}
	
	/**
	 * @return
	 */
	public int getHoughLineThreshold() {
		return houghLineThreshold;
	}

	/**
	 * @param houghLineThreshold
	 */
	public void setHoughLineThreshold(int houghLineThreshold) {
		this.houghLineThreshold = houghLineThreshold;
	}

	/**
	 * @return
	 */
	public int getEdgeTreshold() {
		return edgeTreshold;
	}

	/**
	 * @param edgeTreshold
	 */
	public void setEdgeTreshold(int edgeTreshold) {
		if (edgeTreshold < 0)
			throw new HTException("Brightness factor out of range:" + edgeTreshold);
		this.edgeTreshold = edgeTreshold;
	}

	/**
	 * @return brightness multiply factor.
	 */
	public double getHoughSpaceBrightFactor() {
		return houghSpaceBrightFactor;
	}

	/**
	 * @param houghSpaceBrightFactor
	 */
	public void setHoughSpaceBrightFactor(double houghSpaceBrightFactor) {
		this.houghSpaceBrightFactor = houghSpaceBrightFactor;
	}

	/**
	 * @return
	 */
	public int getHoughRelativeMaxima() {
		return houghRelativeMaxima;
	}

	/**
	 * @param houghRelativeMaxima
	 */
	public void setHoughRelativeMaxima(int houghRelativeMaxima) {
		this.houghRelativeMaxima = houghRelativeMaxima;
	}
	
	/**
	 * @return
	 */
	public int getRDimTop() {
		return rDimTop;
	}
	
	/**
	 * @return
	 */
	public int getRDimCenter() {
		return rDimCenter;
	}
	
	/**
	 * @return
	 */
	public int getRDimCenterOffset() {
		return rDimCenterOffset;
	}
	
	/**
	 * Get the first dimension of the hough space area which is the angle range.
	 * @return
	 */
	public int getAngleDim() {
		return 180;
	}
}
