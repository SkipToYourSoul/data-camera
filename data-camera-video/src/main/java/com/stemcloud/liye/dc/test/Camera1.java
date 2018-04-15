package com.stemcloud.liye.dc.test;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.swing.*;

/**
 * Belongs to data-cameta-video
 * Description:
 *
 * @author liye on 2018/4/15
 */
public class Camera1 {
    public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        CanvasFrame canvasFrame = new CanvasFrame("test");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);

        while (true) {
            if (!canvasFrame.isDisplayable()) {
                grabber.stop();
                System.exit(2);
            }
            canvasFrame.showImage(grabber.grab());

            Thread.sleep(50);
        }
    }
}
