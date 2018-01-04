/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/11
 *  Description:
 */

/**
 * key: exp_id
 * value: exp_interval
 * @type {{}}
 */
var exp_monitor_interval = {};
/**
 * key: exp_id
 * value: timestamp
 * @type {{}}
 */
var exp_newest_timestamp = {};
/**
 * key: exp_id
 * value: 2017-08-17T18:16:31.000+08:00
 * @type {{}}
 */
var recorder_timestamp = {};

/**
 * 初始化实验页面入口
 * 1、初始化实验轨迹、传感器绑定信息
 * 2、初始化实验状态（监控或录制）
 */
function initResourceOfExperimentPage() {
    // init experiment track and sensors
    // -- 初始化实验轨迹、传感器绑定信息
    for (var exp_id in experiments){
        if (!experiments.hasOwnProperty(exp_id)){
            continue;
        }
        var experiment = experiments[exp_id];
        exp_newest_timestamp[exp_id] = new Date().getTime();

        // -- 遍历轨迹
        for (var i = 0; i < experiment['trackInfoList'].length; i++){
            var track = experiment['trackInfoList'][i];
            var track_id = track['id'];
            var track_type = track['type'];

            // init track chart
            var legend = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            var chart_dom = "experiment-track-" + exp_id + "-" + track_id;
            if (track_type == 1) {
                // --- value sensor
                var chart = echarts.init(document.getElementById(chart_dom), "", opts = {height: 200});
                chart.setOption(experimentChartOption(legend));
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
                for (var index in freeSensors){
                    if (freeSensors.hasOwnProperty(index) && track['type'] == freeSensors[index]['sensorConfig']['type']) {
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

    // init experiment which is in monitoring state
    // -- 更改实验状态（如果在监控或录制状态）
    for (var id in isExperimentMonitor){
        if (!isExperimentMonitor.hasOwnProperty(id) || !isExperimentRecorder.hasOwnProperty(id)){
            continue;
        }

        var exp_monitor_btn = $('#experiment-monitor-' + id);
        var exp_monitor_dom = $('#experiment-es-' + id);
        var exp_recorder_btn = $('#experiment-recorder-' + id);
        var exp_recorder_dom = $('#experiment-rs-' + id);

        if (isExperimentMonitor[id] == 1){
            exp_monitor_dom.removeClass('label-default').addClass('label-success').text('正在监控');
            exp_monitor_btn.html('<i class="fa fa-eye"></i>&nbsp;停止监控');

            if (isExperimentRecorder[id] == 1){
                exp_recorder_dom.removeClass('label-default').addClass('label-success').text('正在录制');
                exp_recorder_btn.html('<i class="fa fa-camera-retro"></i>&nbsp;停止录制');

                recorder_timestamp[id] = [];
                recorder_timestamp[id].push(expRecorderTime[id]);

                exp_newest_timestamp[id] = new Date(parseTime(expRecorderTime[id])).getTime();
            }
            doInterval(id);
        }
    }
}

/**
 * 实验监控按钮点击触发
 * @param button
 */
function expMonitor(button){
    var exp_id = button.getAttribute('data');
    var exp_state_dom = $('#experiment-es-' + exp_id);
    var exp_monitor_btn = $('#experiment-monitor-' + exp_id);

    if (!boundSensors.hasOwnProperty(exp_id)){
        message_info('实验未绑定任何设备，不能进行监控', 'error');
        return;
    }

    $.ajax({
        type: 'get',
        url: crud_address + "/monitor",
        data: {
            "exp-id": exp_id,
            "app-id": app['id']
        },
        success: function (response) {
            if (response.code == "1111"){
                message_info('操作无效: ' + response.data, "error");
                return;
            }
            var action = response.data;
            if (action == "1"){
                // start monitor
                isExperimentMonitor[exp_id] = 1;
                exp_state_dom.removeClass('label-default').addClass('label-success').text('正在监控');
                exp_monitor_btn.html('<i class="fa fa-eye"></i>&nbsp;停止监控');

                doInterval(exp_id);
            } else if (action == "0"){
                // stop monitor
                pageStopMonitor(exp_id);
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}

/**
 * 实验录制按钮点击触发
 * @param button
 */
function expRecorder(button) {
    var exp_id = button.getAttribute('data');
    var exp_state_dom = $('#experiment-rs-' + exp_id);
    var exp_recorder_btn = $('#experiment-recorder-' + exp_id);
    
    if (!isExperimentMonitor.hasOwnProperty(exp_id) || isExperimentMonitor[exp_id] == 0){
        message_info("实验" + exp_id + "未开始监控，无法进行录制！");
        return;
    }

    // -- get recorder status
    $.ajax({
        type: 'get',
        url: crud_address + "/isRecord",
        data: {
            "exp-id": exp_id
        },
        success: function (response) {
            if (response.code == "1111"){
                message_info('操作无效: ' + response.data, "error");
                return;
            }
            var recorder_status = response.data;
            var time = new Date().getTime();
            if (recorder_status == "1"){
                // -- end recorder, confirm to save the data
                bootbox.dialog({
                    title: "保存录制数据片段?",
                    message: '<div class="row"> <div class="form-group" style="margin-bottom: 5px"><label class="col-sm-2 control-label">片段名</label>' +
                    '<div class="col-sm-10"><input type="text" class="form-control" id="dialog-data-name" placeholder="请输入片段标题"/></div></div>' +
                    '<div class="form-group"><label class="col-sm-2 control-label">片段描述</label>' +
                    '<div class="col-sm-10"><input type="text" class="form-control" id="dialog-data-desc" placeholder="请输入片段描述"></div></div>' +
                    '</div>',
                    async: false,
                    buttons: {
                        cancel: {
                            label: '<i class="fa fa-times"></i> 取消',
                            className: 'btn-danger',
                            callback: function(){
                                submitToServer(0, time);
                            }
                        },
                        confirm: {
                            label: '<i class="fa fa-check"></i> 保存',
                            className: 'btn-success',
                            callback: function(){
                                submitToServer(1, time);
                            }
                        }
                    }
                });
            } else if (recorder_status == "0"){
                // -- begin recorder
                submitToServer(0, time);
            }
        },
        error: function (response) {
            message_info("请求数据失败", 'error');
        }
    });

    function submitToServer(is_save_recorder, time){
        // --- change recorder status on server
        $.ajax({
            type: 'get',
            url: crud_address + "/record",
            data: {
                "exp-id": exp_id,
                "app-id": app['id'],
                "is-save": is_save_recorder,
                "data-name": $('#dialog-data-name').val(),
                "data-desc": $('#dialog-data-desc').val(),
                "data-time": time
            },
            success: function (response) {
                if (response.code == "1111"){
                    message_info('操作无效: ' + response.data, "error");
                    return;
                }

                if (!recorder_timestamp.hasOwnProperty(exp_id)){
                    recorder_timestamp[exp_id] = [];
                }

                var action = response.data;
                if (action == "-10"){
                    message_info("实验" + exp_id + "未绑定传感器，不能记录");
                } else if (action == "-1"){
                    // start recorder
                    message_info("实验" + exp_id + ": 开始记录");
                    exp_state_dom.removeClass('label-default').addClass('label-success').text('正在录制');
                    exp_recorder_btn.html('<i class="fa fa-camera-retro"></i>&nbsp;停止录制');
                    isExperimentRecorder[exp_id] = 1;
                    if (recorder_timestamp[exp_id].length % 2 == 0){
                        recorder_timestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
                    } else {
                        recorder_timestamp[exp_id].pop();
                        recorder_timestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
                    }
                } else if (action == "0"){
                    message_info("实验" + exp_id + ": 停止记录");
                    pageStopRecorder(exp_id);
                } else {
                    window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + action;
                }
            },
            error: function (response) {
                message_info("请求数据失败", 'error');
            }
        });
    }
}

/**
 * 结束监控时页面的更改
 * @param exp_id
 */
function pageStopMonitor(exp_id){
    if (isExperimentRecorder[exp_id] == 1){
        pageStopRecorder(exp_id);
    }

    isExperimentMonitor[exp_id] = 0;
    $('#experiment-es-' + exp_id).removeClass('label-success').addClass('label-default').text('非监控');
    $('#experiment-monitor-' + exp_id).html('<i class="fa fa-eye"></i>&nbsp;开始监控');

    clearInterval(exp_monitor_interval[exp_id]);
    delete exp_monitor_interval[exp_id];

    $('.app-exp-track-statistics .content-value').html('-');
}

/**
 * 结束录制时页面的更改
 * @param exp_id
 */
function pageStopRecorder(exp_id){
    $('#experiment-rs-' + exp_id).removeClass('label-success').addClass('label-default').text('非录制');
    $('#experiment-recorder-' + exp_id).html('<i class="fa fa-camera-retro"></i>&nbsp;开始录制');
    isExperimentRecorder[exp_id] = 0;
    if (recorder_timestamp[exp_id].length % 2 == 1) {
        recorder_timestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
    }
}

/**
 * 监控时定期更新图表数据
 * @param exp_id
 */
function doInterval(exp_id){
    exp_monitor_interval[exp_id] = setInterval(function(){
        askForData(exp_id);
    }, 2000);

    function askForData(exp_id) {
        var exp_bound_sensors = boundSensors[exp_id];
        $.get(data_addrss + "/monitor", {
            "exp-id": exp_id,
            "timestamp": exp_newest_timestamp[exp_id]
        }, function (response) {
            // --- traverse the sensors of this experiment
            for (var index in exp_bound_sensors){
                var sensor = exp_bound_sensors[index];
                var sensor_type = exp_bound_sensors[index]['sensorConfig']['type'];
                var sensor_id = sensor['id'];
                var track_id = sensor['trackId'];
                var chart_dom = "experiment-track-" + exp_id + "-" + track_id;

                if (sensor_type == 1){
                    if (!response.hasOwnProperty(sensor_id) || echarts.getInstanceByDom(document.getElementById(chart_dom)) == null){
                        continue;
                    }
                    // --- init
                    var chart = echarts.getInstanceByDom(document.getElementById(chart_dom));
                    var series = chart.getOption()['series'];

                    // --- update series data
                    var legend = sensor['sensorConfig']['dimension'].split(';');
                    var unit = sensor['sensorConfig']['unit'].split(';');
                    var statistics_info = {
                        "max": "-",
                        "min": "-",
                        "now": "-"
                    };
                    for (var i = 0; i < legend.length; i++){
                        var key = sensor_id + '-' + legend[i];
                        if (!response[sensor_id].hasOwnProperty(key)){
                            continue;
                        }
                        var new_data = response[sensor_id][key];
                        // -- add new data
                        series[i]['data'].push.apply( series[i]['data'], new_data );
                        // -- keep the arr length 50
                        if (series[i]['data'].length > 10){
                            series[i]['data'].splice(0, series[i]['data'].length - 10);
                        }
                        // -- get statistics info
                        statistics_info = updateInfo(statistics_info, series[i]['data'], key);
                        $('#experiment-info-' + exp_id + "-" + track_id + "-" + (i+1) + "-1").html(statistics_info['max']);
                        $('#experiment-info-' + exp_id + "-" + track_id + "-" + (i+1) + "-2").html(statistics_info['min']);
                        $('#experiment-now-' + exp_id + "-" + track_id + "-" + (i+1)).html(statistics_info['now']);

                        // -- update newest data timestamp
                        var new_time = Date.parse(new_data[new_data.length - 1]['value'][0]);
                        if (new_time > exp_newest_timestamp[exp_id]){
                            exp_newest_timestamp[exp_id] = new_time;
                        }
                    }

                    // --- update series markArea (if recorder)
                    if (recorder_timestamp.hasOwnProperty(exp_id)){
                        var mark_list = series[0]['markArea']['data'];
                        var recorder_length = recorder_timestamp[exp_id].length;
                        if (recorder_length % 2 == 1){
                            // - recorder ing
                            var mark_index = Math.floor(recorder_length/2);
                            mark_list[mark_index] = [{
                                xAxis: parseTime(recorder_timestamp[exp_id][recorder_length - 1])
                            }, {
                                xAxis: new Date().Format("yyyy-MM-dd HH:mm:ss")
                            }];
                            series[0]['markArea']['data'] = mark_list;
                        }
                    }

                    // -- set new option
                    chart.setOption({
                        series: series
                    });
                } else if (sensor_type == 2){
                    if (recorder_timestamp.hasOwnProperty(exp_id) && recorder_timestamp[exp_id].length % 2 == 1){
                        // --- if in recorder state, update info
                        var start_time = recorder_timestamp[exp_id][recorder_timestamp[exp_id].length - 1];
                        $('#experiment-info-' + exp_id + "-" + track_id + "-1").html(parseTime(start_time));
                        $('#experiment-now-' + exp_id + "-" + track_id + "-1").html(Math.round((Date.now() - Date.parse(start_time))/1000));
                    }
                }
            }
        });
    }

    function updateInfo(statistics_info, data, key){
        if (data.length == 0){
            return statistics_info;
        }
        var max_value = -100000, min_value = 100000;
        var now = 0;
        for (var i=0; i<data.length; i++){
            now = data[i]['value'][1];
            if (now > max_value){
                max_value = now;
            }
            if (now < min_value){
                min_value = now;
            }
        }
        statistics_info['max'] = max_value;
        statistics_info['min'] = min_value;
        statistics_info['now'] = now;

        return statistics_info;
    }
}

function allMonitor() {
    console.log(getQueryString('id'));
}

/**
 * 实验页面的图表
 * @param legend
 * @returns {{tooltip, legend: {top: string, left: string, data: *}, grid: [*], calculable: boolean, toolbox: {show: boolean, feature: {dataView: {readOnly: boolean}, magicType: {show: boolean, type: [string,string]}}, right: number}, dataZoom, xAxis: [*], yAxis: [*], series: Array}}
 */
var experimentChartOption = function (legend) {
    var series = [];
    for (var index in legend){
        var name = legend[index];
        series.push({
            name: name,
            type: 'line',
            symbolSize: 6,
            symbol:'circle',
            hoverAnimation: false,
            smooth: true,
            markArea: {
                silent: true,
                data: []
            },
            data: []
        })
    }

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
                boundaryGap: true
            }
        ],
        series: series
    };
};