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

        // 遍历当前所有的chart, index = chartId
        Object.keys(analysisObject.chart).forEach(function (index) {
            // chart的原始数据
            var chartOriginData = analysisObject.getChartData()[index];

            // chart的当前展示数据
            var chartCurrentSeries = analysisObject.chart[index].getOption()['series'];

            // legend
            var legend = chartCurrentSeries[0]['name'];

            // 用户自定义的图表回放
            if (index.startsWith("define")) {
                var nd = updateDefineChartData(chartOriginData);
                chartCurrentSeries[0]['data'] = nd;

                // 更新数据cube
                var length = nd.length;
                if (length > 0) {
                    var legend1 = legend.split("-")[0];
                    var legend2 = legend.split("-")[1];
                    $('.cube-' + legend).html(nd[length - 1][0].toFixed(2) + "-" + nd[length - 1][1].toFixed(2));
                    $('.cube-' + legend1).html(nd[length - 1][0].toFixed(2));
                    $('.cube-' + legend2).html(nd[length - 1][1].toFixed(2));
                }
            } else {
                chartCurrentSeries[0]['data'] = updateChartData(chartOriginData);
                chartCurrentSeries[0]['markLine']['data'] = line;
                // chartCurrentSeries[0]['markArea']['data'] = [];

                // 更新数据cube
                var dLength = chartCurrentSeries[0]['data'].length;
                if (dLength > 2) {
                    $('.cube-' + legend).html(chartCurrentSeries[0]['data'][dLength - 2]['value'][1].toFixed(2));
                }
            }

            if (analysisObject.selectedChart.hasOwnProperty(index)) {
                analysisObject.chart[index].setOption({
                    series: chartCurrentSeries
                });
            }
        });

        // 数据已轮询完毕，停止轮询
        if (isEnd) {
            clearInterval(recorderInterval);
            recorderInterval = null;
            recorderComplete();
        }
    }

    /** 找出当前回放时间点的图表数据 **/
    function updateChartData(d) {
        var position = findPosition(timeMS, d);
        var n = d.slice(0, position);
        if (n.length < d.length) {
            n.push({
                value: [d[d.length - 1]['value'][0]]
            });
        }
        return n;

        function findPosition(i, arr) {
            var left = 0;
            var right = arr.length - 1;

            if (i < transferTime(arr[left]['value'][0])) {
                return 0;
            }
            if (i >= transferTime(arr[right]['value'][0])) {
                return arr.length;
            }

            while (left <= right) {
                var mid = Math.floor((left + right)/2);
                if (i >= transferTime(arr[mid]['value'][0]) && i < transferTime(arr[mid+1]['value'][0])) {
                    return mid + 1;
                } else if (i < transferTime(arr[mid]['value'][0])) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
        }
    }

    function updateDefineChartData(d) {
        var time = d['time'];
        var data = d['data'];

        var position = findPosition(timeMS, time);
        return data.slice(0, position);

        function findPosition(i, arr) {
            var left = 0;
            var right = arr.length - 1;

            if (i < transferTime(arr[left])) {
                return 0;
            }
            if (i >= transferTime(arr[right])) {
                return arr.length;
            }

            while (left <= right) {
                var mid = Math.floor((left + right)/2);
                if (i >= transferTime(arr[mid]) && i < transferTime(arr[mid + 1])) {
                    return mid + 1;
                } else if (i < transferTime(arr[mid])) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
        }
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