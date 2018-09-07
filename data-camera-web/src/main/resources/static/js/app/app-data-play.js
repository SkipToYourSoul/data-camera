/*
* app-data-play.js
* 包含图表轮询相关数据操作
* */

/**
 * chart数据回放入口
 * @param dataStartTime 当前数据起始时间
 * @param interval 数据刷新间隔
 * @param speed 当前回放倍率
 * @return {number}
 */
function chartDataPlay(dataStartTime, interval, speed) {
    console.info("Chart data recorder begin at: ", dataStartTime);
    var timeMS = transferTime(dataStartTime);
    return setInterval(doChartInterval, interval);

    function doChartInterval() {
        var isEnd = false;
        timeMS += interval * speed;
        if (timeMS > transferTime(analysisObject.timeline[analysisObject.timelineEnd])) {
            timeMS = transferTime(analysisObject.timeline[analysisObject.timelineEnd]);
            isEnd = true;
        }
        var line = [{
            xAxis: transferTime(timeMS)
        }];

        // 遍历当前所有的chart
        Object.keys(analysisObject.chart).forEach(function (index) {
            // chart的原始数据
            var chartOriginData = analysisObject.getChartData()[index];

            // chart的当前展示数据
            var chartCurrentSeries = analysisObject.chart[index].getOption()['series'];

            chartCurrentSeries[0]['data'] = updateChartData(chartOriginData);
            chartCurrentSeries[0]['markLine']['data'] = line;
            chartCurrentSeries[0]['markArea']['data'] = [];
            analysisObject.chart[index].setOption({
                series: chartCurrentSeries
            });
        });

        // 数据已轮询完毕，停止轮询
        if (isEnd) {
            clearInterval(recorderInterval);
            recorderInterval = null;
            recorderComplete();
        }
    }

    function updateChartData(d) {
        var n = [];

        // 返回时间范围内的数据
        for (var index = 0; index < d.length; index ++) {
            var time = transferTime(d[index]['value'][0]);
            if (time >= timeMS){
                var lastPoint = d[d.length - 1];
                n = d.slice(0, index);
                n.push({
                    value: [lastPoint['value'][0]]
                });
                break;
            }
        }
        return n;
    }
}

/**
 * 时间轴回放入口
 * @param interval 数据刷新间隔，固定1000
 * @param speed 当前回放倍率
 * @return {number}
 */
function timeLinePlay(interval, speed) {
    console.info("TimeLine play begin at: ", new Date().getTime());
    interval = 1000;
    return setInterval(doTimeLineInterval, interval/speed);

    function doTimeLineInterval() {
        console.info("TimeLine update at: ", new Date().getTime());
        // 更新时间轴
        $(".slider")
            .slider({values: [++analysisObject.timelineStart, analysisObject.timelineEnd]})
            .slider("pips", "refresh")
            .slider("float", "refresh");
        // 显示时间标记
        /*$("#timeline-slider").find(".ui-slider-tip").css("visibility", "visible");*/

        // 数据已轮询完毕，停止轮询
        if (analysisObject.timelineStart >= analysisObject.timelineEnd){
            clearInterval(timeLineInterval);
            timeLineInterval = null;
            recorderComplete();
        }
    }
}