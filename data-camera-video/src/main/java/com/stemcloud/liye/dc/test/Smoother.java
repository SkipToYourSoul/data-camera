package com.stemcloud.liye.dc.test;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

/**
 * Belongs to data-cameta-video
 * Description:
 *
 * @author liye on 2018/4/15
 */
public class Smoother {
    public static void main(String[] args) {
        String fileName = "./1.jpg";
        IplImage image = cvLoadImage(fileName);
        if (image != null) {
            cvSmooth(image, image);
            cvSaveImage(fileName, image);
            cvReleaseImage(image);
        }
    }
}
