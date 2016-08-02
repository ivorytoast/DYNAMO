

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Rectangle;

public class ImageProcessing {

	//---------------------------------------Variables--------------------------------------------------------//
	
	private MBFImage[] segments = new MBFImage[1000000];
	private boolean noiseComparators[] = new boolean[1000000];
	public int[] values = new int[2];
	private float[][] grayScalePixels = null;
	private float[][] whitePixelArray = null;
	
	//---------------------------------------Constructor--------------------------------------------------------//
	
	public ImageProcessing(MBFImage image) {
		grayScalePixels = new float[image.getHeight()][image.getWidth()];
		whitePixelArray = new float[image.getHeight()][image.getWidth()];
		
		fillInWhitePixelArray(image, whitePixelArray);
	}
	
	//---------------------------------------Main Method--------------------------------------------------------//

	public static void main(String[] args) throws IOException {
		MBFImage image = ImageUtilities.readMBF(new File("/Users/Anthony/Desktop/running.png"));
		MBFImage clone = image.clone();
		ImageProcessing main = new ImageProcessing(image);
		main.findObjects(clone, clone.getWidth(), clone.getHeight(), image);
		DisplayUtilities.display(image);
	}
	
	//-----------------------------------------Methods----------------------------------------------------------//
	
	public static double getStandardDeviationValues(MBFImage segment) {
		float[] redValues = new float[100000];
		float[] greenValues = new float[100000];
		float[] blueValues = new float[100000];
		int counter = 0;
		int redCounter = 0;
		int greenCounter = 0;
		int blueCounter = 0;
		for (int y = 0; y < segment.getHeight() - 1; y++) {
		    for(int x = 0; x < segment.getWidth() - 1; x++) {
		        float redColor = segment.getBand(0).pixels[y][x];
		        float greenColor = segment.getBand(1).pixels[y][x];
		        float blueColor = segment.getBand(2).pixels[y][x];
		        redValues[counter] = redColor;
		        redCounter++;
		        greenValues[counter] = greenColor;
		        greenCounter++;
		        blueValues[counter] = blueColor;
		        blueCounter++;
		        counter++;
		    } 
		}
		float redAverage = findAverage(redValues, redCounter);
		float greenAverage = findAverage(greenValues, greenCounter);
		float blueAverage = findAverage(blueValues, blueCounter);
		
		double redSD = 0;
		double greenSD = 0;
		double blueSD = 0;
		
		for (int i = 0; i < counter; i++) {
			redSD += Math.pow(redValues[i] - redAverage, 2);
			greenSD += Math.pow(greenValues[i] - greenAverage, 2);
			blueSD += Math.pow(blueValues[i] - blueAverage, 2);
		}
		
		redSD = Math.sqrt(redSD / counter);
		greenSD = Math.sqrt(greenSD / counter);
		blueSD = Math.sqrt(blueSD / counter);
		
		double totalSD = Math.sqrt(redSD * redSD + greenSD * greenSD + blueSD * blueSD); 
		
		return totalSD;
	}
	
	public static float findAverage(float[] numbers, int total) {
		float sum = 0;
		for (int i = 0; i < numbers.length; i++) {
			sum += (float) numbers[i];
		}
		return sum / (float) total;
	}
	
	public void findObjects(MBFImage clone, int tileWidth, int tileHeight, MBFImage original) {
		int x = 0;
		int y = 0;
		int width = tileWidth;
		int height = tileHeight;
		int counter = 0;
		HashSet<String> lineSegs = new HashSet<>();
		for (int yPos = 0; yPos <= (clone.getHeight() / height); yPos++) {
			width = tileWidth;
			height = tileHeight;
			for (int xPos = 0; xPos <= (clone.getWidth() / width); xPos++) {
				Rectangle bounds = new Rectangle(x, y, getWidth(width, clone, x), getHeight(height, clone, y));
				MBFImage segment = clone.extractROI(bounds);
				segments[counter] = segment;
				//System.out.println("(" + x + "," + y + ")");
				//double sd = getStandardDeviationValues(segment);
				if (analyzeForObjects(segments[counter]) > 1) {
					String[] segs = {bounds.x + " " + bounds.y + " " + (bounds.x + bounds.width) + " " + bounds.y,
							bounds.x + bounds.width + " " + bounds.y + " " + (bounds.x + bounds.width) + " " + (bounds.y + bounds.height),
							bounds.x + " " + (bounds.y + bounds.height) + " " + (bounds.x + bounds.width) + " " + (bounds.y + bounds.height),
							bounds.x + " " + bounds.y + " " + bounds.x + " " + (bounds.y + bounds.height)
					};
					for (String seg : segs)
						if (lineSegs.contains(seg))
							lineSegs.remove(seg);
						else
							lineSegs.add(seg);
				}
				counter++;
				x += width;
			}
			y += height;
			x = 0;
		}
		
		for (String seg : lineSegs)
		{
			String[] coords = seg.split(" ");
			int x1 = (int) Double.parseDouble(coords[0]), y1 = (int) Double.parseDouble(coords[1]), x2 = (int) Double.parseDouble(coords[2]), y2 = (int) Double.parseDouble(coords[3]);
			original.drawLine(x1, y1, x2, y2, 4, RGBColour.RED);
		}
	}
	
