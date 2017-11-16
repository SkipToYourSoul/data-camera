/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/11
 *  Description:
 */

function initTrackOfExperiments() {
    // message_info("Init tracks", "info", 3);
    for (var index in experiments){
        var experiment = experiments[index];
        var exp_id = experiment['id'];
        for (var i in experiment['trackInfoList']){
            var track = experiment['trackInfoList'][i];
            var track_id = track['id'];

            // init track chart
            var legend = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            var chart_dom = "experiment-track-" + exp_id + "-" + track_id;
            var chart = echarts.init(document.getElementById(chart_dom), "", opts = {});
            chart.setOption(experimentChartOption(legend));

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
                    if (isExperimentMonitor[exp_id] == 1){
                        return '数据监控中，不能进行绑定操作';
                    }
                },
                url: crud_address + '/bound/toggle',
                success: function(result) {
                    // refresh editable
                    if (result['action'] == 'unbound'){

                    } else if (result['action'] == 'bound'){

                    }
                    window.location.href = current_address + "?id=" + app['id'];
                },
                error: function (error) {
                    message_info('error' + error, 'info');
                }
            });
        }
    }
}

var exp_monitor_interval = {};
var exp_newest_timestamp = {
    0: new Date().getTime(),
    1: new Date().getTime()
};
function expMonitor(button){
    var exp_id = button.getAttribute('data');
    var exp_state_dom = $('#experiment-state-' + exp_id);
    var exp_monitor_btn = $('#experiment-monitor-' + exp_id);

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

    function askForData(exp_id) {
        message_info('实验' + exp_id + '运行中', 'info', 1);
        var exp_bound_sensors = boundSensors[exp_id];
        var exp_bound_sensor_ids = [];
        $.get(data_addrss + "/monitor", {
            "exp-id": exp_id,
            "timestamp": /*exp_newest_timestamp[exp_id]*/1510838700518
        }, function (response) {
            message_info(response, 'info', 3);
        });
    }
}