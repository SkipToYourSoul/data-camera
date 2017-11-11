/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/11
 *  Description:
 */

function init_track() {
    // message_info("Init tracks", "info", 3);
    for (var index in experiments){
        var experiment = experiments[index];
        var exp_id = experiment['id'];
        for (var i in experiment['trackInfoList']){
            var track = experiment['trackInfoList'][i];
            var track_id = track['id'];
            var legend = (null == track['sensor'])?[]:track['sensor']['sensorConfig']['dimension'].split(';');

            var chart_dom = "experiment-track-" + exp_id + "-" + track_id;
            var chart = echarts.init(document.getElementById(chart_dom), "", opts = {
            });
            chart.setOption(experimentChartOption(legend));
        }
    }
}