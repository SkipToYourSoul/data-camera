/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/23
 *  Description:
 *    生成实验片段对应的数据图表
 *    使用的后台数据(app, recorders)
 */

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
                initDom(chartData, videoData, recorder);
            } else if (response.code == "1111") {
                message_info("加载数据失败，失败原因为：" + response.data, 'error');
            }
        },
        error: function (response) {
            message_info("数据请求被拒绝", 'error');
        }
    });

    function initDom(chartData, videoData, recorder){
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
                    chart.setOption(buildAnalysisChartOption(chartData[sensorId][legend], legend));
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
        tooltip: app_chart_tooltip,
        grid: [{
            top: 10,
            bottom: 30,
            left: 30,
            right: 20
        }],
        calculable: true,
        xAxis: [
            {
                type: 'time',
                boundaryGap : ['20%', '20%'],
                axisPointer: {
                    show: true,
                    type: 'line',
                    snap: true,
                    z: 100
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                scale: true,
                splitArea: {
                    show: true
                },
                boundaryGap: true
            }
        ],
        series: [
            {
                name: legend,
                type: 'line',
                symbolSize: 5,
                symbol:'circle',
                hoverAnimation: false,
                data: data
            }
        ]
    }
}
