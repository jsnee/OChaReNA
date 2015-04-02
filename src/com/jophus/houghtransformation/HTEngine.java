package com.jophus.houghtransformation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Main class of Hough-Transformation.
 * 
 * @author Sun2k
 *
 */
public class HTEngine {

	/**
	 * Holds the latest step that is already calculated in the transformation pipeline.
	 */
	private EHTProcessStep calculatedStep;

	/**
	 * The source image.
	 */
	private HTColorImage originalImage;

	/**
	 * Process step to calculate a grayscale image from the original one.
	 */
	HTAbstractProcessStep stepOriginalToGrayscale;

	/**
	 * Process step to calculate an edge image using a edge filtere from the grayscale image.
	 */
	HTAbstractProcessStep stepGrayscaleToEdge;

	/**
	 * Process step to calculate a threshold edge image from the edge filtered one.
	 */
	HTAbstractProcessStep stepEdgeToThresholdEdge;

	/**
	 * Process step to visualize the hough space with origin at the top left corner.
	 */
	HTAbstractProcessStep stepEdgeThresholdToHoughTop;

	/**
	 * Process step to visualize the hough space with origin at the center.
	 */
	HTAbstractProcessStep stepEdgeThresholdToHoughCenter;

	/**
	 * Process step to calculate the tresholded hough space image (center @ middle top).
	 */
	HTAbstractProcessStep stepHoughToMaximaHoughSpace;

	/**
	 * Process step to calculate the original image overlayed with the detected edges.
	 */
	HTAbstractProcessStep stepMaximaToOverlay;

	/**
	 * Data pool to hold all required data and parameters of the hough process.
	 */
	HTProcessData htProcessData;


	/**
	 * Constructor.
	 */
	public HTEngine()
	{
		calculatedStep = EHTProcessStep.STEP_ORIGINAL;

		htProcessData = new HTProcessData();
	}

	public HTImage getHTProcessSteps(ArrayList<EHTProcessStep> doSteps) {
		HTImage result = originalImage;
		HTProcessData procDat = new HTProcessData();
		for (EHTProcessStep eachStep : doSteps) {
			switch (eachStep) {
			case STEP_GRAYSCALE:
				HTStepOriginalToGrayscale gray = new HTStepOriginalToGrayscale(result, procDat);
				gray.execute();
				result = gray.getImage();
				break;

			case STEP_EDGE_DETECTION:
				HTStepGrayscaleToEdge edge = new HTStepGrayscaleToEdge(result, procDat);
				edge.execute();
				result = edge.getImage();
				break;

			case STEP_EDGE_TRESHOLD:
				HTStepEdgeToThresholdEdge tEdge = new HTStepEdgeToThresholdEdge(result, procDat);
				tEdge.execute();
				result = tEdge.getImage();
				break;

			case STEP_HOUGH_SPACE_TOP:			
				/* first calculate HT space data */
				procDat.calcluateHoughSpaceArray(result);

				HTStepThresholdEdgeToHoughTop houghTop = new HTStepThresholdEdgeToHoughTop(result, procDat);
				houghTop.execute();
				result = houghTop.getImage();
				break;

			case STEP_HOUGH_SPACE_CENTER:
				HTStepThresholdEdgeToHoughCenter houghCenter = new HTStepThresholdEdgeToHoughCenter(result, procDat);
				houghCenter.execute();
				result = houghCenter.getImage();
				break;

			case STEP_HOUGH_SPACE_FILTERED:
				HTStepHoughToMaximaHoughSpace houghFiltered = new HTStepHoughToMaximaHoughSpace(result, procDat);
				houghFiltered.execute();
				result = houghFiltered.getImage();
				break;

			case STEP_ORIGINAL_LINES_OVERLAYED:
				HTStepMaximaToOriginalOverlayedImage overlay = new HTStepMaximaToOriginalOverlayedImage(originalImage, procDat);
				overlay.execute();
				result = overlay.getImage();
				break;

			default:break; 	/* cannot happen */
			}
		}
		return result;
	}

	/**
	 * Get the image of the requested transformation step. If required, calculates
	 * all intermediate steps.
	 * @param processStep HT step to get image for.
	 * @return processed image for requested step.
	 */
	public HTImage getHTProcessStep(EHTProcessStep processStep)
	{
		if (calculatedStep.isLowerStep(processStep))
		{
			processHTSteps(processStep);
		}

		switch (processStep)
		{
		case STEP_ORIGINAL:
			return originalImage;

		case STEP_GRAYSCALE:
			return stepOriginalToGrayscale.getImage();

		case STEP_EDGE_DETECTION:
			return stepGrayscaleToEdge.getImage();

		case STEP_EDGE_TRESHOLD:
			return stepEdgeToThresholdEdge.getImage();

		case STEP_HOUGH_SPACE_TOP:
			return stepEdgeThresholdToHoughTop.getImage();

		case STEP_HOUGH_SPACE_CENTER:
			return stepEdgeThresholdToHoughCenter.getImage();

		case STEP_HOUGH_SPACE_FILTERED:
			return stepHoughToMaximaHoughSpace.getImage();

		case STEP_ORIGINAL_LINES_OVERLAYED:
			return stepMaximaToOverlay.getImage();

		default:
			throw new IllegalStateException();
		}
	}


