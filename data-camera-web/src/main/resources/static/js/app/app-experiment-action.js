/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/6
 *  Description:
 *      实验的监控和录制操作
 */

function initActionStatus(){
    // -- 更改实验状态（如果在监控或录制状态）
    Object.keys(isExperimentMonitor).forEach(function (id) {
        var exp_monitor_btn = $('#experiment-monitor-' + id);
        var exp_monitor_dom = $('#experiment-es-' + id);
        var exp_recorder_btn = $('#experiment-recorder-' + id);
        var exp_recorder_dom = $('#experiment-rs-' + id);

        if (isExperimentMonitor[id] == 1){
            exp_monitor_dom.removeClass('label-default').addClass('label-success').text('正在监控');
            exp_monitor_btn.removeClass('btn-default').addClass('btn-success');

            if (isExperimentRecorder[id] == 1){
                exp_recorder_dom.removeClass('label-default').addClass('label-success').text('正在录制');
                exp_recorder_btn.removeClass('btn-default').addClass('btn-success');

                expObject.setRecorderTime(id, []);
                expObject.recorderTimestamp[id].push(expRecorderTime[id]);

                expObject.setNewTime(id, new Date(parseTime(expRecorderTime[id])).getTime());
            }
            doInterval(id);
        }
    });
}

/**
 * key: exp_id
 * value: exp_interval
 * @type {{}}
 */
var exp_monitor_interval = {};

/**
 * 监控时定期更新图表数据，后期需改成web socket
 * @param exp_id
 */
