/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/11
 *  Description:
 */

var chart_object = {};
var exp_monitor_interval = {};
var exp_newest_timestamp = {
    1: 1511150400000
};

function initTrackOfExperiments() {
    // message_info("Init tracks", "info", 3);
    for (var index in experiments){
        var experiment = experiments[index];
        var exp_id = experiment['id'];
        exp_newest_timestamp[exp_id] = new Date().getTime();

        for (var i in experiment['trackInfoList']){
            var track = experiment['trackInfoList'][i];
            var track_id = track['id'];

            // init track chart
            var legend = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            var chart_dom = "experiment-track-" + exp_id + "-" + track_id;
            var chart = echarts.init(document.getElementById(chart_dom), "", opts = {});
            chart.setOption(experimentChartOption(legend));
            chart_object[chart_dom] = chart;

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
                for (var index in freeSensors){
                    if (track['type'] == freeSensors[index]['sensorConfig']['type']) {
                        source.push({
                            value: freeSensors[index].id,
                            text: freeSensors[index].name
                        });
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
                    for (var index in tracks){
                        if (tracks[index]['id'] + '' != track_id){
                            continue;
                        }
                        var e = tracks[index]['experiment']['id'];
                        if (isExperimentMonitor[e] == 1){
                            return '数据监控中，不能进行绑定操作';
                        }
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

    // click monitor button
    for (var exp_id in isExperimentMonitor){
        var exp_monitor_btn = $('#experiment-monitor-' + exp_id);
        if (isExperimentMonitor[exp_id] == 1){
            isExperimentMonitor[exp_id] = 0;
            exp_monitor_btn.click();
        }
    }
}

function expMonitor(button){
    var exp_id = button.getAttribute('data');
    var exp_state_dom = $('#experiment-state-' + exp_id);
    var exp_monitor_btn = $('#experiment-monitor-' + exp_id);

    $.ajax({
        type: 'get',
        url: crud_address + "/monitor",
        data: {
            "exp-id": exp_id,
            "action": isExperimentMonitor[exp_id]
        },
        success: function (response) {
            if (response == -1){
                message_info('操作无效', 'error');
                return;
            }
            if (isExperimentMonitor[exp_id] == 0){
                // start monitor
                isExperimentMonitor[exp_id] = 1;
                exp_state_dom.removeClass('label-warning').addClass('label-success').text('正在监控');
                exp_monitor_btn.html('停止监控');

                exp_monitor_interval[exp_id] = setInterval(function () {
                    askForData(exp_id);
                }, 2000);
            } else if (isExperimentMonitor[exp_id] == 1){
                // stop monitor
                isExperimentMonitor[exp_id] = 0;
                exp_state_dom.removeClass('label-success').addClass('label-warning').text('非监控');
                exp_monitor_btn.html('开始监控');

                clearInterval(exp_monitor_interval[exp_id]);
                delete exp_monitor_interval[exp_id];
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });

    function askForData(exp_id) {
        var exp_bound_sensors = boundSensors[exp_id];
        $.get(data_addrss + "/monitor", {
            "exp-id": exp_id,
            "timestamp": exp_newest_timestamp[exp_id]
        }, function (response) {
            // --- traverse the sensors of this experiment
            for (var index in exp_bound_sensors){
                var sensor = exp_bound_sensors[index];
                if (!response.hasOwnProperty(sensor['id'])){
                    continue;
                }
                // --- init
                var sensor_id = sensor['id'];
                var track_id = sensor['trackId'];
                var chart_dom = "experiment-track-" + exp_id + "-" + track_id;
                var chart = chart_object[chart_dom];
                var series = chart.getOption()['series'];

                // --- update series data
                var legend = sensor['sensorConfig']['dimension'].split(';');
                for (var i in legend){
                    var key = legend[i];
                    if (!response[sensor_id].hasOwnProperty(key)){
                        continue;
                    }
                    var new_data = response[sensor_id][key];
                    series[i]['data'].push.apply( series[i]['data'], new_data );

                    var new_time = Date.parse(new_data[new_data.length - 1]['value'][0]);
                    if (new_time > exp_newest_timestamp[exp_id]){
                        exp_newest_timestamp[exp_id] = new_time;
                    }
                }

                // -- set new option
                chart_object[chart_dom].setOption({
                    series: series
                });
            }
        });
    }
}

function expRecorder(button) {
    var exp_id = button.getAttribute('data');
    var exp_monitor_btn = $('#experiment-recorder-' + exp_id);
    
    if (!isExperimentMonitor.hasOwnProperty(exp_id) || isExperimentMonitor['exp_id'] == 0){
        message_info("实验" + exp_id + "未开始监控，无法记录！");
        return;
    }

    $.ajax({
        type: 'get',
        url: crud_address + "/recorder",
        data: {
            "exp-id": exp_id,
            "action": isExperimentRecorder[exp_id]
        },
        success: function (response) {

        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}