	public static HashSet<String> fillSD (MBFImage image, int tileWidth, int tileHeight, double maxSD)
	{
		HashSet<String> stuff = new HashSet<>();
		for (int y = 0; y < image.getHeight(); y += tileHeight)
			for (int x = 0; x < image.getWidth(); x += tileWidth)
			{
				int w = Math.min(tileWidth, image.getWidth() - x);
				int h = Math.min(tileHeight, image.getHeight() - y);
				System.out.println(x + " " + y + " " + w + " " + h);
				MBFImage segment = image.extractROI(x, y, w, h);
				double sd = getStandardDeviationValues(segment);
				if (sd <= maxSD && sd > 0)
				{
					stuff.add(x + " " + y);
					for (int xx = x; xx < x + w; xx++)
						for (int yy = y; yy < y + h; yy++)
							image.setPixel(xx, yy, RGBColour.BLACK);
				}
			}
		return stuff;
	}
	
	public int analyzeForObjects(MBFImage image) {
		int counter = 0;
		for (int y=0; y<image.getHeight(); y++) {
			counter = 0;
		    for(int x=0; x<image.getWidth(); x++) {
		    	float sumFloat = 0;
		        float redColor = image.getBand(0).pixels[y][x];
		        float greenColor = image.getBand(1).pixels[y][x];
		        float blueColor = image.getBand(2).pixels[y][x];
		        sumFloat = (((redColor*256) + (greenColor*256) + (blueColor*256)) / 3);
		        if (sumFloat/256.0 < 1) {
		        	counter++;
		        } else {
		        	//continue
		        }
		    } 
		}
		return counter;
	}

	public void performSegmentation(MBFImage clone, int tileWidth, int tileHeight, int[] values) {
		values[0] = 0;
		values[1] = 0;
		int personCounter = 0;
		int x = 0;
		int y = 0;
		int width = tileWidth;
		int height = tileHeight;
		int counter = 0;
		for (int yPos = 0; yPos <= (clone.getHeight() / height); yPos++) {
			width = tileWidth;
			height = tileHeight;
			for (int xPos = 0; xPos <= (clone.getWidth() / width); xPos++) {
				Rectangle bounds = new Rectangle(x, y, getWidth(width, clone, x), getHeight(height, clone, y));
				MBFImage segment = clone.extractROI(bounds);
				segments[counter] = segment;
				if (getTotalNumberOfWhitePixels(segments[counter]) > 100 && getTotalNumberOfBlackPixels(segments[counter]) > 20) {
					if (counter > 0) {
						if (determineIfSamePerson(values[0], values[1], xPos, yPos) == false) {
							personCounter++;
						} else {
							//continue -- same person
						}
					} else {
						//continue
					}
					if (personCounter == 1) {
						clone.drawShape(bounds, 1, RGBColour.RED);
					} else if (personCounter == 2) {
						clone.drawShape(bounds, 1, RGBColour.GREEN);
					} else if (personCounter == 3) {
						clone.drawShape(bounds, 1, RGBColour.BLUE);
					} else if (personCounter == 4) {
						clone.drawShape(bounds, 1, RGBColour.ORANGE);
					} else if (personCounter == 5) {
						clone.drawShape(bounds, 1, RGBColour.PINK);
					} else if (personCounter == 6) {
						clone.drawShape(bounds, 1, RGBColour.GRAY);
					} else if (personCounter == 7) {
						clone.drawShape(bounds, 1, RGBColour.CYAN);
					} else {
						clone.drawShape(bounds, 1, RGBColour.MAGENTA);
					}
					//System.out.println("Person " + personCounter + " positioned at (" + xPos + ", " + yPos + ")");
					values[0] = xPos;
					values[1] = yPos;
				}
				counter++;
				x += width;
			}
			y += height;
			x = 0;
		}
	}
	
