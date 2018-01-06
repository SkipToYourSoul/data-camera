/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/6
 *  Description:
 *      初始化实验页面
 */

function initExperiment(){
    // 判断加载状态
    if (appObject.iFe == false){
        console.log("Not the first time in experiment");
        return;
    } else {
        appObject.iFe = false;
    }

    // -- 初始化实验轨迹、传感器绑定信息
    for (var exp_id in experiments){
        if (!experiments.hasOwnProperty(exp_id)){
            continue;
        }
        var experiment = experiments[exp_id];
        // - 设置该实验的最新数据时间
        expObject.setNewTime(exp_id, new Date().getTime());

        // -- 遍历轨迹
        for (var i = 0; i < experiment['trackInfoList'].length; i++){
            var track = experiment['trackInfoList'][i];
            var track_id = track['id'];
            var track_type = track['type'];

            // init track chart
            var legend = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            if (track_type == 1) {
                // --- value sensor
                for (var j=0; j < legend.length; j++){
                    var chart_dom = "experiment-track-" + exp_id + "-" + track_id + "-" + legend[j];
                    var chart = echarts.init(document.getElementById(chart_dom), "", opts = {height: 150});
                    chart.setOption(experimentChartOption(legend[j]));
                }
            } else if (track_type == 2){

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
                for (var free in freeSensors){
                    if (freeSensors.hasOwnProperty(free) && track['type'] == freeSensors[free]['sensorConfig']['type']) {
                        source.push({
                            value: freeSensors[free].id,
                            text: freeSensors[free].name
                        });
                    }
                }
                for (var boundApp in boundSensors){
                    if (boundSensors.hasOwnProperty(boundApp)) {
                        for (var index in boundSensors[boundApp]){
                            if (boundSensors[boundApp].hasOwnProperty(index) && track['type'] == boundSensors[boundApp][index]['sensorConfig']['type']){
                                source.push({
                                    value: boundSensors[boundApp][index].id,
                                    text: boundSensors[boundApp][index].name + "(绑定于：" + apps[boundApp]['name'] + ")",
                                    disabled: true
                                });
                            }
                        }
                    }
                }
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
                success: function(result) {
                    window.location.href = current_address + "?id=" + app['id'];
                },
                error: function (error) {
                    message_info('绑定操作失败: ' + error, 'error');
                }
            });
        }
    }

    // -- 初始化实验监控和录制状态
    initActionStatus();
}

/**
 * 实验页面的图表
 * @param legend
 * @returns
 */
var experimentChartOption = function (legend) {
    return {
        backgroundColor: '#ffffff',
        tooltip: app_chart_tooltip,
        legend: {
            top: '0%',
            left: 'center',
            data: legend
        },
        grid: [{
            borderWidth: 0,
            top: 10,
            bottom: 30,
            left: 40,
            right: 25,
            textStyle: {
                color: "#fff"
            }
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
                splitLine: {
                    show: false
                },
                boundaryGap: true,
                minInterval: 1,
                splitNumber: 3
            }
        ],
        series: {
            name: legend,
            type: 'line',
            symbolSize: 4,
            symbol:'circle',
            hoverAnimation: false,
            smooth: true,
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
                data: []
            },
            data: []
        }
    };
};
