/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/23
 *  Description:
 *    生成实验片段对应的数据图表
 *    使用的后台数据(app, recorders)
 */

// -- 入口函数，初始化数据图表以及视频
function initRecorderContentDom(recorderId){
    // 清空之前可能存在的轮询
    if (recorderInterval != null){
        clearInterval(recorderInterval);
    }
    analysisObject.playStatus = "normal";

    // 初始化片段数据，构造dom
    askForRecorderDataAndInitDom(recorderId);

    // 填写片段描述
    Object.keys(recorders).forEach(function (rid) {
        if (rid == recorderId){
            var recorder = recorders[rid];
            $('#app-analysis-title').val(recorder['name']);
            $('#app-analysis-desc').val(recorder['description']);
        }
    });
}

function askForRecorderDataAndInitDom(recorderId) {
    var $infoDom = $('#app-analysis-info');
    var $dom = $('#app-analysis-chart');
    var $dom2 = $('#app-analysis-video');

    // -- clear old dom content
    $infoDom.html('<div style="padding:10px">' +
        '<div class="alert alert-info" role="alert"><b>数据加载中</b></div></div>');
    Object.keys(analysisObject.video).forEach(function (id) {
        videojs(id).dispose();
        delete analysisObject.video[id];
    });
    $dom.empty();
    $dom2.empty();
    Object.keys(analysisObject.chart).forEach(function (id) {
        delete analysisObject.chart[id];
    });

    // 异步请求实验片段数据
    $.ajax({
        type: 'get',
        url: data_address + "/get-recorder-data",
        data: {
            "recorder-id": recorderId
        },
        success: function (response) {
            if (response.code == "0000") {
                var chartData = response.data['CHART'];
                var videoData = response.data['VIDEO'];
                initDom(chartData, videoData, response.data['MIN'], response.data['MAX']);
            } else if (response.code == "1111") {
                message_info("加载数据失败，失败原因为：" + response.data, 'error');
            }
        },
        error: function (response) {
            message_info("数据请求被拒绝", 'error');
        }
    });

    function initDom(chartData, videoData, minTime, maxTime){
        // 初始化时间进度条
        generateTimeLine(minTime, maxTime);

        // 片段没有任何数据
        if (isEmptyObject(chartData) && isEmptyObject(videoData)){
            $('#content-handle-bar').find('button').attr('disabled', true);
            $('#delete-btn').attr('disabled', false);
            $infoDom.html('<div style="padding: 0 20px">' +
                '<div class="alert alert-warning" role="alert"><b>录制时段内没有产生任何有效数据</b></div></div>');
            return;
        } else {
            $infoDom.empty();
            $('#content-handle-bar').find('button').attr('disabled', false);
        }

        // 初始化chart
        if (!isEmptyObject(chartData)) {
            Object.keys(chartData).forEach(function (sensorId) {
                var data = chartData[sensorId];
                var chartWidth = 0;
                Object.keys(data).forEach(function (legend) {
                    var chartId = "chart-" + recorderId + '-' + sensorId + '-' + legend;
                    $dom.append(generate(sensorId +'-'+new Date().getTime(), legend, chartId));
                    if (echarts.getInstanceByDom(document.getElementById(chartId)) == null){
                        var chart = echarts.init(document.getElementById(chartId), "", opts = {
                            height: 100
                        });
                        chart.setOption(buildAnalysisChartOption(chartData[sensorId][legend], legend));
                        chart.on('dblclick', function (params) {
                            console.log(params);
                            bootbox.dialog({
                                title: "为数据点添加描述",
                                message: '<div class="form-group">' +
                                '<textarea rows="3" class="form-control" id="dialog-data-mark" >' + params.name + '</textarea></div>',
                                async: false,
                                buttons: {
                                    cancel: {
                                        label: '<i class="fa fa-times"></i> 不保存',
                                        className: 'btn-danger'
                                    },
                                    confirm: {
                                        label: '<i class="fa fa-check"></i> 保存',
                                        className: 'btn-success',
                                        callback: function(){
                                            $.ajax({
                                                type: 'get',
                                                url: data_address + "/user-data-mark",
                                                data: {
                                                    "data-id": params.data.itemStyle.normal.id,
                                                    "data-mark": $('#dialog-data-mark').val()
                                                },
                                                success: function (response) {
                                                    if (response.code == "1111"){
                                                        commonObject.printExceptionMsg(response.data);
                                                    } else if (response.code == "0000"){
                                                        window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + analysisObject.currentRecorderId;
                                                    }
                                                },
                                                error: function () {
                                                    commonObject.printRejectMsg();
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        });

                        analysisObject.setChart(chartId, chart);
                        analysisObject.setChartData(chartId, chartData[sensorId][legend]);
                    }
                });
            });
            // 为chart添加resize监听(echarts初始化width若写死，则无法resize)
            onChartResize(analysisObject.chart);
            $(window).resize(function() {
                onChartResize(analysisObject.chart);
            });
        }

        // 初始化video
        if (!isEmptyObject(videoData)) {
            Object.keys(videoData).forEach(function (vSensorId) {
                var videoOption = videoData[vSensorId]['option'];
                var videoId = 'video-' + vSensorId;
                var videoDomId = 'video-dom-' + vSensorId;
                $dom2.append(generate(videoDomId + '-panel', "视频", videoDomId));

                if (videoOption['sources'] != null){
                    $('#' + videoDomId).append('<div style="padding-left: 15px"><video id="' + videoId + '"class="video-js vjs-fluid vjs-big-play-centered" data-setup="{}"></video></div>');
                    videojs(videoId, videoOption, function () {
                        videojs.log('The video player ' + videoId + ' is ready');
                        analysisObject.setVideo(videoId, this);
                        // 如果是某个片段的子片段，需要设置起始时间
                        analysisObject.videoStartTime = recorders[recorderId]['startSeconds'];
                        this.currentTime(recorders[recorderId]['startSeconds']);
                    });
                } else {
                    var progressBar = '<div class="progress">' +
                        '<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 45%">' +
                        '<span class="sr-only">45% Complete</span></div></div>';
                    $('#' + videoDomId).append('<div id=' + videoId + '> <p class="text-center">视频来自设备(编号：' + vSensorId + ')，上传中</p>' + progressBar + '</div>');
                }
            });
        }
    }
}

/**
 * 初始化播放进度条
 * @param minTime
 * @param maxTime
 */
function generateTimeLine(minTime, maxTime) {
    analysisObject.timeline = generateTimeList(minTime, maxTime);
    analysisObject.timelineStart = 0; analysisObject.timelineEnd = analysisObject.timeline.length - 1;
    analysisObject.secondLine = generateSecondList(minTime, maxTime);
    var step = 5;
    var timeLength = analysisObject.secondLine.length;
    if ( timeLength > 60 && timeLength <= 120){
        step = 10;
    } else if ( timeLength > 120 && timeLength <= 300 ){
        step = 30;
    } else if ( timeLength > 300 && timeLength <= 600 ){
        step = 60;
    } else if ( timeLength > 300 && timeLength <= 1800 ){
        step = 150;
    } else if ( timeLength > 1800 ) {
        step = 600;
    }
    $('#recorder-total-time').html(analysisObject.secondLine[analysisObject.secondLine.length - 1]);
    $(".slider").slider({
        range: true,
        min: 0,
        max: analysisObject.timeline.length - 1,
        values: [0, analysisObject.timeline.length - 1]
    }).slider("pips", {
        rest: 'label',
        step: step,
        labels: analysisObject.secondLine
    }).slider("float", {
        labels: analysisObject.secondLine
    }).on("slidechange", function(e,ui) {
        slideChange(e, ui);
    });
}

/**
 * 生成图表panel
 * @param panelId
 * @param title
 * @param contentId
 * @returns {string}
 */
function generate(panelId, title, contentId) {
    return '<div class="panel panel-default my-panel">' +
        '<div class="my-panel-head">' +
        '<div class="panel-title my-panel-title" style="margin-left:20px;margin-top:10px;">' +
        '<a role="button" data-toggle="collapse" href="#' + panelId + '" aria-expanded="true" class="app-group-title" style="color:#000"> ' + title + '</a>' +
        '</div></div>' +
        '<div id="' + panelId + '" class="panel-collapse collapse in" role="tabpanel">' +
        '<div class="panel-body my-panel-body"><div id="' + contentId + '"></div></div>' +
        '</div></div>';
}

/**
 * 绝对时间数组（时间）
 * @param minTime
 * @param maxTime
 * @returns {Array}
 */
function generateTimeList(minTime, maxTime) {
    var list = [];
    for(var index = Math.floor(minTime/1000); index <= Math.ceil(maxTime/1000); index += 1){
        list.push(new Date(index*1000).Format("yyyy-MM-dd HH:mm:ss"));
    }
    return list;
}

/**
 * 相对时间数组（秒数）
 * @param minTime
 * @param maxTime
 * @returns {Array}
 */
function generateSecondList(minTime, maxTime){
    var list = [];
    var second = 0;
    var minute = 0;
    var hour = 0;
    for(var index = Math.floor(minTime/1000); index <= Math.ceil(maxTime/1000); index += 1){
        var label = "";
        if (hour != 0){
            label += (hour>=10)?(hour + ":"):("0" + hour + ":");
        }
        label += (minute>=10)?(minute + ":"):("0" + minute + ":");
        label += (second>=10)?(second):("0" + second);
        list.push(label);

        second += 1;
        if (second == 60){
            second = 0;
            minute++;
        }
        if (minute == 60){
            minute = 0;
            hour ++;
        }
    }
    return list;
}
