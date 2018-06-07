/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/6
 *  Description:
 *      实验的监控和录制操作
 */
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
        $.ajax({
            type: 'get',
            url: data_address + "/monitoring",
            timeout: 2000,
            data: {
                "exp-id": exp_id,
                "timestamp": expObject.newestTimestamp[exp_id]
            },
            async: true,
            success: function (response) {
                if (response.code == "1111"){
                    commonObject.printExceptionMsg(response.data);
                } else if (response.code == "0000"){
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
                                if (!response.data.hasOwnProperty(sensor_id) || !response.data[sensor_id].hasOwnProperty(dim)
                                    || echarts.getInstanceByDom(document.getElementById(chart_dom)) == null){
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
                                // -- keep the arr length 10
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
                                if (expObject.recorderTimestamp.hasOwnProperty(exp_id) && expObject.recorderTimestamp[exp_id].length == 1){
                                    var mark_list = series[0]['markArea']['data'];
                                    mark_list[0] = [{
                                        xAxis: parseTime(expObject.recorderTimestamp[exp_id][0])
                                    }, {
                                        xAxis: new Date().Format("yyyy-MM-dd HH:mm:ss")
                                    }];
                                    series[0]['markArea']['data'] = mark_list;
                                }

                                // -- set new option
                                chart.setOption({
                                    series: series
                                });
                            }
                        } else if (sensor_type == 2){
                            if (expObject.recorderTimestamp.hasOwnProperty(exp_id) && expObject.recorderTimestamp[exp_id].length == 1){
                                // --- if in recorder state, update info
                                var start_time = expObject.recorderTimestamp[exp_id][0];
                                $('#experiment-info-' + exp_id + "-" + track_id + "-" + sensor_dimension).html(parseTime(start_time));
                                $('#experiment-now-' + exp_id + "-" + track_id + "-" + sensor_dimension).html(Math.round((Date.now() - Date.parse(start_time))/1000));
                            }
                        }
                    });
                }
            }, error: function (response) {
                commonObject.printRejectMsg();
            }
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

/**
 * 获取当前实验状态，用于判断监控和录制的动作进行
 *  NOT_BOUND_SENSOR, MONITORING_NOT_RECORDING, MONITORING_AND_RECORDING, NOT_MONITOR, UNKNOWN
 * Ajax同步请求
 * @param expId
 * @returns {string}
 */
function getExpStatusFromServer(expId){
    var status = "unknown";
    $.ajax({
        type: 'get',
        url: action_address + "/status",
        async: false,
        data: {
            "exp-id": expId
        },
        success: function (response) {
            if (response.code === "1111"){
                commonObject.printExceptionMsg(response.data);
            } else if (response.code === "0000"){
                status = response.data;
            }
        },
        error: function () {
            commonObject.printRejectMsg();
        }
    });
    return status;
}

/**
 * 获取当前应用下实验的整体状态，用于判断全局监控和录制的动作进行
 *  ALL_MONITORING_AND_ALL_RECORDING, ALL_MONITORING_AND_PART_RECORDING, ALL_MONITORING_AND_NO_RECORDING,
 *  ALL_NOT_MONITOR, PART_MONITORING, NO_AVAILABLE_SENSOR
 * Ajax同步请求
 * @returns {string}
 */
function getAppStatusFromServer(){
    var status = "unknown";
    $.ajax({
        type: 'get',
        url: action_address + "/status/all",
        async: false,
        data: {
            "app-id": app['id']
        },
        success: function (response) {
            if (response.code === "1111"){
                commonObject.printExceptionMsg(response.data);
            } else if (response.code === "0000"){
                status = response.data;
            }
        },
        error: function () {
            commonObject.printRejectMsg();
        }
    });
    return status;
}

/**
 * 询问是否需要保存录制的数据片段
 * @returns {number}
 */