function doInterval(exp_id){
    exp_monitor_interval[exp_id] = setInterval(function(){
        askForData(exp_id);
    }, 2000);

    /**
     * 从服务器请求最新的监控数据
     * @param exp_id
     */
    function askForData(exp_id) {
        var exp_bound_sensors = boundSensors[exp_id];
        $.get(data_address + "/monitoring", {
            "exp-id": exp_id,
            "timestamp": expObject.newestTimestamp[exp_id]
        }, function (response) {
            if (response.code == "1111"){
                message_info('请求数据失败: ' + response.data, "error");
                return;
            }

            // --- traverse the sensors of this experiment
            exp_bound_sensors.forEach(function (sensor, index) {
                var sensor_type = sensor['sensorConfig']['type'];
                var sensor_dimension = sensor['sensorConfig']['dimension'];
                var sensor_id = sensor['id'];
                var track_id = sensor['trackId'];

                if (sensor_type == 1){
                    for (var dimIndex in sensor_dimension.split(';')){
                        var dim = sensor_dimension.split(';')[dimIndex];
                        var chart_dom = "experiment-track-" + exp_id + "-" + track_id + "-" + dim;
                        if (!response.data.hasOwnProperty(sensor_id) || echarts.getInstanceByDom(document.getElementById(chart_dom)) == null){
                            continue;
                        }
                        if (!response.data[sensor_id].hasOwnProperty(dim)){
                            continue;
                        }
                        // --- init
                        var chart = echarts.getInstanceByDom(document.getElementById(chart_dom));
                        var series = chart.getOption()['series'];
                        // --- update series data
                        var unit = sensor['sensorConfig']['unit'].split(';');
                        var statistics_info = {
                            "max": "-",
                            "min": "-",
                            "now": "-"
                        };
                        // --- add new data
                        var new_data = response.data[sensor_id][dim];
                        series[0]['data'].push.apply( series[0]['data'], new_data );
                        // -- keep the arr length 50
                        if (series[0]['data'].length > 10){
                            series[0]['data'].splice(0, series[0]['data'].length - 10);
                        }
                        // -- get statistics info
                        statistics_info = updateInfo(statistics_info, series[0]['data']);
                        $('#experiment-info-' + exp_id + "-" + track_id + "-" + dim + "-1").html(statistics_info['max']);
                        $('#experiment-info-' + exp_id + "-" + track_id + "-" + dim + "-2").html(statistics_info['min']);
                        $('#experiment-now-' + exp_id + "-" + track_id + "-" + dim).html(statistics_info['now']);
                        // -- update newest data timestamp
                        var new_time = Date.parse(new_data[new_data.length - 1]['value'][0]);
                        if (new_time > expObject.newestTimestamp[exp_id]){
                            expObject.newestTimestamp[exp_id] = new_time;
                        }

                        // --- update series markArea (if recorder)
                        if (expObject.recorderTimestamp.hasOwnProperty(exp_id)){
                            var mark_list = series[0]['markArea']['data'];
                            var recorder_length = expObject.recorderTimestamp[exp_id].length;
                            if (recorder_length % 2 == 1){
                                // - recorder ing
                                var mark_index = Math.floor(recorder_length/2);
                                mark_list[mark_index] = [{
                                    xAxis: parseTime(expObject.recorderTimestamp[exp_id][recorder_length - 1])
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
                    }
                } else if (sensor_type == 2){
                    if (expObject.recorderTimestamp.hasOwnProperty(exp_id) && expObject.recorderTimestamp[exp_id].length % 2 == 1){
                        // --- if in recorder state, update info
                        var start_time = expObject.recorderTimestamp[exp_id][expObject.recorderTimestamp[exp_id].length - 1];
                        $('#experiment-info-' + exp_id + "-" + track_id + "-" + sensor_dimension).html(parseTime(start_time));
                        $('#experiment-now-' + exp_id + "-" + track_id + "-" + sensor_dimension).html(Math.round((Date.now() - Date.parse(start_time))/1000));
                    }
                }
            });
        });
    }

    function updateInfo(statistics_info, data){
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

function getExpStatusFromServer(expId){
    var status = null;
    $.ajax({
        type: 'get',
        url: action_address + "/status",
        async: false,
        data: {
            "exp-id": expId
        },
        success: function (response) {
            if (response.code == "1111"){
                commonObject.printExceptionMsg(response.data);
            } else if (response.code == "0000"){
                var status = response.data;
            }
        },
        error: function (response) {
            commonObject.printRejectMsg();
        }
    });
    return status;
}

/**
 * 实验监控按钮点击触发
 * @param button
 */
function expMonitor(button){
    var expId = button.getAttribute('data');
    $.ajax({
        type: 'get',
        url: action_address + "/status",
        async: false,
        data: {
            "exp-id": expId
        },
        success: function (response) {
            if (response.code == "1111"){
                commonObject.printExceptionMsg(response.data);
            } else if (response.code == "0000"){
                var status = response.data;
                if (status == "not_bound_sensor" || status == "unknown"){
                    message_info("实验未绑定任何设备，不能进行监控", "info");
                } else if (status == "stop"){
                    // 当前状态是非监控，开始监控
                    message_info("开始监控实验" + expId, "success");
                    doMonitor(1);
                } else if (status == "doing"){
                    // 当前状态是监控，停止监控，若正在录制，提示是否保存
                }
            }
        },
        error: function (response) {
            commonObject.printRejectMsg();
        }
    });

    function doMonitor(action) {

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
                message_info('监控操作失败: ' + response.data, "error");
                return;
            }
            var action = response.data;
            if (action == "1"){
                // start monitor
                pageStartMonitor(exp_id);
            } else if (action == "0"){
                // stop monitor
                pageStopMonitor(exp_id);
            }
        },
        error: function (response) {
            message_info("数据请求失败", 'error');
        }
    });
}

/**
 * 全局监控
 *  若有不在监控状态下的实验，则调整为监控
 *  若所有实验都在监控状态，则停止监控
 */
function allMonitor() {
    $.ajax({
        type: 'get',
        url: crud_address + "/allMonitor",
        data: {
            "app-id": app['id']
        },
        success: function (response) {
            if (response.code == "1111"){
                message_info('服务器异常: ' + response.data, "error");
                return;
            }
            var status = response.data;
            if (status['action'] == "close"){
                for (var expId in experiments){
                    pageStopMonitor(expId);
                }
                $('#all-monitor-btn').removeClass('btn-success').addClass('btn-default');
            } else if (status['action'] == "open"){
                for (var exp in status['ids']){
                    pageStartMonitor(status['ids'][exp]);
                }
                $('#all-monitor-btn').removeClass('btn-default').addClass('btn-success');
            }
        },
        error: function (response) {
            message_info("数据请求失败", 'error');
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
                message_info('录制操作无效: ' + response.data, "error");
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

                if (!expObject.recorderTimestamp.hasOwnProperty(exp_id)){
                    expObject.setRecorderTime(exp_id, []);
                }

                var action = response.data;
                if (action == "-10"){
                    message_info("实验" + exp_id + "未绑定传感器，不能记录");
                } else if (action == "-1"){
                    // start recorder
                    message_info("实验" + exp_id + ": 开始记录");
                    exp_state_dom.removeClass('label-default').addClass('label-success').text('正在录制');
                    exp_recorder_btn.removeClass('btn-default').addClass('btn-success');
                    isExperimentRecorder[exp_id] = 1;
                    if (expObject.recorderTimestamp[exp_id].length % 2 == 0){
                        expObject.recorderTimestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
                    } else {
                        expObject.recorderTimestamp[exp_id].pop();
                        expObject.recorderTimestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
                    }
                } else if (action == "0"){
                    message_info("实验" + exp_id + ": 停止记录");
                    pageStopRecorder(exp_id);
                } else {
                    pageStopRecorder(exp_id);
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
 * 全局录制
 *  若有在监控状态下但不在录制状态下的实验，则调整为录制
 *  若所有实验都在录制状态，则停止录制
 */
function allRecord(){
    message_info("developing", "info");
}

/**
 * 开始监控时页面的更改
 * @param exp_id
 */
function pageStartMonitor(exp_id){
    var exp_state_dom = $('#experiment-es-' + exp_id);
    var exp_monitor_btn = $('#experiment-monitor-' + exp_id);

    isExperimentMonitor[exp_id] = 1;
    exp_state_dom.removeClass('label-default').addClass('label-success').text('正在监控');
    exp_monitor_btn.removeClass('btn-default').addClass('btn-success');

    doInterval(exp_id);
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
    $('#experiment-monitor-' + exp_id).removeClass('btn-success').addClass('btn-default');

    clearInterval(exp_monitor_interval[exp_id]);
    delete exp_monitor_interval[exp_id];

    $('.content-value-' + exp_id).html('-');
}

/**
 * 结束录制时页面的更改
 * @param exp_id
 */
function pageStopRecorder(exp_id){
    $('#experiment-rs-' + exp_id).removeClass('label-success').addClass('label-default').text('非录制');
    $('#experiment-recorder-' + exp_id).removeClass('btn-success').addClass('btn-default');
    isExperimentRecorder[exp_id] = 0;
    if (expObject.recorderTimestamp[exp_id].length % 2 == 1) {
        expObject.recorderTimestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
    }
}