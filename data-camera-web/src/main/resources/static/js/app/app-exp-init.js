/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/6
 *  Description:
 *      初始化实验页面
 */

function initExperiment(iFi){
    if (iFi === false) {
        // 不是第一次加载
        return;
    } else {
        expObject.iFi = false;
    }

    // -- 初始化实验轨迹、传感器绑定信息
    Object.keys(experiments).forEach(function (exp_id) {
        var experiment = experiments[exp_id];

        // -- 遍历轨迹
        experiment['trackInfoList'].forEach(function (track, index) {
            var track_id = track['id'];
            var track_type = track['type'];

            // init track chart
            var legends = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            if (track_type === 1) {
                // --- 数值型传感器，可能存在多个维度
                legends.forEach(function (legend) {
                    var dom = "experiment-track-" + track_id + "-" + legend;
                    if (document.getElementById(dom) != null) {
                        var chart = echarts.init(document.getElementById(dom), "", opts = {height: 150});
                        chart.setOption(buildExperimentChartOption(legend));
                        expObject.setChart(dom, chart);
                    } else {
                        console.log(legend);
                    }
                });
            } else if (track_type === 2){
                // --- 视频直播
                Object.keys(expObject.video).forEach(function (id) {
                    // 移除之前创建的视频对象
                    expObject.video[id].remove();
                    delete expObject.video[id];
                });
                legends.forEach(function (legend) {
                    var dom = "#experiment-track-" + track_id + "-" + legend;
                    var videoId = "experiment-video-" + exp_id + "-" + track_id;
                    var liveAddress = track['sensor']['mark'];

                    // rtmp://47.100.173.108:1935/live/stem
                    // rtmp://live.hkstv.hk.lxdns.com/live/hks
                    $(dom).css("padding", "10px 25px 0").html('<div id=' + videoId + '></div>');
                    /*var videoPlayer = cyberplayer(videoId).setup({
                        width: $(dom).width,
                        height: 300,
                        file: "rtmp://47.100.173.108:1935/live/" + liveAddress, // <—rtmp直播地址
                        autostart: true,
                        stretching: "uniform",
                        volume: 100,
                        controls: true,
                        rtmp: {
                            reconnecttime: 5, // rtmp直播的重连次数
                            bufferlength: 1 // 缓冲多少秒之后开始播放 默认1秒
                        },
                        ak: "3c482e9f90a641cfab6a236960fbb707" // 公有云平台注册即可获得accessKey
                    });*/

                    // 临时替换方案
                    var videoPlayer = cyberplayer(videoId).setup({
                        width: $(dom).width,
                        height: 300,
                        file: "/camera/img/rocket.mp4",
                        autostart: false,
                        stretching: "uniform",
                        volume: 100,
                        controls: "over",
                        repeat: true,
                        ak: "3c482e9f90a641cfab6a236960fbb707" // 公有云平台注册即可获得accessKey
                    });

                    expObject.setVideo(videoId, videoPlayer);
                });
            } else if (track_type === 0){
                
            }

            // 初始化轨迹绑定传感器的设置
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
    Object.keys(isExperimentMonitor).forEach(function (id) {
        if (isExperimentMonitor[id] === 1) {
            pageStartMonitor(id);
        }
        if (isExperimentRecorder[id] === 1){
            pageStartRecord(id, parseTime(expRecorderTime[id]));
        }
    });
}

function initExpSelect() {
    // 初始化内容
    var $exp_select = $('#exp-add-select');
    var valueHtml = '<optgroup label="<b>数值型传感器</b>">';
    var videoHtml = '<optgroup label="<b>摄像头</b>">';
    sensors.forEach(function (sensor, index) {
        var id = sensor['id'];
        var type = sensor['sensorConfig']['type'];
        var text = sensor['name'];
        if (type == 1){
            if (freeSensors.hasOwnProperty(id)){
                valueHtml += '<option value="' + sensor.id + '">' + text + '</option>';
            } else {
                valueHtml += '<option value="' + sensor.id + '" disabled="disabled">' + text + '(已绑定)</option>';
            }
        } else if (type == 2){
            if (freeSensors.hasOwnProperty(id)){
                videoHtml += '<option value="' + sensor.id + '">' + text + '</option>';
            } else {
                videoHtml += '<option value="' + sensor.id + '" disabled="disabled">' + text + '(已绑定)</option>';
            }
        }
    });
    valueHtml += '</optgroup>';
    videoHtml += '</optgroup>';
    $exp_select.html(valueHtml + videoHtml);

    // 初始化添加传感器的SELECT
    $exp_select.multiSelect({
        selectableHeader: "<input type='text' class='form-control muti-select-search-input' autocomplete='off' placeholder='搜索设备'>",
        selectionHeader: "<div style='height: 40px; font-size: 16px; font-weight: 600; padding-top: 5px; color: #9d9d9d'>已选设备</div>",
        afterInit: function(ms){
            var that = this,
                $selectableSearch = that.$selectableUl.prev(),
                selectableSearchString = '#'+that.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)';

            that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
                .on('keydown', function(e){
                    if (e.which === 40){
                        that.$selectableUl.focus();
                        return false;
                    }
                });
        },
        afterSelect: function(){
            this.qs1.cache();
        },
        afterDeselect: function(){
            this.qs1.cache();
        }
    });

    // 初始化删除传感器和轨迹的SELECT
    var $expDelSelect = $('#exp-del-select');
    $expDelSelect.multiSelect({
        selectableHeader: "<input type='text' class='form-control muti-select-search-input' autocomplete='off' placeholder='搜索设备'>",
        selectionHeader: "<div style='height: 40px; font-size: 16px; font-weight: 600; padding-top: 5px; color: #9d9d9d'>已选设备</div>",
        afterInit: function(ms){
            var that = this,
                $selectableSearch = that.$selectableUl.prev(),
                selectableSearchString = '#'+that.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)';

            that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
                .on('keydown', function(e){
                    if (e.which === 40){
                        that.$selectableUl.focus();
                        return false;
                    }
                });
        },
        afterSelect: function(){
            this.qs1.cache();
        },
        afterDeselect: function(){
            this.qs1.cache();
        }
    });
}