	/**
	 * Calculate all steps of the hough-transformation until the step given as argument.
	 * 
	 * @param processStep
	 */
	private void processHTSteps(EHTProcessStep processStep) {

		EHTProcessStep curStep = calculatedStep;
		while (curStep.isLowerStep(processStep))
		{
			curStep = curStep.getNextStep();
			switch (curStep)
			{
			case STEP_GRAYSCALE:
				stepOriginalToGrayscale = new HTStepOriginalToGrayscale(originalImage, htProcessData);
				stepOriginalToGrayscale.execute();
				calculatedStep = EHTProcessStep.STEP_GRAYSCALE;
				break;

			case STEP_EDGE_DETECTION:
				stepGrayscaleToEdge = new HTStepGrayscaleToEdge(stepOriginalToGrayscale.getImage(), htProcessData);
				stepGrayscaleToEdge.execute();
				calculatedStep = EHTProcessStep.STEP_EDGE_DETECTION;
				break;

			case STEP_EDGE_TRESHOLD:
				stepEdgeToThresholdEdge = new HTStepEdgeToThresholdEdge(stepGrayscaleToEdge.getImage(), htProcessData);
				stepEdgeToThresholdEdge.execute();
				calculatedStep = EHTProcessStep.STEP_EDGE_TRESHOLD;
				break;

			case STEP_HOUGH_SPACE_TOP:			
				/* first calculate HT space data */
				htProcessData.calcluateHoughSpaceArray(stepEdgeToThresholdEdge.getImage());

				stepEdgeThresholdToHoughTop = new HTStepThresholdEdgeToHoughTop(stepEdgeToThresholdEdge.getImage(), htProcessData);
				stepEdgeThresholdToHoughTop.execute();
				calculatedStep = EHTProcessStep.STEP_HOUGH_SPACE_TOP;
				break;

			case STEP_HOUGH_SPACE_CENTER:
				stepEdgeThresholdToHoughCenter = new HTStepThresholdEdgeToHoughCenter(stepEdgeToThresholdEdge.getImage(), htProcessData);
				stepEdgeThresholdToHoughCenter.execute();
				calculatedStep = EHTProcessStep.STEP_HOUGH_SPACE_CENTER;
				break;

			case STEP_HOUGH_SPACE_FILTERED:
				stepHoughToMaximaHoughSpace = new HTStepHoughToMaximaHoughSpace(
						stepEdgeThresholdToHoughTop.getImage(), htProcessData);
				stepHoughToMaximaHoughSpace.execute();
				calculatedStep = EHTProcessStep.STEP_HOUGH_SPACE_FILTERED;
				break;

			case STEP_ORIGINAL_LINES_OVERLAYED:
				stepMaximaToOverlay = new HTStepMaximaToOriginalOverlayedImage(
						originalImage, htProcessData);
				stepMaximaToOverlay.execute();
				calculatedStep = EHTProcessStep.STEP_ORIGINAL_LINES_OVERLAYED;
				break;

			default:break; 	/* cannot happen */
			}

		}
	}


	/**
	 * Set an image to perform HT for.
	 * @param imageFilename full path to source image.
	 */
	public void setSourceImage(String imageFilename) {
		this.originalImage = new HTColorImage(imageFilename);

		/* reset all calculated steps */
		calculatedStep = EHTProcessStep.STEP_ORIGINAL;
		stepOriginalToGrayscale = null;
		stepGrayscaleToEdge = null;
		stepEdgeToThresholdEdge = null;
		stepEdgeThresholdToHoughTop = null;
		stepEdgeThresholdToHoughCenter= null;
		stepHoughToMaximaHoughSpace = null;
		stepMaximaToOverlay = null;
	}

	public void setSourceImage(BufferedImage img) {
		this.originalImage = new HTColorImage(img);

		/* reset all calculated steps */
		calculatedStep = EHTProcessStep.STEP_ORIGINAL;
		stepOriginalToGrayscale = null;
		stepGrayscaleToEdge = null;
		stepEdgeToThresholdEdge = null;
		stepEdgeThresholdToHoughTop = null;
		stepEdgeThresholdToHoughCenter= null;
		stepHoughToMaximaHoughSpace = null;
		stepMaximaToOverlay = null;
	}

	/**
	 * Get the original root image.
	 * @return
	 */
	public HTImage getOriginalImage() 
	{
		return this.originalImage;
	}