	public boolean determineIfSamePerson(int x1, int y1, int x2, int y2) {
		boolean isSamePerson = true;
		int xDifference = Math.abs(x2 - x1);
		int yDifference = Math.abs(y2 - y1);
		int totalDifference = xDifference - yDifference;
		if (xDifference > 1 || yDifference > 1 || totalDifference > 1) {
			isSamePerson = false;
		} else {
			isSamePerson = true;
		}
		return isSamePerson;
	}
	
	public void fillInWhitePixelArray(MBFImage image, float[][] whiteOut) {
		for (int y=1; y<image.getHeight() - 1; y++) {
		    for(int x=1; x<image.getWidth() - 1; x++) {
		    	whiteOut[y][x] = 1;
		    } 
		}
	}

	public int getTotalNumberOfWhitePixels(MBFImage image) {
		int counter = 0;
		for (int y=1; y<image.getHeight() - 1; y++) {
		    for(int x=1; x<image.getWidth() - 1; x++) {
		    	float sumFloat = 0;
		        float redColor = image.getBand(0).pixels[y][x];
		        float greenColor = image.getBand(1).pixels[y][x];
		        float blueColor = image.getBand(2).pixels[y][x];
		        sumFloat = (((redColor*256) + (greenColor*256) + (blueColor*256)) / 3);
		        if (sumFloat/256 < 0.98) {
		        	sumFloat = 1;
		        } else {
		        	sumFloat = sumFloat / 256;
		        	counter++;
		        }
		        grayScalePixels[y][x] = sumFloat;
		    } 
		}
		if (counter > 10) {
			colorInImage(image, grayScalePixels);
		} else {
			colorInImage(image, whitePixelArray);
		}
		return counter;
	}
	
	public int getTotalNumberOfBlackPixels(MBFImage image) {
		int counter = 0;
		for (int y=1; y<image.getHeight() - 1; y++) {
		    for(int x=1; x<image.getWidth() - 1; x++) {
		    	float sumFloat = 0;
		        float redColor = image.getBand(0).pixels[y][x];
		        float greenColor = image.getBand(1).pixels[y][x];
		        float blueColor = image.getBand(2).pixels[y][x];
		        sumFloat = (((redColor*256) + (greenColor*256) + (blueColor*256)) / 3);
		        if (sumFloat/256 > 0.99) {
		        	sumFloat = 1;
		        } else {
		        	sumFloat = sumFloat / 256;
		        	counter++;
		        }
		        grayScalePixels[y][x] = sumFloat;
		    } 
		}
		if (counter > 10) {
			colorInImage(image, grayScalePixels);
		} else {
			colorInImage(image, whitePixelArray);
		}
		return counter;
	}
	
	public static void colorInImage(MBFImage image, float[][] pixels) {
		for (int y=1; y<image.getHeight() - 1; y++) {
		    for(int x=1; x<image.getWidth() - 1; x++) {
		        image.getBand(0).setPixel(x, y, pixels[y][x]);
		        image.getBand(1).setPixel(x, y, pixels[y][x]);
		        image.getBand(2).setPixel(x, y, pixels[y][x]);
		    } 
		}
	}

	public static int getWidth(int currentWidth, MBFImage image, int currentX) {
		if (currentWidth < (image.getWidth() - currentX)) {
			//continue
		} else {
			if ((image.getWidth() - currentX - 1) < 0) {
				currentWidth = (image.getWidth() - currentX);
			} else {
				currentWidth = (image.getWidth() - currentX - 1);
			}
		}
		return currentWidth;
	}

	public static int getHeight(int currentHeight, MBFImage image, int currentY) {
		if ((image.getHeight() - currentHeight) > currentY) {
			//continue
		} else {
			if ((image.getHeight() - currentY - 1) < 0) {
				currentHeight = (image.getHeight() - currentY);
			} else {
				currentHeight = (image.getHeight() - currentY - 1);
			}
		}
		return currentHeight;
	}
}