function askForSaveRecorder(doFunction, action, endTime, title){
    var msgHtml = '<div class="row"><div class="form-group" style="margin-bottom: 45px"><label class="col-sm-2 control-label">片段名</label>' +
        '<div class="col-sm-10"><input type="text" class="form-control" id="dialog-data-name" placeholder="请输入片段标题"/></div></div>' +
        '<div class="form-group"><label class="col-sm-2 control-label">片段描述</label>' +
        '<div class="col-sm-10"><textarea rows="3" class="form-control" id="dialog-data-desc" placeholder="请输入片段描述"></textarea></div></div>' +
        '</div>';

    bootbox.dialog({
        title: title,
        message: msgHtml,
        async: false,
        buttons: {
            cancel: {
                label: '<i class="fa fa-times"></i> 不保存',
                className: 'btn-danger',
                callback: function(){
                    doFunction(action, 0, endTime);
                }
            },
            confirm: {
                label: '<i class="fa fa-check"></i> 保存',
                className: 'btn-success',
                callback: function(){
                    doFunction(action, 1, endTime);
                }
            }
        }
    });

    // 完成后清空片段数据框
    $('#dialog-data-name').val("");
    $('#dialog-data-desc').val("");
}

/**
 * 实验监控按钮点击触发
 * @param button
 */
function expMonitor(button){
    var expId = button.getAttribute('data');

    lockTheBtn(expId, "monitor");
    var status = getExpStatusFromServer(expId);
    if (status === "unknown"){
        message_info("状态unknown", "info");
        unlockBtn(expId);
    } else if (status === "not_bound_sensor"){
        message_info("实验未绑定任何设备，不能进行监控", "info");
        unlockBtn(expId);
    } else if (status === "not_monitor"){
        // 当前状态是非监控，开始监控
        doMonitor(1, 0, 0);
    } else if (status === "monitoring_not_recording") {
        // 当前状态是监控非录制，停止监控
        doMonitor(0, 0, 0);
    } else if (status === "monitoring_and_recording"){
        askForSaveRecorder(doMonitor, 0, new Date().getTime(), "是否保存录制数据片段?");
    }

    // action -> 0: stop, 1: start
    // isSave -> 0: not save, 1: save
    function doMonitor(action, isSave, endTime) {
        $.ajax({
            type: 'get',
            url: action_address + "/monitor",
            data: {
                "exp-id": expId, "action": action, "isSave": isSave, "data-time": endTime,
                "data-name": $('#dialog-data-name').val(), "data-desc": $('#dialog-data-desc').val()
            },
            success: function (response) {
                unlockBtn(expId);
                var returnStatus = response.data;
                if (response.code === "1111"){
                    commonObject.printExceptionMsg(response.data);
                } else if (response.code === "0000"){
                    if (action === 1){
                        pageStartMonitor(expId);
                    } else if (action === 0){
                        pageStopMonitor(expId);
                        if (isSave === 1) {
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + returnStatus;
                        }
                    }
                }
            },
            error: function () {
                commonObject.printRejectMsg();
            }
        });
    }
}

/**
 * 实验录制按钮点击触发
 * @param button
 */
function expRecorder(button) {
    var expId = button.getAttribute('data');

    lockTheBtn(expId, "record");
    var status = getExpStatusFromServer(expId);
    if (status === "unknown"){
        message_info("状态unknown", "info");
        unlockBtn(expId);
    } else if (status === "not_bound_sensor"){
        message_info("实验未绑定任何设备，不能进行录制", "info");
        unlockBtn(expId);
    } else if (status === "not_monitor"){
        // 当前状态是非监控，不能录制
        message_info("实验未开始监控，不能进行录制", "info");
        unlockBtn(expId);
    } else if (status === "monitoring_not_recording") {
        // 当前状态是监控非录制，开始录制
        doRecorder(1, 0, 0);
    } else if (status === "monitoring_and_recording"){
        // 停止录制
        askForSaveRecorder(doRecorder, 0, new Date().getTime(), "是否保存录制数据片段?");
    }

    function doRecorder(action, isSave, endTime){
        $.ajax({
            type: 'get',
            url: action_address + "/record",
            data: {
                "exp-id": expId, "action": action, "isSave": isSave, "data-time": endTime,
                "data-name": $('#dialog-data-name').val(), "data-desc": $('#dialog-data-desc').val()
            },
            success: function (response) {
                if (response.code === "1111"){
                    commonObject.printExceptionMsg(response.data);
                } else if (response.code === "0000"){
                    unlockBtn(expId);
                    if (action === 1){
                        pageStartRecord(expId);
                    } else if (action === 0){
                        pageStopRecorder(expId);
                        if (isSave === 1 && response.data !== -1){
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + response.data;
                        } else if (response.data === -1){
                            commonObject.printExceptionMsg("录制状态结束异常");
                        }
                    }
                }
            },
            error: function () {
                commonObject.printRejectMsg();
            }
        });
    }
}


