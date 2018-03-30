/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/6
 *  Description:
 *      初始化实验页面
 */

function initExperiment(iFi){
    if (iFi == false) {
        // 不是第一次加载
        return;
    } else {
        expObject.iFi = false;
    }

    // -- 初始化实验轨迹、传感器绑定信息
    Object.keys(experiments).forEach(function (exp_id) {
        var experiment = experiments[exp_id];
        // - 设置该实验的最新数据时间
        expObject.setNewTime(exp_id, new Date().getTime());

        // -- 遍历轨迹
        experiment['trackInfoList'].forEach(function (track, index) {
            var track_id = track['id'];
            var track_type = track['type'];

            // init track chart
            var legends = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            if (track_type == 1) {
                // --- value sensor
                legends.forEach(function (legend) {
                    var dom = "experiment-track-" + exp_id + "-" + track_id + "-" + legend;
                    var chart = echarts.init(document.getElementById(dom), "", opts = {height: 150});
                    chart.setOption(buildExperimentChartOption(legend));
                    expObject.setChart(dom, chart);
                });
            } else if (track_type == 2){
                // --- 视频直播
                Object.keys(expObject.video).forEach(function (id) {
                    videojs(id).dispose();
                    delete expObject.video[id];
                });
                legends.forEach(function (legend) {
                    var dom = "experiment-track-" + exp_id + "-" + track_id + "-" + legend;
                    var videoId = "experiment-video-" + exp_id + "-" + track_id;
                    $('#' + dom).html('<div style="padding: 10px 25px 0">' +
                        '<video id="' + videoId + '"class="video-js vjs-fluid vjs-big-play-centered" data-setup="{}"></video></div>');
                    var video = videojs(videoId, {
                        controls: false,
                        /*poster: "/camera/img/video-load2.jpg",*/
                        preload: "auto",
                        loop: true,
                        sources: [{src: "/camera/img/oceans.mp4", type: "video/mp4"}],
                        techOrder: ["html5", "flash"]
                    }, function () {
                    });
                    expObject.setVideo(videoId, video);
                });
            } else if (track_type == 0){
                
            }

            // init track bound sensor
            var $track_bound_dom = $('#track-bound-' + track_id);
            var sensor = (null == track['sensor'])?null:track['sensor'];
            var source = [];
            var value = "";
            if (null != sensor){
                // already bound
                source.push({
                    value: sensor.id,
                    text: sensor.name
                });
                value = sensor.id;
            } else {
                // not bound, add freeSensors to source
                sensors.forEach(function (sensor, index) {
                    var id = sensor['id'];
                    if (freeSensors.hasOwnProperty(id) && track['type'] == freeSensors[id]['sensorConfig']['type']){
                        source.push({
                            value: freeSensors[id].id,
                            text: freeSensors[id].name
                        });
                    } else if (sensor['sensorConfig']['type'] == track['type']){
                        source.push({
                            value: id,
                            text: sensor['name'] + "(绑定于：" + apps[sensor['appId']]['name'] + ")",
                            disabled: true
                        });
                    }
                });
            }
            $track_bound_dom.editable({
                prepend: '不绑定设备',
                source: source,
                value: value,
                sourceError: 'error loading data',
                pk: track_id,
                validate: function (value) {
                    var track_id = $(this)['context']['id'].split('-')[2];
                    var track_exp_id = tracks[track_id]['experiment']['id'];
                    if (isExperimentMonitor.hasOwnProperty(track_exp_id) && isExperimentMonitor[track_exp_id] == 1){
                        return '数据监控中，不能进行绑定操作';
                    }
                },
                url: crud_address + '/bound/toggle',
                success: function (response) {
                    if (response.code == "0000"){
                        window.location.href = current_address + "?id=" + app['id'];
                    } else if (response.code == "1111") {
                        commonObject.printExceptionMsg(response.data);
                    }
                },
                error: function () {
                    commonObject.printRejectMsg();
                }
            });
        });
    });

    // 为chart添加resize监听
    $(window).resize(function() {
        onChartResize(expObject.chart);
    });

    // -- 初始化实验监控和录制状态
    initActionStatus();
}

function initActionStatus(){
    // -- 更改实验状态（如果在监控或录制状态）
    Object.keys(isExperimentMonitor).forEach(function (id) {
        var exp_monitor_dom = $('#experiment-es-' + id);
        var exp_recorder_dom = $('#experiment-rs-' + id);

        if (isExperimentMonitor[id] == 1){
            exp_monitor_dom.removeClass('label-default').addClass('label-success').text('正在监控');

            if (isExperimentRecorder[id] == 1){
                exp_recorder_dom.removeClass('label-default').addClass('label-success').text('正在录制');
                expObject.setRecorderTime(id, [expRecorderTime[id]]);
                expObject.setNewTime(id, new Date(parseTime(expRecorderTime[id])).getTime());
            }
            doInterval(id);

            // 模拟视频播放
            Object.keys(expObject.video).forEach(function (vid) {
                if (vid.split('-')[2] == id) {
                    videojs(vid).play();
                }
            });
        }
    });
}
