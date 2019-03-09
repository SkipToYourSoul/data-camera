package com.stemcloud.liye.dc.util;

import com.stemcloud.liye.dc.dao.data.VideoDataRepository;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  控制直播流的录制，截取录制的直播流并保存文件和上传阿里云
 * @author liye on 2018/6/6
 */
@Component
public class LiveRecorderUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiveRecorderUtil.class);

    @Autowired
    OssService ossService;

    @Autowired
    VideoDataRepository videoDataRepository;

    @Value("${oss.server.path}")
    private static String baseServerUploadPath;

    @Value("${rtmp.live.server}")
    private static String liveServer;

    public static Map<String, String> recorderStatusMap = new HashMap<String, String>();
    private static final int VIDEO_WIDTH = 1280;
    private static final int VIDEO_HEIGHT = 720;
    private static final double FRAME_RATE = 25.0;
    private static final int MOTION_FACTOR = 1;

    public static String mkLiveVideoKey(long appId, long expId, long sensorId) {
        return String.format("[%s]-[%s]-[%s]", appId, expId, sensorId);
    }

    /**
     * 按帧录制视频
     * @param liveAddress 直播流地址
     * @param audioChannel 是否录制音频（0:不录制/1:录制）
     * @param sensorId 用以标记文件名
     */
    public boolean startRecordByFrame(String liveAddress, int audioChannel, long appId, long expId, long sensorId) {
        /*// 构造直播流地址
        String inputFile = liveServer + liveAddress;

        // 该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
        String key = LiveRecorderUtil.mkLiveVideoKey(appId, expId, sensorId);
        String outputFile = String.format("%s/%s.mp4", baseServerUploadPath, key);

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

        recorderStatusMap.put(key, "start");
        ExecutorUtil.RECORDER_EXECUTOR.submit(new SyncRecorder(grabber, recorder, key));

        int waitCount = 100; // 10s, 若超过10s仍然未开始录制，则返回false
        while (!recorderStatusMap.get(key).equals("doing") && waitCount > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitCount --;
        }

        return waitCount > 0;*/
        return true;
    }

    /**
     * 结束录制
     * @param recorderInfo
     * @param sensorId
     * @param isSave
     */
    public void endRecorderByFrame(RecorderInfo recorderInfo, long sensorId, long trackId, int isSave) {
        /*String key = mkLiveVideoKey(recorderInfo.getAppId(), recorderInfo.getExpId(), sensorId);
        final String filename = String.format("%s.mp4", key);

        // -- 移除全局变量中的key，停止直播流的录制并中断录制线程
        LiveRecorderUtil.recorderStatusMap.remove(key);
        if (isSave == 0) {
            return;
        }

        // 存储视频
        LOGGER.info("Save video data, sensor id is {}, track id is {}, recorder is {}", sensorId, trackId, recorderInfo.getId());
        VideoData videoData = new VideoData();
        videoData.setSensorId(sensorId);
        videoData.setTrackId(trackId);
        videoData.setRecorderInfo(recorderInfo);
        videoData.setVideoPath("/camera/img/rocket.mp4");
        final long vid = videoDataRepository.save(videoData).getId();*/

        // 异步上传阿里云
        /*ExecutorUtil.UPLOAD_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // -- 5s后上传，等待录制线程结束
                    Thread.sleep(5000);
                    String aliAddress = ossService.uploadVideoToOss(filename);
                    VideoData video = videoDataRepository.findOne(vid);
                    video.setVideoPath(aliAddress);
                    videoDataRepository.save(video);
                    LOGGER.info("--> Get video path from oss, the path={}", aliAddress);
                } catch (Exception e) {
                    LOGGER.error("Upload oss failure", e);
                }
            }
        });*/
    }

    /*class SyncRecorder implements Runnable {
        private FFmpegFrameGrabber grabber;
        private FFmpegFrameRecorder recorder;
        private String recorderKey;

        public SyncRecorder(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, String recorderKey) {
            this.grabber = grabber;
            this.recorder = recorder;
            this.recorderKey = recorderKey;
        }

        @Override
        public void run() {
            LOGGER.info("Build recorder threads: " + recorderKey);

            try {
                grabber.start();
                recorder.start();
                Frame frame;
                // LiveRecorderUtil.showStatusMap();
                LiveRecorderUtil.recorderStatusMap.put(recorderKey, "doing");
                // LiveRecorderUtil.showStatusMap();

                while (LiveRecorderUtil.recorderStatusMap.containsKey(recorderKey) && (frame = grabber.grabFrame()) != null) {
                    recorder.record(frame);
                }
                recorder.stop();
                grabber.stop();
            } catch (FrameRecorder.Exception e) {
                LOGGER.error("FrameRecorder.Exception", e);
            } catch (FrameGrabber.Exception e) {
                LOGGER.error("FrameGrabber.Exception", e);
            } finally {
                if (grabber != null) {
                    try {
                        grabber.stop();
                    } catch (FrameGrabber.Exception e) {
                        LOGGER.error("FrameRecorder.Exception", e);
                    }
                }
                if (recorder != null) {
                    try {
                        recorder.stop();
                    } catch (FrameRecorder.Exception e) {
                        LOGGER.error("FrameGrabber.Exception", e);
                    }
                }
            }

            LOGGER.info("Exit recorder threads: " + recorderKey);
        }
    }*/
}
