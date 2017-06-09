/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.unizh.ini.jaer.projects.minliu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.nio.file.Files.list;
import static java.rmi.Naming.list;
import java.util.ArrayList;
import static java.util.Collections.list;
import java.util.List;
import org.opencv.core.Core; 
import org.opencv.core.CvType; 
import org.opencv.core.Mat; 
import org.opencv.core.MatOfByte; 
import org.opencv.core.MatOfFloat; 
import org.opencv.core.MatOfPoint; 
import org.opencv.core.TermCriteria;
import org.opencv.core.MatOfPoint2f; 
import org.opencv.core.Point; 
import org.opencv.core.Rect; 
import org.opencv.core.RotatedRect; 
import org.opencv.core.Scalar; 
import org.opencv.core.Size; 
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import java.util.Random;
import org.bytedeco.javacpp.opencv_videoio.VideoWriter;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author minliu
 */
public class OpenCVFlow {
    
    static { 
    String jvmVersion = System.getProperty("sun.arch.data.model");
        
    try {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    // System.loadLibrary("opencv_ffmpeg320_" + jvmVersion);   // Notice, cannot put the file type extension (.dll) here, it will add it automatically. 
    } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library failed to load.\n" + e);
        System.exit(1);
        }
    }
    
    public static void main(String[] args) throws Exception {

            System.out.println("Welcome to OpenCV " + Core.VERSION);

            Mat m = new Mat(5, 5, CvType.CV_8UC1, new Scalar(1));
            System.out.println("OpenCV Mat: " + m);
            
            VideoCapture cap  = new VideoCapture("slow.flv");
            
            // params for ShiTomasi corner detection
            FeatureParams feature_params  = new FeatureParams(100, 0.3, 7, 7);
            
            // Parameters for lucas kanade optical flow
            LKParams lk_params = new LKParams(15, 15, 2, new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 10, 0.03));            

            // Create some random colors
            Random rand = new Random();         
            int[][] color = new int[100][3];
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 3; j++) {
                    color[i][j] = rand.nextInt(255);
                }
            }
            
            // Take first frame and find corners in it
            Mat old_frame = new Mat();
            Mat old_gray = new Mat();
            MatOfPoint p0 = new MatOfPoint();

            boolean ret = cap.read(old_frame);
            if(old_frame.empty() || ret == false) {
                System.out.println("The frame cannot be read or the frame is empty.\n");
                System.exit(1);                
            }
            
            // Convert it to grayscale and find the good features.
            Imgproc.cvtColor(old_frame,old_gray,Imgproc.COLOR_BGR2GRAY);
            Imgproc.goodFeaturesToTrack(old_gray, p0, feature_params.maxCorners, feature_params.qualityLevel, feature_params.minDistance); 
            
            while(true) {
                Mat frame = new Mat();
                Mat frame_gray = new Mat();
                for(int skipNum = 0; skipNum <= 20; skipNum ++) {
                    ret = cap.read(frame);                    
                }
                if(frame.empty() || ret == false) {
                    System.out.println("The frame cannot be read or the frame is empty.\n");
                    System.exit(1);                
                }            
                
                Imgproc.cvtColor(frame,frame_gray,Imgproc.COLOR_BGR2GRAY);
                Imgproc.goodFeaturesToTrack(old_gray, p0, feature_params.maxCorners, feature_params.qualityLevel, feature_params.minDistance); 

                MatOfPoint2f prevPts = new MatOfPoint2f(p0.toArray());
                MatOfPoint2f nextPts = new MatOfPoint2f();
                MatOfByte status = new MatOfByte();
                MatOfFloat err = new MatOfFloat();

                int featureNum = prevPts.checkVector(2, CvType.CV_32F, true);
                System.out.println("The number of feature detected is : " + featureNum);

                try {
                    Video.calcOpticalFlowPyrLK(old_gray, frame_gray, prevPts, nextPts, status, err);            
                } catch (Exception e) {
                    System.err.println(e);
                    frame_gray.copyTo(old_gray);
                    continue;
                }

                // TODO: Select good points 

                // draw the tracks
                Point[] prevPoints = prevPts.toArray();
                Point[] nextPoints = nextPts.toArray();
                byte[] st = status.toArray();
                float[] er = err.toArray();    
                Mat mask = new Mat(old_gray.rows(), old_gray.cols(), CvType.CV_8UC1);
                for (int i = 0; i < prevPoints.length; i++) {
                    Imgproc.line(frame, prevPoints[i], nextPoints[i], new Scalar(color[i][0],color[i][1],color[i][2]), 2);  
                    Imgproc.circle(frame,prevPoints[i],5,new Scalar(color[i][0],color[i][1],color[i][2]),-1);
                }

                // Save the frames.
                // Imgcodecs.imwrite("tmpfiles/old_frame.jpg", old_frame);
                Imgcodecs.imwrite("tmpfiles/frame.jpg", frame);  
                
                // Now update the previous frame and previous points
                frame_gray.copyTo(old_gray);
                p0 = new MatOfPoint(nextPts.toArray());
            }

            // Imgcodecs.imwrite("tmpfiles/OF.jpg", mask);                 
    }
    
    public static class FeatureParams {
        
        int maxCorners;
        double qualityLevel;
        double minDistance;
        int blockSize;

        public FeatureParams(int maxCorners, double qualityLevel, int minDistance, int blockSize) {
            this.maxCorners = maxCorners;
            this.qualityLevel = qualityLevel;
            this.minDistance = minDistance;
            this.blockSize = blockSize;
        }
    }  

    public static class LKParams {
        int winSizeX;
        int winSizeY;
        int maxLevel;
        TermCriteria criteria = new TermCriteria();

        public LKParams(int winSizeX, int winSizeY, int maxLevel, TermCriteria criteria) {
            this.winSizeX = winSizeX;
            this.winSizeY = winSizeY;
            this.maxLevel = maxLevel;
            this.criteria = criteria;
        }
    }
    
}


