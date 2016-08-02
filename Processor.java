
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;

public class Processor
{
	public static void replaceGreen (MBFImage img, float c)
	{
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				float r = img.getBand(0).pixels[i][j];
				float g = img.getBand(1).pixels[i][j];
				float b = img.getBand(2).pixels[i][j];
				if (g > r && g > b)
				{
					img.getBand(0).pixels[i][j] = c;
					img.getBand(1).pixels[i][j] = c;
					img.getBand(2).pixels[i][j] = c;
				}
			}
	}

	public static void replaceBrown (MBFImage img, float c, HashSet<String> stuff, int tileWidth, int tileHeight)
	{
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				int rectX = j / tileWidth * tileWidth, rectY = i / tileHeight * tileHeight;
				if (stuff.contains(rectX + " " + rectY))
					continue;
				float r = img.getBand(0).pixels[i][j];
				float g = img.getBand(1).pixels[i][j];
				float b = img.getBand(2).pixels[i][j];
				float min = Float.min(Float.min(r, g), b);
				float max = Float.max(Float.max(r, g), b);
				if (r <= .8 && r >= .6 && g <= .8 && g >= .6 && b <= .8 && b >= .6 || max - min < .07)
				{
					img.getBand(0).pixels[i][j] = c;
					img.getBand(1).pixels[i][j] = c;
					img.getBand(2).pixels[i][j] = c;
				}
			}
	}

	public static void replaceBrownGray (MBFImage img, float c, double thresh, HashSet<String> stuff, int tileWidth, int tileHeight)
	{
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				int rectX = j / tileWidth * tileWidth, rectY = i / tileHeight * tileHeight;
				if (stuff.contains(rectX + " " + rectY))
					continue;
				float r = img.getBand(0).pixels[i][j];
				float g = img.getBand(1).pixels[i][j];
				float b = img.getBand(2).pixels[i][j];
				float min = Float.min(Float.min(r, g), b);
				float max = Float.max(Float.max(r, g), b);
				if (max - min < thresh)
				{
					img.getBand(0).pixels[i][j] = c;
					img.getBand(1).pixels[i][j] = c;
					img.getBand(2).pixels[i][j] = c;
				}
			}
	}

	public static void replaceAll (MBFImage img, float c, double thresh)
	{
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				float r = img.getBand(0).pixels[i][j];
				float g = img.getBand(1).pixels[i][j];
				float b = img.getBand(2).pixels[i][j];
				float min = Float.min(Float.min(r, g), b);
				float max = Float.max(Float.max(r, g), b);
				if (r > .9 || g > .9 || b > .9)
					continue;
				if (g > r && g > b || r <= .8 && r >= .6 && g <= .8 && g >= .6 && b <= .8 && b >= .6 || max - min < thresh)
				{
					img.getBand(0).pixels[i][j] = c;
					img.getBand(1).pixels[i][j] = c;
					img.getBand(2).pixels[i][j] = c;
				}
			}
	}

	public static void replaceWhite (MBFImage img, float c, double thresh)
	{
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				float r = img.getBand(0).pixels[i][j];
				float g = img.getBand(1).pixels[i][j];
				float b = img.getBand(2).pixels[i][j];
				float avg = (r + g + b) / 3;
				if (avg > thresh)
				{
					img.getBand(0).pixels[i][j] = c;
					img.getBand(1).pixels[i][j] = c;
					img.getBand(2).pixels[i][j] = c;
				}
			}
	}
	
	public static void findStandardDeviation(MBFImage image, int x, int y, int width, int height) {
		
	}

	/**
	 * Converts to grayscale
	 * @param img
	 */
	public static void grayscale (MBFImage img)
	{
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				float r = img.getBand(0).pixels[i][j];
				float g = img.getBand(1).pixels[i][j];
				float b = img.getBand(2).pixels[i][j];
				float avg = (r + g + b) / 3;
				img.getBand(0).pixels[i][j] = avg;
				img.getBand(1).pixels[i][j] = avg;
				img.getBand(2).pixels[i][j] = avg;
			}
	}

	/**
	 * Assumes grayscale image. Converts to black, white, and irrelevant (1, 0, -1) based on threshold value (0-255)
	 * @param img
	 * @param threshold
	 * @return
	 */
	public static int[][] binarize(MBFImage img, float minBlack, float maxBlack, float minWhite)
	{
		int[][] bin = new int[img.getHeight()][img.getWidth()];
		for (int i = 0; i < bin.length; i++)
			for (int j = 0; j < bin[i].length; j++)
				if (img.getBand(0).pixels[i][j] <= maxBlack && img.getBand(0).pixels[i][j] >= minBlack)
				{
					bin[i][j] = 1;
					img.getBand(0).pixels[i][j] = 0;
					img.getBand(1).pixels[i][j] = 0;
					img.getBand(2).pixels[i][j] = 0;
				}
				else if (img.getBand(0).pixels[i][j] >= minWhite)
				{
					bin[i][j] = 0;
					img.getBand(0).pixels[i][j] = 1;
					img.getBand(1).pixels[i][j] = 1;
					img.getBand(2).pixels[i][j] = 1;
				}
				else
				{
					bin[i][j] = -1;
					img.getBand(0).pixels[i][j] = .5f;
					img.getBand(1).pixels[i][j] = .5f;
					img.getBand(2).pixels[i][j] = .5f;
				}
		return bin;
	}

	public static int[][] edgeDetect (MBFImage img, double threshold)
	{
		int[][] bin = new int[img.getHeight()][img.getWidth()];
		int[][] moves = {{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};
		for (int i = 0; i < img.getHeight(); i++)
			for (int j = 0; j < img.getWidth(); j++)
			{
				float r = img.getBand(0).pixels[i][j];
				float g = img.getBand(1).pixels[i][j];
				float b = img.getBand(2).pixels[i][j];
				int edges = 0;
				for (int[] move : moves)
				{
					int x = j + move[0], y = i + move[1];
					if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight())
						continue;
					float rr = img.getBand(0).pixels[y][x];
					float gg = img.getBand(1).pixels[y][x];
					float bb = img.getBand(2).pixels[y][x];
					if (dist(new float[] {r, g, b}, new float[] {rr, gg, bb}) > threshold)
					{
						edges++;
						if (edges >= 4)
							break;
					}
				}
				if (edges >= 4)
					bin[i][j] = 1;
				else
					bin[i][j] = 0;
			}
		for (int i = 0; i < bin.length; i++)
			for (int j = 0; j < bin[i].length; j++)
			{
				float c = 1 - bin[i][j];
				img.getBand(0).pixels[i][j] = c;
				img.getBand(1).pixels[i][j] = c;
				img.getBand(2).pixels[i][j] = c;
			}
		return bin;
	}

	private static double dist (float[] rgb1, float[] rgb2)
	{
		double res = Math.pow(rgb1[0] - rgb2[0], 2) + Math.pow(rgb1[1] - rgb2[1], 2) + Math.pow(rgb1[2] - rgb2[2], 2);
		res = Math.sqrt(res);
		return res;
	}

	/**
	 * Finds contiguous sets black (1). The sets are ordered top-left to bottom-right
	 * @param binary
	 * @return
	 */
	/*
	public static ArrayList<TreeSet<int[]>> genBlobs (int[][] binary)
	{
		ArrayList<TreeSet<int[]>> blobs = new ArrayList<>();
		boolean[][] visited = new boolean[binary.length][binary[0].length];
		for (int i = 0; i < visited.length; i++)
			for (int j = 0; j < visited[i].length; j++)
				if (!visited[i][j] && binary[i][j] == 1)
					blobs.add(genBlob(i, j, visited, binary));
		return blobs;
	}*/

	/**
	 * Helper method for genBlobs
	 * @param i
	 * @param j
	 * @param visited
	 * @param binary
	 * @return
	 */
	/*
	private static TreeSet<int[]> genBlob (int i, int j, boolean[][] visited, int[][] binary)
	{
		TreeSet<int[]> blob = new TreeSet<>(new ComparatorIntArray());
		ArrayList<int[]> points = new ArrayList<>();
		points.add(new int[] {i, j});
		visited[i][j] = true;
		int[][] moves = {{0,1}, {0,-1}, {1,0}, {-1,0}};
		while (!points.isEmpty())
		{
			int[] point = points.remove(points.size() - 1);
			int x = point[0], y = point[1];
			blob.add(point);
			for (int[] move : moves)
			{
				int newX = x + move[0], newY = y + move[1];
				if (newX < 0 || newX >= binary.length || newY < 0 || newY >= binary[newX].length)
					continue;
				if (visited[newX][newY])
					continue;
				if (binary[newX][newY] != binary[x][y])
					continue;
				points.add(new int[] {newX, newY});
				visited[newX][newY] = true;
			}
		}
		return blob;
	}
*/	/*
	public static ArrayList<TreeSet<int[]>> genRelevantBlobs (int[][] binary, ArrayList<Rectangle> boxes)
	{
		ArrayList<TreeSet<int[]>> blobs = new ArrayList<>();
		boolean[][] visited = new boolean[binary.length][binary[0].length];
		for (int i = 0; i < visited.length; i++)
			for (int j = 0; j < visited[i].length; j++)
				if (!visited[i][j] && binary[i][j] == 1 && isRelevant(i, j, boxes))
					blobs.add(genBlob(i, j, visited, binary));
		return blobs;
	}
*/
	private static boolean isRelevant (int i, int j, ArrayList<Rectangle> boxes)
	{
		for (Rectangle r : boxes)
			if (j >= r.x && j <= r.x + r.width && i >= r.y && i <= r.y + r.height)
				return true;
		return false;
	}

	public static ArrayList<Rectangle> genRelevantBoxesAndGrayscale (MBFImage image, int tileWidth, int tileHeight)
	{
		ArrayList<Rectangle> boxes = new ArrayList<>();
		for (int y = 0; y < image.getHeight(); y += tileHeight)
			for (int x = 0; x < image.getWidth(); x += tileWidth)
			{
				Rectangle bounds = new Rectangle(x, y, Math.min(tileWidth, image.getWidth() - x - 1), Math.min(tileHeight, image.getHeight() - y - 1));
				int numWhite = 0, numBlack = 0;
				for (int yy = y; yy < y + bounds.height; yy++)
					for (int xx = x; xx < x + bounds.width; xx++)
					{
						float r = image.getBand(0).pixels[yy][xx];
						float g = image.getBand(1).pixels[yy][xx];
						float b = image.getBand(2).pixels[yy][xx];
						float avg = (r + g + b) / 3;
						if (avg >= .98)
							numWhite++;
						if (avg <= .99)
							numBlack++;
						image.getBand(0).pixels[yy][xx] = avg;
						image.getBand(1).pixels[yy][xx] = avg;
						image.getBand(2).pixels[yy][xx] = avg;
					}
				if (numWhite > 100 && numBlack > 20)
					boxes.add(bounds);
			}
		return boxes;
	}
}
