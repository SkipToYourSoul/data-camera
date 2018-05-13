$(function () {
    // /camera/img/oceans.mp4
    // rtmp://localhost/live/test
    // rtmp://live.hkstv.hk.lxdns.com/live/hks
    // rtmp://push.stemcloud.cn/AppName/StreamName?auth_key=1524478514-0-0-bef0746a6b3630faa9a13c8d1b8e1711
    // rtmp://47.100.173.108:1935/live/stem

    var player = cyberplayer("video").setup({
        width: 600,
        height: 300,
        isLive: true,
        file: "rtmp://47.100.173.108:1935/live/stem", // <—rtmp直播地址
        autostart: true,
        stretching: "uniform",
        volume: 100,
        controls: true,
        rtmp: {
            reconnecttime: 5, // rtmp直播的重连次数
            bufferlength: 1 // 缓冲多少秒之后开始播放 默认1秒
        },
        ak: "3c482e9f90a641cfab6a236960fbb707" // 公有云平台注册即可获得accessKey
    });
    // player.seek(0);
    /*console.log(player.getBuffer());
    player.stop();
    player.load({file: "rtmp://localhost/live/test"});*/


    /*$f("flow-video", "/camera/source/video-baidu/flowplayer-3.2.18.swf", {
        clip: {
            /!*url: "/camera/img/oceans.mp4",
            autoPlay: true,
            autoBuffering: true,*!/

            url: "test",
            live: true,
            provider: 'influxis'
        },
        plugins: {
            influxis: {
                url: "/camera/source/video-baidu/flowplayer.rtmp-3.2.13.swf",
                // netConnectionUrl defines where the streams are found
                netConnectionUrl: 'rtmp://localhost/live/'
            }
        }
    });*/


    /*var video = videojs("my-js-video", {
        controls: true,
        height: 300,
        width: 600,
        preload: "auto",
        autoplay: true,
        loop: true,
        sources: [
            {src: "rtmp://live.hkstv.hk.lxdns.com/live/hks", type: "rtmp/flv"}
        ],
        techOrder: ["flash", "html5"]
    }, function () {
    });*/

    /*var ksVideo = ksplayer("ks-video", {
        "controls": true,
        "autoplay": true,
        "preload": "auto",
        "sources": [
            {src: "rtmp://localhost/live/test", type: "rtmp/flv"}
        ],
        techOrder: ["flash", "html5"]
    }, function(){
        // 播放器 (this) 已经初始化完毕
    });*/

});