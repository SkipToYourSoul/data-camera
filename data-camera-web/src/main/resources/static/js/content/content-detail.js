/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/19
 *  Description:
 */

initContentDetailDom();

function initContentDetailDom(){
    var recorderId = currentContent['recorderInfo']['id'];
    // 异步请求实验片段数据
    $.ajax({
        type: 'get',
        url: data_address + "/get-recorder-data",
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
        // timeline
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
            $('#recorder-current-time').html(analysisObject.secondLine[ui.values[0]]);
            $('#recorder-total-time').html(analysisObject.secondLine[ui.values[1]]);
            if (recorderInterval != null){
                // 正在回放数据，不进行高亮片段更新，进行标记线更新
                var line = [{
                    xAxis: analysisObject.timeline[ui.values[0]]
                }];
                Object.keys(analysisObject.chart).forEach(function (i) {
                    var series = analysisObject.chart[i].getOption()['series'];
                    series[0]['markLine']['data'] = line;
                    analysisObject.chart[i].setOption({
                        series: series
                    });
                });
            } else {
                // 正常状态，进行标记区域更新
                analysisObject.timelineStart = ui.values[0];
                analysisObject.timelineEnd = ui.values[1];
                var mark = [[{
                    xAxis: analysisObject.timeline[analysisObject.timelineStart]
                }, {
                    xAxis: analysisObject.timeline[analysisObject.timelineEnd]
                }]];
                Object.keys(analysisObject.chart).forEach(function (i) {
                    var series = analysisObject.chart[i].getOption()['series'];
                    series[0]['markArea']['data'] = mark;
                    analysisObject.chart[i].setOption({
                        series: series
                    });
                });
            }
        });

        // chart
        var $dom = $('#app-analysis-chart');
        $dom.empty();
        Object.keys(chartData).forEach(function (sensorId) {
            var data = chartData[sensorId];
            var chartWidth = 0;
            Object.keys(data).forEach(function (legend) {
                var chartId = "chart-" + recorderId + '-' + sensorId + '-' + legend;
                $dom.append(generate(sensorId +'-'+new Date().getTime(), legend, chartId));
                chartWidth = $('#' + chartId).width();
            });
            Object.keys(data).forEach(function (legend) {
                var chartId = "chart-" + recorderId + '-' + sensorId + '-' + legend;
                if (echarts.getInstanceByDom(document.getElementById(chartId)) == null){
                    var chart = echarts.init(document.getElementById(chartId), "", opts = {
                        height: 100,
                        width: chartWidth
                    });
                    chart.setOption(buildAnalysisChartOption(chartData[sensorId][legend], legend));

                    analysisObject.setChart(chartId, chart);
                    analysisObject.setChartData(chartId, chartData[sensorId][legend]);
                }
            });
        });
    }
}