	/**
	 * Set a new source image but remain at the current calculation step.
	 * @param img new source image to set.
	 */
	public void reloadNewImage(HTImage img)
	{
		this.originalImage = new HTColorImage(img);
		/* reset all calculated steps */
		EHTProcessStep oldStep =  calculatedStep;
		calculatedStep = EHTProcessStep.STEP_ORIGINAL;
		/* redo the previously performed steps with the new source image */
		processHTSteps(oldStep);
	}

	/**
	 * Set a new source image but remain at the current calculation step.
	 * @param filename new source image to set.
	 */
	public void reloadNewImage(String filename)
	{
		this.originalImage = new HTColorImage(filename);
		/* reset all calculated steps */
		EHTProcessStep oldStep =  calculatedStep;
		calculatedStep = EHTProcessStep.STEP_ORIGINAL;
		/* redo the previously performed steps with the new source image */
		processHTSteps(oldStep);
	}

	public void reloadNewImage(BufferedImage img)
	{
		this.originalImage = new HTColorImage(img);
		/* reset all calculated steps */
		EHTProcessStep oldStep =  calculatedStep;
		calculatedStep = EHTProcessStep.STEP_ORIGINAL;
		/* redo the previously performed steps with the new source image */
		processHTSteps(oldStep);
	}

	/**
	 * Get the latest step in hough transfrom which is calculated and uptodate.
	 * @return up-to-date calculated hough transform step.
	 */
	public EHTProcessStep getMaxCalculatedStep()
	{
		return calculatedStep;
	}

	/*********************************************************************
	 * Getter and setter for parameters of the hough transmformation
	 *********************************************************************/

	/**
	 * Set a new threshold value for the step edge -> threshold edge.
	 * @param threshold
	 */
	public void setEdgeTreshold(int threshold)
	{
		htProcessData.setEdgeTreshold(threshold);

		/* force to recalculate edge step with new threshold */ 
		if (stepEdgeToThresholdEdge != null)
		{
			/* set main engine back to this step because all further steps are invalid now as they were calculated based on the old value. */
			calculatedStep = EHTProcessStep.STEP_EDGE_DETECTION;
		}
	}

	/**
	 * Set a new brightness factor to visualize the hough space. Note that this value does not have any
	 * influence on the actual hough transform, but just affects the visualiaztion.
	 * @param brightFactor
	 */
	public void setBrightnessFactor(double brightFactor)
	{
		htProcessData.setHoughSpaceBrightFactor(brightFactor);
		if (stepEdgeThresholdToHoughTop != null)
		{	/* stepEdgeThresholdToHoughTop already caluclated, set FSM back to recalculate with new value */
			calculatedStep = EHTProcessStep.STEP_EDGE_TRESHOLD;
		}
	}

	/**
	 * Set a new threshold value to apply to the hough space in order to filter.
	 * @param relativeMax new maximum threshold value.
	 */
	public void setRelativeMaximum(int relativeMax)
	{
		htProcessData.setHoughRelativeMaxima(relativeMax);
		if (stepHoughToMaximaHoughSpace != null)
		{
			/* step was already calculated but relative maximum value affecting this step changed,
                   so force redo */
			calculatedStep = EHTProcessStep.STEP_HOUGH_SPACE_TOP;
		}
	}

	/**
	 * Get the hough space coordinate of the given image space coordinate. 
	 * @param x x-coordinate in image space.
	 * @param y y-coordinate in image space.
	 * @param width total width of image
	 * @param height total height of image
	 * @return hough coordinate
	 */
	public HoughCoordinate getHoughCoordinateTop(int x, int y, int width, int height)
	{
		if (calculatedStep.isLowerStep(EHTProcessStep.STEP_HOUGH_SPACE_TOP))
			throw new HTException("Illegal state to get hough space data.");

		double angle = 180.0 / width * x - 90.0;
		double r = (double)htProcessData.getRDimTop() / (double)height * (double)y;

		return new HoughCoordinate(angle, r);
	}

	/**
	 * Get the hough space coordinate of the given image space coordinate. 
	 * @param x x-coordinate in image space.
	 * @param y y-coordinate in image space.
	 * @param width total width of image
	 * @param height total height of image
	 * @return hough coordinate
	 */
	public HoughCoordinate getHoughCoordinateCenter(int x, int y, int width, int height)
	{
		if (calculatedStep.isLowerStep(EHTProcessStep.STEP_HOUGH_SPACE_CENTER))
			throw new HTException("Illegal state to get hough space data.");

		double angle = 180.0 / width * x - 90.0;
		double r = ((double)htProcessData.getRDimCenter() / (double)height * (double)y) - htProcessData.getRDimCenterOffset();

		return new HoughCoordinate(angle, r);
	}

	/**
	 * Class representing a coordinate in hough space consisting of an angle and offset.
	 * @author SUN2K
	 *
	 */
	public class HoughCoordinate {
		private final double angle;
		private final double r;

		public HoughCoordinate(double angle, double r)
		{
			this.angle = angle;
			this.r = r;
		}

		public double getAngle() { return angle; }
		public double getDistance() { return r; }
	}
}
