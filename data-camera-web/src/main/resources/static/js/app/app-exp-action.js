/**
 * web socket init and controller
 * 实验的监控和录制操作
 **/
$(function () {
    $(document).ready(function (){
        connect();
    });

    var $loading = $("#app-main-content");

    var websocket = null;
    function connect() {
        if(!'WebSocket' in window){
            message_info("你的浏览器不支持websocket，不能正常使用监控功能", "error");
        } else {
            if (websocket == null) {
                websocket = new WebSocket("ws://10.221.73.206:8889/dc-websocket");
                console.info("websocket链接中");

                websocket.onopen = function (ev) {
                    console.log("websocket链接成功");
                    // 将监控按钮设置为可见
                    $('.monitor-btn').attr("disabled", false);
                    $('.record-btn').attr("disabled", false);

                    // 注册操作，获取当前页面所有相关设备ID
                    var sensors = [];
                    Object.keys(experiments).forEach(function (expId) {
                        var tracks = experiments[expId]['trackInfoList'];
                        Object.keys(tracks).forEach(function (trackId) {
                            var sensor = tracks[trackId]['sensor'];
                            if (tracks[trackId]['type'] === 1) {
                                sensors.push(sensor['id']);
                            }
                        });
                    });
                    var message = {
                        "type": "REGISTER",
                        "data": {
                            "sensors": sensors
                        }
                    };
                    send(message);
                };

                websocket.onclose = function (ev) {
                    console.log("websocket链接断开");
                    // 将监控按钮设置为不可见
                    $('.monitor-btn').attr("disabled", true);
                    $('.record-btn').attr("disabled", true);
                };

                websocket.onerror = function(){
                    console.log("websocket链接错误");
                    // 将监控按钮设置为不可见
                    $('.monitor-btn').attr("disabled", true);
                    $('.record-btn').attr("disabled", true);
                };

                //接收到消息的回调方法
                websocket.onmessage = function(event){
                    console.info("receive message = " + event.data);
                    var message = $.parseJSON(event.data);
                    switch (message["type"]) {
                        case "START_M":
                            pageStartMonitor(message["data"]["expId"]);
                            break;
                        case "END_M":
                            pageStopMonitor(message["data"]["expId"]);
                            break;
                        case "START_R":
                            pageStartRecord(message["data"]["expId"], new Date().Format("yyyy-MM-dd HH:mm:ss"));
                            break;
                        case "END_R":
                            pageStopRecorder(message["data"]["expId"]);
                            break;
                        case "DATA":
                            var data = message["data"];
                            updateChart(data["trackId"], data["data"], data["timestamp"]);
                    }
                };

                //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
                window.onbeforeunload = function(){
                    websocket.close();
                };
            }
        }
    }

    function send(message){
        if (websocket != null && message !== undefined) {
            websocket.send(JSON.stringify(message));
            console.info("send message = " + JSON.stringify(message));
        }
    }

    function updateChart(trackId, data, time) {
        Object.keys(data).forEach(function (dim) {
            var value = parseFloat(data[dim].toFixed(2));
            var chartDom = "experiment-track-" + trackId + "-" + dim;

            if (echarts.getInstanceByDom(document.getElementById(chartDom)) != null) {
                var chart = echarts.getInstanceByDom(document.getElementById(chartDom));
                var series = chart.getOption()['series'];

                // -- 移除初始化时的节点
                if (series[0]['data'][0].hasOwnProperty("mark")) {
                    series[0]['data'].splice(0, 1);
                }
                series[0]['data'].push({
                    "value": [new Date(time).Format("yyyy-MM-dd HH:mm:ss.S"), value]
                });
                // -- keep the arr length 10
                if (series[0]['data'].length > 10){
                    series[0]['data'].splice(0, series[0]['data'].length - 10);
                }

                // -- 统计值
                updateInfo(series[0]['data']);

                // -- set new option
                chart.setOption({
                    series: series
                });
            }

            function updateInfo(data) {
                var length = data.length;
                if (length > 0) {
                    var max_value = -100000, min_value = 100000;
                    var now = 0;
                    for (var i = 0; i < length; i ++) {
                        now = data[i]['value'][1];
                        if (now > max_value){
                            max_value = now;
                        }
                        if (now < min_value){
                            min_value = now;
                        }
                    }
                    $('#experiment-info-' + trackId + "-" + dim + "-1").html(max_value);
                    $('#experiment-info-' + trackId + "-" + dim + "-2").html(min_value);
                    $('#experiment-now-' + trackId + "-" + dim).html(now);
                }
            }
        });
    }

    /** 监控按钮点击 **/
    $(".monitor-btn").click(function () {
        var id = $(this).attr("id");
        // 点击全局监控
        if (id === "all-monitor-btn") {
            return;
        }

        $loading.mLoading("show");
        var expId = $(this).attr("data");

        // -- 从服务器获取最新的实验监控状态
        switch (getExpStatusFromServer(expId)) {
            case "unknown":
                message_info("服务状态异常，不能进行监控", "error");
                $loading.mLoading("hide");
                break;
            case "not_bound_sensor":
                message_info("实验未绑定任何设备，不能进行监控", "info");
                $loading.mLoading("hide");
                break;
            case "not_monitor":
                // 开始监控
                doMonitor(1, 0, 0);
                break;
            case "monitoring_not_recording":
                // 结束监控
                doMonitor(0, 0, 0);
                break;
            case "monitoring_and_recording":
                // 结束监控和录制
                $loading.mLoading("hide");
                askForSaveRecorder(doMonitor, 0, "是否保存录制数据片段?");
                break;
            default:
                console.info("Unknown status from server, in default case.");
        }

        // 触发监控请求，该请求为同步请求，超时时间为2s
        // action -> 0: stop, 1: start
        // isSave -> 0: not save, 1: save
        function doMonitor(action, isSave) {
            console.info("Do monitor, action = " + action + ", isSave = " + isSave);
            $.ajax({
                type: 'get',
                url: action_address + "/monitor",
                timeout: 2000,
                data: {
                    "exp-id": expId, "action": action, "isSave": isSave,
                    "data-name": $("#dialog-data-name").val(), "data-desc": $("#dialog-data-desc").val()
                },
                success: function (response) {
                    $loading.mLoading("hide");
                    if (response.code === FAILURE){
                        commonObject.printExceptionMsg(response.data);
                    } else if (response.code === SUCCESS){
                        // 发送消息至websocket
                        var message = {
                            "type": (action === 1)?"START_M":"END_M",
                            "data": {
                                "sensors": response.data["sensor"],
                                "expId": expId
                            }
                        };
                        send(message);

                        if (action === 0 && isSave === 1) {
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + response.data["recorder"];
                        }
                    }
                },
                error: function () {
                    commonObject.printRejectMsg();
                    $loading.mLoading("hide");
                }
            });
        }
    });

    /** 全局监控按钮点击 **/
    $('#all-monitor-btn').click(function () {
        $loading.mLoading("show");
        switch (getAppStatusFromServer()) {
            case "unknown":
                message_info("服务状态异常，不能进行监控", "error");
                $loading.mLoading("hide");
                break;
            case "no_available_sensor":
                message_info("没有可用的传感器组，不能进行监控", "info");
                $loading.mLoading("hide");
                break;
            case "has_not_monitor":
                bootBox("是否开始全局监控", 1, 0);
                break;
            case "all_monitoring_and_no_recording":
                bootBox("是否结束全局监控", 0, 0);
                break;
            default:
                $loading.mLoading("hide");
                askForSaveRecorder(doAllMonitor, 0, "即将结束全局监控，是否保存录制数据片段?");
        }

        function bootBox(message, action, isSave) {
            $loading.mLoading("hide");
            bootbox.confirm({
                title: "提示",
                message: message,
                buttons: {
                    cancel: { label: '<i class="fa fa-times"></i> 取消' },
                    confirm: { label: '<i class="fa fa-check"></i> 确认' }
                },
                callback: function (result) {
                    if (result){
                        $loading.mLoading("show");
                        doAllMonitor(action, isSave);
                    }
                }
            });
        }

        function doAllMonitor(action, isSave) {
            $.ajax({
                type: 'get',
                url: action_address + "/monitor/all",
                data: {
                    "app-id": app['id'], "action": action, "isSave": isSave,
                    "data-name": $('#dialog-data-name').val(), "data-desc": $('#dialog-data-desc').val()
                },
                success: function (response) {
                    $loading.mLoading("hide");
                    if (response.code === FAILURE){
                        commonObject.printExceptionMsg(response.data);
                    } else if (response.code === SUCCESS){
                        Object.keys(response.data).forEach(function (expId) {
                            // 发送消息至websocket
                            var message = {
                                "type": (action === 1)?"START_M":"END_M",
                                "data": {
                                    "sensors": response.data[expId]["sensor"],
                                    "expId": expId
                                }
                            };
                            send(message);
                        });
                        if (action === 0 && isSave === 1) {
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2";
                        }
                    }
                },
                error: function () {
                    commonObject.printRejectMsg();
                    $loading.mLoading("hide");
                }
            });
        }
    });

    /** 录制按钮点击 **/
    $(".record-btn").click(function () {
        var id = $(this).attr("id");
        // 点击全局监控
        if (id === "all-record-btn") {
            return;
        }

        $loading.mLoading("show");
        var expId = $(this).attr("data");
        switch (getExpStatusFromServer(expId)) {
            case "unknown":
                message_info("服务状态异常，不能进行监控", "error");
                $loading.mLoading("hide");
                break;
            case "not_bound_sensor":
                message_info("实验未绑定任何设备，不能进行监控", "info");
                $loading.mLoading("hide");
                break;
            case "not_monitor":
                message_info("实验未开始监控，不能进行录制", "info");
                $loading.mLoading("hide");
                break;
            case "monitoring_not_recording":
                // 当前状态是监控非录制，开始录制
                doRecorder(1, 0, 0);
                break;
            case "monitoring_and_recording":
                $loading.mLoading("hide");
                askForSaveRecorder(doRecorder, 0, "是否保存录制数据片段?");
        }

        function doRecorder(action, isSave){
            console.info("Do recorder, action = " + action + ", isSave = " + isSave);
            $.ajax({
                type: 'get',
                url: action_address + "/record",
                data: {
                    "exp-id": expId, "action": action, "isSave": isSave,
                    "data-name": $('#dialog-data-name').val(), "data-desc": $('#dialog-data-desc').val()
                },
                success: function (response) {
                    $loading.mLoading("hide");
                    if (response.code === "1111"){
                        commonObject.printExceptionMsg(response.data);
                    } else if (response.code === "0000"){
                        if (response.data.hasOwnProperty("sensor")) {
                            // 发送消息至websocket
                            var message = {
                                "type": (action === 1)?"START_R":"END_R",
                                "data": {
                                    "sensors": response.data["sensor"],
                                    "expId": expId
                                }
                            };
                            send(message);
                            if (action === 0 && isSave === 1) {
                                window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + response.data['recorder'];
                            }
                        } else {
                            message_info("状态异常", "error");
                        }
                    }
                },
                error: function () {
                    commonObject.printRejectMsg();
                    $loading.mLoading("hide");
                }
            });
        }
    });

    /** 全局录制按钮点击 **/
    $('#all-record-btn').click(function () {
        $loading.mLoading("show");
        switch (getAppStatusFromServer()) {
            case "unknown":
                message_info("服务状态异常，不能进行监控", "error");
                $loading.mLoading("hide");
                break;
            case "no_available_sensor":
                message_info("没有可用的传感器组，不能进行监控", "info");
                $loading.mLoading("hide");
                break;
            case "has_not_monitor":
                message_info("当前不是全局监控状态，不能进行全局录制", "info");
                $loading.mLoading("hide");
                break;
            case "all_monitoring_and_no_recording":
                bootbox.confirm({
                    title: "开始全局录制",
                    message: "是否要开始全局录制",
                    buttons: {
                        cancel: { label: '<i class="fa fa-times"></i> 取消' },
                        confirm: { label: '<i class="fa fa-check"></i> 确认' }
                    },
                    callback: function (result) {
                        if (result){
                            $loading.mLoading("show");
                            doAllRecord(1, 0);
                        }
                    }
                });
                break;
            case "all_monitoring_and_part_recording":
                $loading.mLoading("hide");
                askForSaveRecorder(doAllRecord, 1, "即将开始全局录制，是否保存之前已录制的数据片段?");
                break;
            case "all_monitoring_and_all_recording":
                $loading.mLoading("hide");
                askForSaveRecorder(doAllRecord, 0, "即将结束全局录制，是否保存录制的数据片段?");
        }

        function doAllRecord(action, isSave){
            $.ajax({
                type: 'get',
                url: action_address + "/record/all",
                data: {
                    "app-id": app['id'], "action": action, "isSave": isSave,
                    "data-name": $('#dialog-data-name').val(), "data-desc": $('#dialog-data-desc').val()
                },
                success: function (response) {
                    $loading.mLoading("hide");
                    if (response.code === "1111"){
                        commonObject.printExceptionMsg(response.data);
                    } else if (response.code === "0000"){
                        Object.keys(response.data).forEach(function (expId) {
                            if (response.data[expId].hasOwnProperty("sensor")) {
                                // 发送消息至websocket
                                var message = {
                                    "type": (action === 1)?"START_R":"END_R",
                                    "data": {
                                        "sensors": response.data[expId]["sensor"],
                                        "expId": expId
                                    }
                                };
                                send(message);
                            } else {
                                message_info("实验" + expId + "状态异常", "error");
                            }
                        });
                        if (action === 0 && isSave === 1) {
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2";
                        }
                    }
                },
                error: function () {
                    commonObject.printRejectMsg();
                    $loading.mLoading("hide");
                }
            });
        }
    });

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
    function askForSaveRecorder(doFunction, action, title){
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
                        $loading.mLoading("show");
                        doFunction(action, 0);
                    }
                },
                confirm: {
                    label: '<i class="fa fa-check"></i> 保存',
                    className: 'btn-success',
                    callback: function(){
                        $loading.mLoading("show");
                        doFunction(action, 1);
                    }
                }
            }
        });

        // 完成后清空片段数据框
        $('#dialog-data-name').val("");
        $('#dialog-data-desc').val("");
    }
});

/** 开始监控时页面的更改 **/
function pageStartMonitor(exp_id){
    var exp_state_dom = $('#experiment-es-' + exp_id);
    isExperimentMonitor[exp_id] = 1;
    exp_state_dom.removeClass('label-default').addClass('label-success').text('正在监控');
}

/** 结束监控时页面的更改 **/
function pageStopMonitor(exp_id){
    pageStopRecorder(exp_id);
    isExperimentMonitor[exp_id] = 0;
    $('#experiment-es-' + exp_id).removeClass('label-success').addClass('label-default').text('非监控');

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

/** 开始录制时页面的更改 **/
function pageStartRecord(exp_id, time) {
    var exp_state_dom = $('#experiment-rs-' + exp_id);
    exp_state_dom.removeClass('label-default').addClass('label-success').text('录制中: ' + time);
    isExperimentRecorder[exp_id] = 1;
}

/** 结束录制时页面的更改 **/
function pageStopRecorder(exp_id){
    $('#experiment-rs-' + exp_id).removeClass('label-success').addClass('label-default').text('非录制');
    isExperimentRecorder[exp_id] = 0;
}
