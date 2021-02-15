package com.example.health_checker;

// This class computes the Heart rate

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity
{
    public static String TAG = "Debug";
    Algorithms algorithms = new Algorithms();

    // Function to measure the heart rate
    public String measure_heart_rate(String videoPath, String videoName) throws IOException {
        VideoCapture videoCapture = new VideoCapture();

        // Reading the .avi file into opencv functions
        if(new File(videoPath + videoName).exists()){
            Log.d(TAG, "AVI file exists!");
            videoCapture.open(videoPath + videoName);
            if(videoCapture.isOpened()){
                Log.d(TAG, "isOpened() works!");

                Mat current_frame = new Mat();
                Mat next_frame = new Mat();
                Mat diff_frame = new Mat();

                List<Double> extremes = new ArrayList<Double> ();


                int video_length = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
                Log.d(TAG, "Video Length: " + video_length);
                int frames_per_second = (int) videoCapture.get(Videoio.CAP_PROP_FPS);
                Log.d(TAG, "Frames per second: " + frames_per_second);

                List<Double> list = new ArrayList<Double>();

                // Processing the video to calculate the mean red pixel values in the frame
                videoCapture.read(current_frame);
                for(int k = 0; k < video_length - 1; k++){
                    videoCapture.read(next_frame);
                    Core.subtract(next_frame, current_frame, diff_frame);
                    next_frame.copyTo(current_frame);
                    list.add(Core.mean(diff_frame).val[0] + Core.mean(diff_frame).val[1] + Core.mean(diff_frame).val[2]);
                }

                List<Double> new_list = new ArrayList<Double>();
                for(int i = 0; i < (Integer)(list.size()/5) - 1; i++){
                    List<Double> sublist = list.subList(i*5, (i+1)*5);
                    double sum = 0.0;
                    for(int j = 0; j < sublist.size(); j++){
                        sum += sublist.get(j);
                    }

                    new_list.add(sum/5);
                }

                int mov_period = 50;
                // Calculating the moving average and performing peak detection on the signal
                List<Double> avg_data = algorithms.calculate_moving_avg(mov_period, new_list);
                int peak_counts = algorithms.count_zero_crossings(avg_data);

                double fps_to_sec = (video_length/frames_per_second);
                double count_heart_rate = (peak_counts/2)*(60)/fps_to_sec;

                return ""+count_heart_rate;

            }
            else{
                Log.d(TAG, ":(");
                return "";
            }
        }
        else{
            Log.d(TAG, "AVI file does not exist!");
            return "";
        }
    }
}
