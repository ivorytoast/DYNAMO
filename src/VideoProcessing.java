
import java.io.File;
import java.util.HashSet;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

public class VideoProcessing {	
	public static void main(String[] args) {
		/**
		 * Get the Video File and Display It
		 */
		Video<MBFImage> video = new XuggleVideo(new File("/Users/Anthony/Desktop/objectSearch.mov"));
		
		/**
		 * Iterate through the frames of the file
		 * "mbfImage" is the iterator which acts as each individual frame through the run through
		 */
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
		display.addVideoListener(new VideoDisplayListener<MBFImage>() {
		    public void beforeUpdate(MBFImage frame) {	
				ImageProcessing processor = new ImageProcessing(frame);
				MBFImage clone = frame.clone();
				int tileWidth = 5, tileHeight = 5;
				Processor jackProcessor = new Processor();
				jackProcessor.replaceGreen(clone, 1f);
				HashSet<String> stuff = processor.fillSD(clone, tileWidth, tileHeight, .009);
				jackProcessor.replaceBrown(clone, 1f, stuff, tileWidth, tileHeight);
				jackProcessor.replaceBrownGray(clone, 1, 0.18f, stuff, tileWidth, tileHeight);
				
				processor.findObjects(clone, tileWidth, tileHeight, frame);
				//jackProcessor.replaceWhite(frame, 0.9f, 0.1f);
				//processor.reduceNoise(frame, 80, 80);
				//processor.performSegmentation(frame, 80, 80, processor.values);
				
		    }

		    public void afterUpdate(VideoDisplay<MBFImage> display) {
		    }
		  });
		  
	}
}