/**
 * 全局监控
 *  若有不在监控状态下的实验，则调整为监控
 *  若所有实验都在监控状态，则停止监控
 */
function allMonitor() {
    var status = getAppStatusFromServer();
    console.log("status: " + status);
    if (status == "unknown"){
        message_info("状态unknown", "info");
    } else if (status == "no_available_sensor") {
        message_info("没有可用的传感器组", "info");
    } else if (status == "part_monitoring" || status == "all_not_monitor"){
        bootbox.confirm({
            title: "开始全局监控",
            message: "是否要开始全局监控",
            buttons: {
                cancel: { label: '<i class="fa fa-times"></i> 不开始全局监控' },
                confirm: { label: '<i class="fa fa-check"></i> 开始全局监控' }
            },
            callback: function (result) {
                if (result){
                    doAllMonitor(1, 0, 0);
                }
            }
        });
    } else if (status == "all_monitoring_and_no_recording"){
        bootbox.confirm({
            title: "结束全局监控",
            message: "是否要结束全局监控",
            buttons: {
                cancel: { label: '<i class="fa fa-times"></i> 不结束全局监控' },
                confirm: { label: '<i class="fa fa-check"></i> 结束全局监控' }
            },
            callback: function (result) {
                if (result){
                    doAllMonitor(0, 0, 0);
                }
            }
        });
    } else if (status == "all_monitoring_and_all_recording" || status == "all_monitoring_and_part_recording"){
        var endTime = new Date().getTime();
        askForSaveRecorder(doAllMonitor, 0, endTime, "即将结束全局监控，是否保存录制数据片段?");
    }

    function doAllMonitor(action, isSave, endTime){
        $.ajax({
            type: 'get',
            url: action_address + "/monitor/all",
            data: {
                "app-id": app['id'],
                "action": action,
                "isSave": isSave,
                "data-time": endTime,
                "data-name": $('#dialog-data-name').val(),
                "data-desc": $('#dialog-data-desc').val()
            },
            success: function (response) {
                if (response.code == "1111"){
                    commonObject.printExceptionMsg(response.data);
                } else if (response.code == "0000"){
                    var targetExp = response.data;
                    if (action == 1){
                        $('#all-monitor-btn').removeClass('btn-default').addClass('btn-success');
                        targetExp.forEach(function (expId) {
                            pageStartMonitor(expId);
                        });
                    } else if (action == 0){
                        targetExp.forEach(function (expId) {
                            pageStopMonitor(expId);
                        });
                        if (isSave == 1){
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2";
                        }
                    }
                }
            },
            error: function (response) {
                commonObject.printRejectMsg();
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
    var status = getAppStatusFromServer();
    console.log("status: " + status);
    if (status == "unknown"){
        message_info("状态unknown", "info");
    } else if (status == "no_available_sensor") {
        message_info("没有可用的传感器组", "info");
    } else if (status == "part_monitoring" || status == "all_not_monitor"){
        message_info("当前不是全局监控状态，不能进行全局录制", "info");
    } else if (status == "all_monitoring_and_no_recording"){
        bootbox.confirm({
            title: "开始全局录制",
            message: "是否要开始全局录制",
            buttons: {
                cancel: { label: '<i class="fa fa-times"></i> 不开始全局录制' },
                confirm: { label: '<i class="fa fa-check"></i> 开始全局录制' }
            },
            callback: function (result) {
                if (result){
                    doAllRecord(1, 0, 0);
                }
            }
        });
    } else if (status == "all_monitoring_and_part_recording") {
        askForSaveRecorder(doAllRecord, 1, new Date().getTime(), "即将开始全局录制，是否保存之前已录制的数据片段?");
    } else if (status == "all_monitoring_and_all_recording"){
        askForSaveRecorder(doAllRecord, 0, new Date().getTime(), "即将结束全局录制，是否保存录制的数据片段?");
    }

    function doAllRecord(action, isSave, endTime){
        $.ajax({
            type: 'get',
            url: action_address + "/record/all",
            data: {
                "app-id": app['id'],
                "action": action,
                "isSave": isSave,
                "data-time": endTime,
                "data-name": $('#dialog-data-name').val(),
                "data-desc": $('#dialog-data-desc').val()
            },
            success: function (response) {
                if (response.code == "1111"){
                    commonObject.printExceptionMsg(response.data);
                } else if (response.code == "0000"){
                    var targetExp = response.data;
                    if (action == 1){
                        targetExp.forEach(function (expId) {
                            pageStartRecord(expId);
                        });
                    } else if (action == 0){
                        targetExp.forEach(function (expId) {
                            pageStopRecorder(expId);
                        });
                        if (isSave == 1){
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2";
                        }
                    }
                }
            },
            error: function (response) {
                commonObject.printRejectMsg();
            }
        });
    }
}

function lockTheBtn(expId, action) {
    var $monitorBtn = $('#experiment-monitor-' + expId);
    var $allMonitorBtn = $('#all-monitor-btn');
    var $recorderBtn = $('#experiment-recorder-' + expId);
    var $allRecorderBtn = $('#all-record-btn');

    $monitorBtn.addClass('disabled');
    $allMonitorBtn.addClass('disabled');
    $recorderBtn.addClass('disabled');
    $allRecorderBtn.addClass('disabled');

    if (action === "monitor") {
        $('#experiment-es-' + expId).text("加载中");
    } else if (action === "record") {
        $('#experiment-rs-' + expId).text("加载中");
    }
}

function unlockBtn(expId) {
    var $monitorBtn = $('#experiment-monitor-' + expId);
    var $allMonitorBtn = $('#all-monitor-btn');
    var $recorderBtn = $('#experiment-recorder-' + expId);
    var $allRecorderBtn = $('#all-record-btn');

    $monitorBtn.removeClass('disabled');
    $allMonitorBtn.removeClass('disabled');
    $recorderBtn.removeClass('disabled');
    $allRecorderBtn.removeClass('disabled');
}

/**
 * 开始监控时页面的更改
 * @param exp_id
 */
function pageStartMonitor(exp_id){
    console.info("Page start monitor: " + exp_id);

    var exp_state_dom = $('#experiment-es-' + exp_id);
    isExperimentMonitor[exp_id] = 1;
    exp_state_dom.removeClass('label-default').addClass('label-success').text('正在监控');

    doInterval(exp_id);
}

/**
 * 结束监控时页面的更改
 * @param exp_id
 */
function pageStopMonitor(exp_id){
    pageStopRecorder(exp_id);

    isExperimentMonitor[exp_id] = 0;
    $('#experiment-es-' + exp_id).removeClass('label-success').addClass('label-default').text('非监控');

    clearInterval(exp_monitor_interval[exp_id]);
    delete exp_monitor_interval[exp_id];

    // 清空状态数据
    $('.current-content-value-' + exp_id).html('-');
    $('.content-value-' + exp_id).html('-');

    // 清空echart数据
    Object.keys(expObject.chart).forEach(function (dom) {
        var expId = dom.split('-')[2];
        if (exp_id === expId) {
            var chart = echarts.getInstanceByDom(document.getElementById(dom));
            var series = chart.getOption()['series'];
            series[0]['data'] = [{
                value : [new Date().Format("yyyy-MM-dd HH:mm:ss"), 0]
            }];
            series[0]['markArea']['data'] = [];
            chart.setOption({
                series: series
            });
        }
    });
}

/**
 * 开始录制时页面的更改
 * @param exp_id
 */
function pageStartRecord(exp_id) {
    console.info("Page start recorder: " + exp_id);
    var exp_state_dom = $('#experiment-rs-' + exp_id);

    exp_state_dom.removeClass('label-default').addClass('label-success').text('正在录制');

    expObject.setRecorderTime(exp_id, [new Date().Format("yyyy-MM-dd HH:mm:ss")]);
    isExperimentRecorder[exp_id] = 1;
}

/**
 * 结束录制时页面的更改
 * @param exp_id
 */
function pageStopRecorder(exp_id){
    console.info("Page stop recorder: " + exp_id);
    $('#experiment-rs-' + exp_id).removeClass('label-success').addClass('label-default').text('非录制');
    isExperimentRecorder[exp_id] = 0;
    expObject.setRecorderTime(exp_id, []);
}