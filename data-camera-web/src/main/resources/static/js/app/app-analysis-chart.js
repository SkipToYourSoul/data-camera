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
    $('#app-analysis-title').val(recorders[analysisObject.currentRecorderId]['name']);
    $('#app-analysis-desc').val(recorders[analysisObject.currentRecorderId]['description']);
}

function askForRecorderDataAndInitDom(recorderId) {
    var $infoDom = $('#app-analysis-info');
    var $dom = $('#app-analysis-chart');
    var $dom2 = $('#app-analysis-video');
    var $dom3 = $('#app-analysis-define-chart');

    // -- clear old dom content
    $infoDom.html('<div style="padding:10px">' +
        '<div class="alert alert-info" role="alert"><b>数据加载中</b></div></div>');
    Object.keys(analysisObject.video).forEach(function (id) {
        videojs(id).dispose();
        delete analysisObject.video[id];
    });
    $dom.empty(); $dom2.empty(); $dom3.empty();
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
            if (response.code === "0000") {
                var chartData = response.data['CHART'];
                var videoData = response.data['VIDEO'];
                var eventData = response.data['EVENT'];
                var defineData = response.data['DEFINE'];
                initDom(chartData, videoData, defineData, eventData, response.data['MIN'], response.data['MAX']);
            } else if (response.code === "1111") {
                message_info("加载数据失败，失败原因为：" + response.data, 'error');
            }
        },
        error: function () {
            message_info("数据请求被拒绝", 'error');
        }
    });

    function initDom(chartData, videoData, defineData, eventData, minTime, maxTime){
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

        // 保存chart的数据维度，方便视频侧展示实时数据
        var chartLegends = {};

        // 初始化chart
        if (!isEmptyObject(chartData)) {
            // 初始化用户自定义图表
            if (!isEmptyObject(defineData)) {
                defineData.forEach(function (dInfo) {
                    var chartId = 'define-chart-' + dInfo['info']['id'];
                    var legend = dInfo['info']['x'] + "-" + dInfo['info']['y'];
                    $dom3.append(generateDefineChartDom(chartId, legend, dInfo['info']['name'], dInfo['info']['desc']));
                    chartLegends[chartId] = legend;

                    // define chart
                    var chart = echarts.init(document.getElementById(chartId), "walden", opts = {
                        height: 180
                    });
                    chart.setOption(buildAnalysisDefineChartOption(dInfo, legend));

                    var chartData = {
                        'time': dInfo['timestamp'],
                        'data': dInfo['data']
                    };
                    analysisObject.setChart(chartId, chart);
                    analysisObject.setChartData(chartId, chartData);
                });
            }

            // 录制生成的图表
            Object.keys(chartData).forEach(function (sensorId) {
                var data = chartData[sensorId];
                Object.keys(data).forEach(function (legend) {
                    var chartId = "chart-" + recorderId + '-' + sensorId + '-' + legend;
                    $dom.append(generateChartDom(legend, chartId));
                    chartLegends[chartId] = legend;

                    if (echarts.getInstanceByDom(document.getElementById(chartId)) == null){
                        var chart = echarts.init(document.getElementById(chartId), "walden", opts = {
                            height: 100
                        });
                        chart.setOption(buildAnalysisChartOption(chartData[sensorId][legend], legend));
                        chart.on('click', function (params) {
                            var chartId = $(this)[0]['_dom'].getAttribute('id');
                            if (params['componentType'] === 'series') {
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
                                                        "data-id": params.data.itemStyle.id,
                                                        "data-mark": $('#dialog-data-mark').val()
                                                    },
                                                    success: function (response) {
                                                        if (response.code === "1111"){
                                                            commonObject.printExceptionMsg(response.data);
                                                        } else if (response.code === "0000"){
                                                            // window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + analysisObject.currentRecorderId;
                                                            params['data']['name'] = $('#dialog-data-mark').val();
                                                            // 异步刷新图表
                                                            var dataIndex = params['dataIndex'];
                                                            var chartData = analysisObject.chartData[chartId];
                                                            chartData[dataIndex] = params['data'];
                                                            analysisObject.chart[chartId].setOption(buildAnalysisChartOption(chartData, params['seriesName']));
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
                            }
                        });

                        analysisObject.setChart(chartId, chart);
                        analysisObject.setChartData(chartId, chartData[sensorId][legend]);
                    }
                });
            });

            // 默认点击显示刚生成的用户自定义图表
            if (getQueryString("chart") != null) {
                var showDefineChartId = 'define-chart-' + getQueryString("chart");
                var $select = "[key=" + showDefineChartId + "]";
                clickAddCube($($select));
            }

            // 如果没有视频数据，则初始化数据方格
            if (isEmptyObject(videoData)) {
                $('#chart-cube').html(generateChartCube(chartLegends));
            }

            // 为chart添加resize监听(echarts初始化width若写死，则无法resize)
            onChartResize(analysisObject.chart);
            $(window).resize(function() {
                onChartResize(analysisObject.chart);
            });
        }

        // 初始化video
        if (!isEmptyObject(videoData)) {
            var videoHeight = 50;
            Object.keys(videoData).forEach(function (vSensorId) {
                var videoOption = videoData[vSensorId]['option'];
                var videoId = 'video-' + vSensorId;
                var videoDomId = 'video-dom-' + vSensorId;
                $dom2.append(generateVideoDom(videoDomId));

                if (videoOption['sources'] != null){
                    var $videoDom = $('#' + videoDomId);
                    $videoDom.append('<div style="padding-left: 50px"><video id="' + videoId + '"class="video-js vjs-fluid vjs-big-play-centered" data-setup="{}"></video></div>');
                    videojs(videoId, videoOption, function () {
                        videojs.log('The video player ' + videoId + ' is ready');
                        analysisObject.setVideo(videoId, this);
                        // 如果是某个片段的子片段，需要设置起始时间
                        analysisObject.videoStartTime = recorders[recorderId]['startSeconds'];
                        this.currentTime(recorders[recorderId]['startSeconds']);

                        // 等待video高度自适应调整后，再做数据cube的生成
                        setTimeout(function () {
                            videoHeight = $videoDom.height();
                            // 新增统计数据
                            $('#video-cube').html(generateVideoCube(chartLegends, videoHeight));
                        }, 500);
                    });
                } else {
                    var progressBar = '<div class="progress">' +
                        '<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 45%">' +
                        '<span class="sr-only">45% Complete</span></div></div>';
                    $('#' + videoDomId).append('<div id=' + videoId + '> <p class="text-center">视频来自设备(编号：' + vSensorId + ')，上传中</p>' + progressBar + '</div>');
                }
            });
        }

        // 初始化事件列表
        var $eventTable = $('#event-table');
        $eventTable.bootstrapTable({
            data: eventData,
            columns: [{
                field: 'time',
                title: '时间',
                formatter: function (value) {
                    var c = value - minTime;
                    var hour = Math.floor(c/(60 * 60 * 1000));
                    c = c - hour * (60 * 60 * 1000);
                    var minutes = Math.floor(c/(60 * 1000));
                    c = c - minutes * (60 * 1000);
                    var seconds = Math.floor(c/1000);

                    if (minutes < 10) {
                        minutes = "0" + minutes;
                    }
                    if (seconds < 10) {
                        seconds = "0" + seconds;
                    }
                    return minutes + ":" + seconds;
                }
            }, {
                field: 'mark',
                title: '事件'
            }, {
                field: 'legend',
                title: '来源'
            }]
        });
        // 双击可切换查看相应数据
        $eventTable.on('click-row.bs.table', function (row, $element, field) {
            var tableTime = $element['time'];
            // 触发sliderChange事件，改变图表和视频内容
            for (var index in analysisObject.timeline) {
                var time = analysisObject.timeline[index];
                if (transferTime(tableTime).startsWith(time)) {
                    $(".slider").slider({values: [index, analysisObject.timeline.length - 1]}).slider("pips", "refresh").slider("float", "refresh");
                    break;
                }
            }
        });
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

/** 初始化图表内容 **/
function generateChartDom(legend, chartId) {
    var chartDom = chartId + "-dom";
    return '<div class="row in-row ' + chartDom + '" hidden="hidden">' +
        '<div class="col-sm-10 col-md-10 col-no-padding-both"><div id="' + chartId + '" style="margin-bottom: 5px"></div></div>' +
        '<div class="col-sm-2 col-md-2 col-little-padding-left" style="padding-top: 5px">' +
        '<div class="btn btn-default btn-block" onclick="clickHideCube(this)" style="height: 90px; white-space: pre-wrap"" key="' + chartId + '">' +
        generateDataCubeDom(legend) +
        '</div></div>';
}

/** 初始化图表数据方格 **/
function generateChartCube(chartLegends) {
    var html = "";
    Object.keys(chartLegends).forEach(function (chartId) {
        var legend = chartLegends[chartId];
        var cube = "cube-" + chartId;
        html += '<div class="col-sm-2 col-md-2 col-little-padding-both" id="' + cube + '">' +
            '<div class="btn btn-default btn-block" onclick="clickAddCube(this)" style="height: 90px; white-space: pre-wrap"" key="' + chartId + '">' +
            generateDataCubeDom(legend) +
            '</div>' +
            '</div>';
    });
    return html;
}

/** 初始化用户自定义图表数据 **/
function generateDefineChartDom(chartId, legend, title, desc) {
    var legend1 = legend.split("-")[0];
    var legend2 = legend.split("-")[1];
    var chartDom = chartId + "-dom";
    return '<div class="row in-row ' + chartDom + '" hidden="hidden">' +
        '<div class="row in-row">' +
        '<div class="col-sm-9 col-md-9 col-no-padding-both">' +
        '<div><span style="font-size: 18px; font-weight: 600; padding-left: 50px">' + title + '</span><span style="font-size: 16px; padding-left: 20px; padding-top: 3px">' + desc + '</span></div>' +
        '<div id="' + chartId + '" style="margin-bottom: 5px"></div>' +
        '</div>' +
        '<div class="col-sm-1 col-no-padding-both"><div class="text-right" style="font-weight: 600; font-size: 16px; margin-bottom: 55px; padding-right: 10px">横<br/>坐<br/>标</div><div class="text-right" style="font-weight: 600; font-size: 16px; padding-right: 10px">纵<br/>坐<br/>标</div></div>' +
        '<div class="col-sm-2 col-little-padding-left">' +
        '<div class="btn btn-default btn-block" onclick="clickHideCube(this)" style="height: 200px; white-space: pre-wrap"" key="' + chartId + '">' +
        generateDataCubeDom(legend1) +
        '<div style="margin-bottom: 35px"></div>' +
        generateDataCubeDom(legend2) +
        '</div></div></div>';
}

function generateDataCubeDom(legend) {
    var unit = "";
    if (commonObject.legendUnit.hasOwnProperty(legend)) {
        unit = commonObject.legendUnit[legend];
    }

    if (legend.indexOf("-") === -1) {
        return '<div style="font-size: 12px; font-weight: 600; padding-bottom: 5px; padding-top: 10px" class="text-center">' + legend + '</div>' +
            '<div class="text-center cube-text cube-' + legend + '" style="font-size: 16px; font-weight: 600; color: #35b5eb;">-</div>' +
            '<div class="text-center" style="font-size: 12px">' + unit + '</div>';
    } else {
        // user define cube
        var text = legend.split("-")[0] + "<br/><div style='font-weight: normal; margin: 5px 0;'>VS</div>" + legend.split("-")[1];
        return '<div style="font-size: 12px; font-weight: 600; padding-bottom: 5px; padding-top: 10px" class="text-center">' + text + '</div>';
    }

}

/** 点击cube时的动作 **/
function clickAddCube(btn) {
    var chartId = $(btn).attr("key");
    var chartDom = chartId + "-dom";

    // 隐藏自身
    $('#cube-' + chartId).attr("hidden", "hidden");

    // 显示chart
    $('.' + chartDom).attr("hidden", false);
    var chart = analysisObject.chart[chartId];
    chart.resize();

    // 将chart加入播放选择
    analysisObject.setSelectedChart(chartId, chart);

    // 隐藏通知
    $('#app-analysis-cube-alert').attr("hidden", "hidden");
}

function clickHideCube(btn) {
    var chartId = $(btn).attr("key");
    var chartDom = chartId + "-dom";

    // 隐藏chart
    $('.' + chartDom).attr("hidden", "hidden");

    // 显示到cube栏
    $('#cube-' + chartId).attr("hidden", false);

    // 将chart移除播放选择
    analysisObject.removeSelectedChart(chartId);

    // 显示通知
    if (isEmptyObject(analysisObject.selectedChart)) {
        $('#app-analysis-cube-alert').attr("hidden", false);
    }
}

/** 初始化视频内容 **/
function generateVideoDom(contentId) {
    return '<div class="row in-row">' +
        '<div id="' + contentId + '"></div>' +
        '</div>';
}

/** 初始化视频数据方格 **/
function generateVideoCube(chartLegends, videoHeight) {
    var html = '<div style="height: ' + videoHeight + 'px; overflow-x: hidden; overflow-y: auto;">';
    Object.keys(chartLegends).forEach(function(chartId) {
        var legend = chartLegends[chartId];
        var cube = "cube-" + chartId;

        var infoDom = '<div class="btn btn-default btn-block" onclick="clickAddCube(this)" style="height: 90px; white-space: pre-wrap" key="' + chartId + '">' +
            generateDataCubeDom(legend) +
            '</div>';
        html += '<div class="col-sm-6 col-little-padding-both" id="' + cube + '">' + infoDom + '</div>';
    });
    return html + '</div>';
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
