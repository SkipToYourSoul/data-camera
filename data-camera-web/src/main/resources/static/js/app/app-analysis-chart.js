/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/23
 *  Description:
 *    生成实验片段对应的数据图表
 *    使用的后台数据(app, recorders)
 */

// -- 当前显示的所有echarts对象
var analysisCharts = [];

// -- 入口函数，初始化数据图表
function initChartDom(recorderId){
    console.info("Request recorder: " + recorderId);
    var recorder = findRecorderInfo(recorderId);
    if (recorder == null){
        console.log("Null data recorder");
        return;
    }
    // 异步请求实验片段数据
    $.ajax({
        type: 'get',
        url: data_addrss + "/get-recorder-data",
        data: {
            "recorder-id": recorderId
        },
        success: function (response) {
            if (response.code == "0000") {
                message_info("请求数据成功", 'success');
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
        // timeline
        var timeline = generateTimeList(minTime, maxTime);
        $(".slider").slider({
            range: true,
            min: 0,
            max: timeline.length - 1,
            values: [0, timeline.length - 1]
        }).slider("pips", {
            rest: "pip",
            labels: timeline
        }).slider("float", {
            labels: timeline
        }).on("slidechange", function(e,ui) {
            var start = timeline[ui.values[0]];
            var end = timeline[ui.values[1]];
            var mark = [[{
                xAxis: start
            }, {
                xAxis: end
            }]];
            for (var i=0; i<analysisCharts.length; i++){
                var series = analysisCharts[i].getOption()['series'];
                series[0]['markArea']['data'] = mark;
                analysisCharts[i].setOption({
                    series: series
                });
            }
        });

        var $dom = $('#app-analysis-chart-dom');
        $dom.empty();
        // chart
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
                    chart.setOption(buildAnalysisChartOption(chartData[sensorId][legend], legend, timeline));
                    analysisCharts.push(chart);
                }
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
            for(var index = minTime; index <= maxTime; index += 1000){
                list.push(new Date(index).Format("yyyy-MM-dd HH:mm:ss"));
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

function buildAnalysisChartOption(data, legend, timeline) {
    return {
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(245, 245, 245, 0.8)',
            borderWidth: 1,
            borderColor: '#ccc',
            padding: 10,
            textStyle: {
                color: '#000'
            }
        },
        grid: [{
            top: 5,
            bottom: 5,
            left: 40,
            right: 10
        }],
        calculable: true,
        xAxis: [
            {
                type: 'time',
                splitLine: {
                    show: false
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
                showSymbol: false,
                hoverAnimation: false,
                itemStyle: {
                    normal: {
                        color: '#9e9e9e'
                    }
                },
                areaStyle: {
                    normal: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                            offset: 0,
                            color: '#9d9d9d'
                        }, {
                            offset: 1,
                            color: '#9e9e9e'
                        }])
                    }
                },
                smooth: true,
                sampling: true,
                markArea: {
                    silent: true,
                    data: [[{
                        xAxis: timeline[0]
                    },{
                        xAxis: timeline[timeline.length - 1]
                    }]]
                },
                data: data
            }
        ]
    }
}
