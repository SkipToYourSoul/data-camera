package com.stemcloud.liye.dc.websocket;

import com.stemcloud.liye.dc.common.PropKit;
import com.stemcloud.liye.dc.socket.Server;
import com.stemcloud.liye.dc.socket.Threads;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2018/9/5
 */
public class WebSocketServer implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);
    private static final String SOCKET_ADDRESS = "/dc-websocket";
    public static final WebSocketServer I = new WebSocketServer();

    private AtomicBoolean started = new AtomicBoolean(false);
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ChannelFuture channelFuture;
    private int port;

    private WebSocketServer() {
        PropKit propKit = PropKit._default();
        port = propKit.getInt("websocket.port");
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
                            protected void initChannel(Channel ch) {
                                // HttpServerCodec：将请求和应答消息解码为HTTP消息
                                ch.pipeline().addLast(new HttpServerCodec());
                                // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
                                ch.pipeline().addLast(new HttpObjectAggregator(65536));
                                ch.pipeline().addLast(new WebSocketServerCompressionHandler());
                                ch.pipeline().addLast(new WebSocketServerProtocolHandler(SOCKET_ADDRESS, null,true));
                                ch.pipeline().addLast("ServerHandle", new WebSocketServerHandler());
                            }
                        });
                LOGGER.info("Starting Server... Port: '{}'", port);

                channelFuture = b.bind(port).sync();
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            LOGGER.info("Bind Port Successfully");
                        } else {
                            LOGGER.error("Bind Failed.", future.cause());
                        }
                    }
                });
                if (listener != null){
                    channelFuture.addListener(listener);
                }
            } else {
                LOGGER.warn("Server Already Started!");
            }
        } catch (Exception e) {
            LOGGER.error("Start Server Error", e);
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
