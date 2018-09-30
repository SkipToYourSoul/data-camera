package com.stemcloud.liye.dc.test;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.*;

import java.util.logging.Logger;

/**
 * Belongs to data-camera-video
 * Description:
 *
 * @author liye on 2018/5/13
 */
public class GetCamera {
    private final static Logger LOGGER = Logger.getLogger(GetCamera.class.getName());

    private static final int VIDEO_WIDTH = 1280;
    private static final int VIDEO_HEIGHT = 720;
    private static final double FRAME_RATE = 25.0;
    private static final double MOTION_FACTOR = 1;

    /**
     * 按帧录制视频
     *
     * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
     * @param outputFile
     *            -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
     * @throws FrameGrabber.Exception
     * @throws FrameRecorder.Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     */
    public static void frameRecord(String inputFile, String outputFile, int audioChannel)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

        boolean isStart=true;//该变量建议设置为全局控制变量，用于控制录制结束
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, VIDEO_WIDTH, VIDEO_HEIGHT, audioChannel);

        recorder.setFrameRate(25.0);
        recorder.setVideoBitrate((int)((VIDEO_WIDTH * VIDEO_HEIGHT * FRAME_RATE) * MOTION_FACTOR * 0.07));
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        //设置视频编码  28 指代h.264
        recorder.setVideoCodec(28);
        recorder.setFormat("mp4");

        // 开始取视频源
        LOGGER.info("Start Recorder...");
        recordByFrame(grabber, recorder, isStart);
        LOGGER.info("End Recorder...");
    }

    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        int recorderSeconds = 500;

        try {//建议在线程中使用该方法
            grabber.start();
            recorder.start();
            Frame frame = null;
            LOGGER.info("Initial Complete");
            while (status && (frame = grabber.grabFrame()) != null && recorderSeconds > 0) {
                recorder.record(frame);
                recorderSeconds --;
                System.out.println(recorderSeconds);
            }
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String inputFile = "rtmp://47.100.173.108:1935/live/stem";
        String outputFile = "test3.mp4";
        frameRecord(inputFile, outputFile, 0);
    }
}
