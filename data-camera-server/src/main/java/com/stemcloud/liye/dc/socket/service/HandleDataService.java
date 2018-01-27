package com.stemcloud.liye.dc.socket.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stemcloud.liye.dc.Constants;
import com.stemcloud.liye.dc.common.RedisClient;
import com.stemcloud.liye.dc.dao.MysqlRepository;
import com.stemcloud.liye.dc.socket.AckResult;
import com.stemcloud.liye.dc.socket.Packet;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class HandleDataService implements Service {

    private static final Logger LOG = LoggerFactory.getLogger(HandleDataService.class);
    private static final RedisClient REDIS = RedisClient.I;

    @Override
    public void handle(ChannelHandlerContext context, Packet packet) {
        LOG.info("handle packet -> '{}'", packet);

        if (packet.isValue()){
            // 如果是文本
            try {
                handleTxtMonitor(context, packet);
                handleTxtRecord(context, packet);
                Packet ack = packet.ack(AckResult.OK);
                context.channel().write(ack);
            }catch (Exception e){
                LOG.error("handleTxtMonitor error, packet is '{}'", packet, e);
                Packet ack = packet.ack(AckResult.FAILED);
                context.channel().write(ack);
            }
        }else if (packet.isVideo()){
            // 如果是视频, 先预留
            LOG.info("video data.");
            Packet ack = packet.ack(AckResult.OK);
            context.channel().write(ack);
        } else {
            LOG.warn("wrong type of data type. skip it");
            Packet ack = packet.ack(AckResult.FAILED);
            context.channel().write(ack);
        }

    }

    private void handleTxtMonitor(ChannelHandlerContext context, Packet packet){
        String code = packet.getCode();
        String json = REDIS.msingle(String.class, Constants.RedisNamespace.MONITOR, code);
        if (json == null || json.isEmpty()){
            LOG.info("this packet do not monitor, code is '{}'", code);
        }else {
            // 处理监控
            Map<String, Object> meta = JSON.parseObject(json);
            Map<String, Object> data = packet.asJson();
            meta.putAll(data);
            MysqlRepository.saveValueDatas(meta);
        }

    }

    private void handleTxtRecord(ChannelHandlerContext context, Packet packet){
        String code = packet.getCode();
        String json = REDIS.msingle(String.class, Constants.RedisNamespace.RECORD, code);
        if (json == null || json.isEmpty()){
            LOG.info("this packet do not record, code is '{}'", code);
        }else {
            // 处理记录
            Map<String, Object> meta = JSON.parseObject(json);
            Map<String, Object> data = packet.asJson();
            meta.putAll(data);
            MysqlRepository.saveValueDatas(meta);
        }

    }


}
