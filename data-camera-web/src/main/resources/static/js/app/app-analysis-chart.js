/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/23
 *  Description:
 *    生成实验片段对应的数据图表
 *    使用的后台数据(app, recorders)
 */

// -- 入口函数，初始化数据图表以及视频
function initRecorderContentDom(recorderId){
    console.info("Request recorder: " + recorderId);
    var recorder = findRecorderInfo(recorderId);
    if (recorder == null){
        console.log("Null data recorder");
        window.location.href = current_address + "?id=" + app['id'] + "&tab=2";
        return;
    }

    // 获取数据片段描述
    var $appAnalysisDesc = $('#app-analysis-desc');
    $appAnalysisDesc.editable('destroy');
    $appAnalysisDesc.html(recorder['description']);
    $appAnalysisDesc.editable({
        title: '输入描述',
        rows: 5,
        placeholder: '在这里输入描述',
        pk: recorderId,
        url: crud_address + '/recorder/desc',
        success: function(response) {
            if (response.code == "1111"){
                message_info('操作无效: ' + response.data, "error");
            } else if (response.code == "0000"){
                message_info("实验记录描述变更为：" + response.data, 'success');
                recorder['description'] = response.data;
            }
        },
        error: function (response) {
            message_info('数据请求被拒绝', 'error');
        }
    });
    // 异步请求实验片段数据
    $.ajax({
        type: 'get',
        url: data_addrss + "/get-recorder-data",
        data: {
            "recorder-id": recorderId
        },
        success: function (response) {
            if (response.code == "0000") {
                console.log("请求数据成功", 'success');
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
        // 片段没有任何数据
        if (isEmptyObject(chartData) && isEmptyObject(videoData)){
            $('.app-analysis-chart-dom').attr("hidden", true);
            $('.app-analysis-info-dom').attr("hidden", false);
            return;
        } else {
            $('.app-analysis-chart-dom').attr("hidden", false);
            $('.app-analysis-info-dom').attr("hidden", true);
        }

        // timeline
        analysisObject.timeline = generateTimeList(minTime, maxTime);
        analysisObject.timelineStart = 0; analysisObject.timelineEnd = analysisObject.timeline.length - 1;
        $(".slider").slider({
            range: true,
            min: 0,
            max: analysisObject.timeline.length - 1,
            values: [0, analysisObject.timeline.length - 1]
        }).slider("pips", {
            rest: "pip",
            labels: analysisObject.timeline
        }).slider("float", {
            labels: analysisObject.timeline
        }).on("slidechange", function(e,ui) {
            if (recorderInterval != null){
                // 正在回放数据，不进行高亮片段更新

            } else {
                analysisObject.timelineStart = ui.values[0];
                analysisObject.timelineEnd = ui.values[1];
                var mark = [[{
                    xAxis: analysisObject.timeline[analysisObject.timelineStart]
                }, {
                    xAxis: analysisObject.timeline[analysisObject.timelineEnd]
                }]];
                for (var i in analysisObject.chart){
                    var series = analysisObject.chart[i].getOption()['series'];
                    series[0]['markArea']['data'] = mark;
                    analysisObject.chart[i].setOption({
                        series: series
                    });
                }
            }
        });
        
        // chart
        var $dom = $('#app-analysis-chart');
        $dom.empty();
        for (var sensorId in chartData){
            if (!chartData.hasOwnProperty(sensorId)){
                continue;
            }
            for (var legend in chartData[sensorId]){
                if (!chartData[sensorId].hasOwnProperty(legend)){
                    continue;
                }
                var chartId = "chart-" + recorderId + '-' + legend;
                var title = "[设备" + legend + "]";
                $dom.append(generate(legend, title, chartId));
                if (echarts.getInstanceByDom(document.getElementById(chartId)) == null){
                    var chart = echarts.init(document.getElementById(chartId), "", opts = {
                        height: 100
                    });
                    chart.setOption(buildAnalysisChartOption(chartData[sensorId][legend], legend));

                    analysisObject.setChart(chartId, chart);
                    analysisObject.setChartData(chartId, chartData[sensorId][legend]);
                }
            }
        }
        
        // video
        var $dom2 = $('#app-analysis-video');
        // -- clear dom content
        for (var domId in analysisObject.video){
            videojs(domId).dispose();
            console.log("Dispose video " + domId);
            delete analysisObject.video[domId];
        }
        $dom2.empty();
        var vCount = 0;
        // -- generate new content
        for (var vSensorId in videoData){
            if (!videoData.hasOwnProperty(vSensorId)){
                continue;
            }
            var videoOption = videoData[vSensorId]['option'];
            var videoId = 'video-' + vSensorId;
            var videoDomId = 'video-dom-' + vSensorId;
            $dom2.append(generate(videoDomId + '-' + (vCount++), "video from sensor " + vSensorId, videoDomId));

            if (videoOption['sources'] != null){
                $('#' + videoDomId).append('<video id="' + videoId + '"class="video-js vjs-fluid vjs-big-play-centered" data-setup="{}"></video>');
                videojs(videoId, videoOption, function () {
                    videojs.log('The video player ' + videoId + ' is ready');
                    analysisObject.setVideo(videoId, this);
                });

            } else {
                var progressBar = '<div class="progress">' +
                    '<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 45%">' +
                    '<span class="sr-only">45% Complete</span></div></div>';
                $('#' + videoDomId).append('<div id=' + videoId + '> <p class="text-center">视频来自设备(编号：' + vSensorId + ')，上传中</p>' + progressBar + '</div>');
            }
        }

        function generate(panelId, title, contentId) {
            return '<div class="panel panel-default my-panel">' +
                '<div class="my-panel-heading">' +
                '<div class="panel-title">' +
                '<a role="button" data-toggle="collapse" href="#' + panelId + '" aria-expanded="true"><i class="fa fa-arrows-v"></i>&nbsp; ' + title + '</a>' +
                '</div></div>' +
                '<div id="' + panelId + '" class="panel-collapse collapse in" role="tabpanel">' +
                '<div class="panel-body my-panel-body"><div id="' + contentId + '"></div></div>' +
                '</div></div>';
        }

        function generateTimeList(minTime, maxTime) {
            var list = [];
            for(var index = Math.floor(minTime/1000); index <= Math.ceil(maxTime/1000); index += 1){
                list.push(new Date(index*1000).Format("yyyy-MM-dd HH:mm:ss"));
            }
            return list;
        }
    }

    /**
     * 找出当前需要展示的recorder
     * @param recorderId
     */
    function findRecorderInfo(recorderId){
        if (!recorders.hasOwnProperty(app['id'])){
            return;
        }
        for (var index=0; index<recorders[app['id']].length; index++){
            if (recorders[app['id']][index]['id'] == recorderId){
                return recorders[app['id']][index];
            }
        }
    }
}

function buildAnalysisChartOption(data, legend) {
    return {
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(245, 245, 245, 0.8)',
            borderWidth: 1,
            borderColor: '#ccc',
            padding: 10,
            formatter: function (params) {
                params = params[0];
                var html = "<b>TIME: </b>" + params.value[0] + "<br/>";
                if (params.value.length > 1){
                    html += "<b>VALUE: </b>" + params.value[1] + "<br/>";
                }
                if (params.name !== null){
                    html += "<b>MARK: </b>"+ params.name;
                }
                return html;
            },
            textStyle: {
                color: '#000'
            }
        },
        grid: [{
            top: 5,
            bottom: 5,
            left: 40,
            right: 20
        }],
        calculable: true,
        xAxis: [
            {
                type: 'time',
                splitLine: {
                    show: true
                },
                axisLabel: {
                    show: false
                },
                axisTick: {
                    show: false
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                scale: true,
                boundaryGap: false,
                splitLine: {
                    show: false
                },
                minInterval: 1
            }
        ],
        series: [
            {
                name: legend,
                type: 'line',
                symbolSize: 5,
                symbol:'circle',
                hoverAnimation: false,
                itemStyle: {
                    normal: {
                        color: 'rgb(255, 70, 131)'
                    }
                },
                smooth: true,
                sampling: true,
                areaStyle: {
                    normal: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                            offset: 0,
                            color: 'rgb(255, 158, 68)'
                        }, {
                            offset: 1,
                            color: 'rgb(255, 70, 131)'
                        }])
                    }
                },
                markArea: {
                    silent: true,
                    itemStyle: {
                        normal: {
                            color: 'rgb(245, 245, 245)'
                        }
                    },
                    data: [[{
                        xAxis: analysisObject.timeline[0]
                    },{
                        xAxis: analysisObject.timeline[analysisObject.timeline.length - 1]
                    }]]
                },
                data: data
            }
        ]
    }
}
