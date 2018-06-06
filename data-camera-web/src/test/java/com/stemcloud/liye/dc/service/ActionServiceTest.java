package com.stemcloud.liye.dc.service;

import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Belongs to data-camera-web
 * Description:
 *  test record/monitor action
 * @author liye on 2018/6/6
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActionServiceTest {
    @Autowired
    ActionService actionService;

    @Test
    public void testRecordByFrame() throws InterruptedException {
        String inputFile = "rtmp://47.100.173.108:1935/live/stem";
        RecorderInfo recorderInfo = new RecorderInfo();
        recorderInfo.setAppId(0);
        recorderInfo.setExpId(0);
        recorderInfo.setId(0);

        actionService.startRecordByFrame(inputFile, 0, recorderInfo,0);
        Thread.sleep(10000);
        actionService.endRecorderByFrame(recorderInfo,0);
        Thread.sleep(10000);
    }
}