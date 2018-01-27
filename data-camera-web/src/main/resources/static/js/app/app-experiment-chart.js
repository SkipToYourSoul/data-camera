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
    Object.keys(experiments).forEach(function (exp_id) {
        var experiment = experiments[exp_id];
        // - 设置该实验的最新数据时间
        expObject.setNewTime(exp_id, new Date().getTime());

        // -- 遍历轨迹
        experiment['trackInfoList'].forEach(function (track, index) {
            var track_id = track['id'];
            var track_type = track['type'];

            // init track chart
            var legends = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            if (track_type == 1) {
                // --- value sensor
                legends.forEach(function (legend) {
                    var chart_dom = "experiment-track-" + exp_id + "-" + track_id + "-" + legend;
                    var chart = echarts.init(document.getElementById(chart_dom), "", opts = {height: 150});
                    chart.setOption(experimentChartOption(legend));
                });
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
