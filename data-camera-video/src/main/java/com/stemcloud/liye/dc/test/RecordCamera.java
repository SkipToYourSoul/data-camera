package com.stemcloud.liye.dc.test;


import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.*;

import javax.swing.*;

/**
 * Belongs to data-camera-video
 * Description:
 *
 * @author liye on 2018/4/15.
 */
public class RecordCamera {

    public static void record(String outputFile, double frameRate) throws FrameGrabber.Exception, FrameRecorder.Exception, InterruptedException {
        Loader.load(opencv_objdetect.class);

        FrameGrabber grabber = FrameGrabber.createDefault(0);
        // FrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        opencv_core.IplImage grabbedImage = converter.convert(grabber.grab());
        int width = grabbedImage.width();
        int height = grabbedImage.height();

        FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);
        recorder.start();

        long startTime = 0;
        long videoTs = 0;
        CanvasFrame frame = new CanvasFrame("recorder", CanvasFrame.getDefaultGamma()/grabber.getGamma());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        Frame rotatedFrame = converter.convert(grabbedImage);
        while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {
            rotatedFrame = converter.convert(grabbedImage);
            frame.showImage(rotatedFrame);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            videoTs = 1000 * (System.currentTimeMillis() - startTime);
            recorder.setTimestamp(videoTs);
            recorder.record(rotatedFrame);
            Thread.sleep(40);
        }

        frame.dispose();
        recorder.stop();
        recorder.release();
        grabber.stop();
    }

    public static void main(String[] args) {
        try {
            // record("./test.mp4", 25);

            String stem = "rtmp://video-center.alivecdn.com/AppName/StreamName?vhost=push.stemcloud.cn&auth_key=1524479131-0-0-5cf5378a644063bb45d663baabed01f5";
            String ali = "rtmp://47.100.173.108:1935/live/stem";
            String qiyi = "rtmp://10.5.138.8/live/test";

            record(ali, 25);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
