package com.stemcloud.liye.dc.socket;

import com.stemcloud.liye.dc.common.PropKit;
import com.stemcloud.liye.dc.socket.codec.PacketCodec;
import com.stemcloud.liye.dc.socket.handler.PacketHandler;
import com.stemcloud.liye.dc.socket.service.PacketService;
import com.stemcloud.liye.dc.socket.service.Service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.stemcloud.liye.dc.socket.common.Packet.MAX_FRAME_LENGTH;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public final class NettyServer implements Server {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServer.class);
    public static final NettyServer I = new NettyServer();

    private AtomicBoolean started = new AtomicBoolean(false);
    private Service service = new PacketService();
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ChannelFuture channelFuture;
    private int port;


    private NettyServer(){
        PropKit propKit = PropKit._default();
        port = propKit.getInt("server.port");
        int bossThread = propKit.getInt("server.bossThread");
        int workerThread = propKit.getInt("server.workerThread");
        boss = new NioEventLoopGroup(bossThread);
        worker = new NioEventLoopGroup(workerThread);
    }


    @Override
    public void start(ChannelFutureListener listener) {
        try {
            if (started.compareAndSet(false, true)) {
                ServerBootstrap b = new ServerBootstrap()
                        .group(boss, worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline().addLast("LengthFieldDecoder",
                                        new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 6, 4));
                                // 数据包解码
                                ch.pipeline().addLast("PacketCodec",
                                        new PacketCodec());
                                // 数据处理逻辑
                                ch.pipeline().addLast("ServerHandler",
                                        new PacketHandler(service, Threads.RECEIVER_DATA_HANDLER));
                            }
                        });

                LOG.info("Starting Server... Port: '{}'", port);

                channelFuture = b.bind(port).sync();

                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            LOG.info("Bind Port Successfully");
                        } else {
                            LOG.error("Bind Failed.", future.cause());
                        }
                    }
                });
                if (listener != null){
                    channelFuture.addListener(listener);
                }
            }else {
                LOG.warn("Server Already Started!");
            }

        } catch (Exception e){
            LOG.error("Start Server Error", e);
        }
    }

    @Override
    public void start() {
        start(null);
    }

    @Override
    public void shutdown() {
        if (channelFuture != null) {
            channelFuture.channel().close().syncUninterruptibly();
        }
        if (boss != null) {
            boss.shutdownGracefully();
        }
        if (worker != null) {
            worker.shutdownGracefully();
        }
        Threads.shutdown();
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }
}
