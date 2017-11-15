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
                    source.push({
                        value: freeSensors[index].id,
                        text: freeSensors[index].name
                    });
                }
            }
            $track_bound_dom.editable({
                prepend: '不绑定设备',
                source: source,
                value: value,
                sourceError: 'error loading data',
                pk: track_id,
                validate: function (value) {
                    if (0 == 1){
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

function initTrackSensor(){

}