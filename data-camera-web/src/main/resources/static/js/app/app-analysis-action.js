/**
 * Belongs to
 * Author: liye on 2017/12/27
 * Description:
 */

/**
 * 录制相关操作，play, pause, reset
 * @type {null}
 */
var recorderInterval = null;
var timeLineInterval = null;
var lastSlideValue = 0;

/**
 * 点击播放按钮时触发
 */
function recorderPlay() {
    if (analysisObject.playStatus === "play") {
        message_info("正在回放", "info", 3);
        return;
    }

    // 开始播放
    console.info("Recorder play at ", new Date());
    analysisObject.playStatus = "play";

    var speed = parseFloat($('#recorder-speed').selectpicker('val'));
    var startTime = analysisObject.timeline[analysisObject.timelineStart];
    recorderInterval = chartDataPlay(startTime, 500, speed);
    timeLineInterval = timeLinePlay(1000, speed);

    // 播放视频
    if (analysisObject.timelineStart !== analysisObject.timelineEnd) {
        Object.keys(analysisObject.video).forEach(function (i) {
            var video = analysisObject.video[i];
            video.playbackRate(speed);
            video.play();
        });
    }

    // 更改界面样式
    $('#play-btn').attr('disabled', 'disabled');
    $('#pause-btn').removeAttr('disabled');
}

function recorderPause() {
    if (analysisObject.playStatus !== "play") {
        message_info("不在回放", "info", 3);
        return;
    }

    console.info("Recorder pause at ", new Date());
    analysisObject.playStatus = "pause";

    clearInterval(recorderInterval);
    clearInterval(timeLineInterval);

    // 暂停视频
    Object.keys(analysisObject.video).forEach(function (i) {
        var video = analysisObject.video[i];
        video.pause();
    });

    // 更改界面样式
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');
}

function recorderComplete() {
    if (recorderInterval == null && timeLineInterval == null) {
        analysisObject.playStatus = "normal";
        $('#play-btn').removeAttr('disabled');
        $('#pause-btn').attr('disabled', 'disabled');

        // 暂停视频
        Object.keys(analysisObject.video).forEach(function (i) {
            var video = analysisObject.video[i];
            video.pause();
        });
    }
}

function recorderReset() {
    console.info("Recorder reset");
    clearInterval(recorderInterval);
    clearInterval(timeLineInterval);

    analysisObject.playStatus = "normal";
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');
    $(".slider")
        .slider({values: [0, analysisObject.timeline.length - 1]})
        .slider("pips", "refresh")
        .slider("float", "refresh");
    lastSlideValue = 0;

    // 重置chart数据
    Object.keys(analysisObject.chart).forEach(function (i) {
        var series = analysisObject.chart[i].getOption()['series'];
        if (i.startsWith("define")) {
            series[0]['data'] = analysisObject.getChartData()[i]['data'];
        } else {
            series[0]['data'] = analysisObject.getChartData()[i];
            /*series[0]['markArea']['data'] = [[{
                xAxis: analysisObject.timeline[0]
            }, {
                xAxis: analysisObject.timeline[analysisObject.timeline.length - 1]
            }]];*/
            series[0]['markLine']['data'] = [];
        }

        analysisObject.chart[i].setOption({
            series: series
        });
    });

    // 清空数据方块数值
    $(".cube-text").html("-");

    // 隐藏时间标记
    $("#timeline-slider").find(".ui-slider-tip").css("visibility", "");

    // 重置视频
    Object.keys(analysisObject.video).forEach(function (i) {
        var video = analysisObject.video[i];
        video.pause();
        video.currentTime(analysisObject.videoStartTime);
    });
}

function slideChange(e, ui) {
    // 时间轴标注
    // todo：显示真实视频播放时间，后续需接入摄像头录制倍数
    $('#recorder-current-time').html(/*analysisObject.secondLine[ui.values[0]]*/ui.values[0]);
    $('#recorder-total-time').html(/*analysisObject.secondLine[ui.values[1]]*/ ui.values[1] + "s");

    analysisObject.timelineStart = ui.values[0];
    analysisObject.timelineEnd = ui.values[1];

    // 图表状态
    if (analysisObject.playStatus === "play" || analysisObject.playStatus === "pause") {
        // 手动改变时间轴，更改视频时间
        if (Math.abs(ui.values[0] - lastSlideValue) > 1) {
            updateVideoTime();
        }
    } else if (analysisObject.playStatus === "normal") {
        updateMarkLine();
        // updateMarkArea();
        updateVideoTime();
    }
    lastSlideValue = ui.values[0];

    // 进行标记线更新
    function updateMarkLine() {
        var line = [{
            xAxis: analysisObject.timeline[ui.values[0]]
        }];
        Object.keys(analysisObject.chart).forEach(function (i) {
            if (i.startsWith("define")) {
                return;
            }
            var series = analysisObject.chart[i].getOption()['series'];
            series[0]['markLine']['data'] = line;
            analysisObject.chart[i].setOption({
                series: series
            });
        });
    }

    // 进行标记区域更新
    function updateMarkArea() {
        var mark = [[{
            xAxis: analysisObject.timeline[analysisObject.timelineStart]
        }, {
            xAxis: analysisObject.timeline[analysisObject.timelineEnd]
        }]];
        Object.keys(analysisObject.chart).forEach(function (i) {
            var series = analysisObject.chart[i].getOption()['series'];
            series[0]['markArea']['data'] = mark;
            analysisObject.chart[i].setOption({
                series: series
            });
        });
    }

    // 视频时间调整
    function updateVideoTime() {
        Object.keys(analysisObject.video).forEach(function (i) {
            var video = analysisObject.video[i];
            var vTime = parseInt(ui.values[0]) + parseInt(analysisObject.videoStartTime);
            console.log("Set video time: " + vTime);
            video.currentTime(vTime);
        });
    }
}

function changeSpeed(select) {
    console.info($(select).selectpicker('val'));
}

/**
 * 删除实验片段内容
 */
function deleteContent() {
    bootbox.confirm({
        title: "删除数据片段?",
        message: "确认删除数据片段吗? 当前数据片段id为：" + analysisObject.currentRecorderId,
        buttons: {
            cancel: {
                label: '<i class="fa fa-times"></i> 取消'
            },
            confirm: {
                label: '<i class="fa fa-check"></i> 确认删除'
            }
        },
        callback: function (result) {
            if (result){
                $.ajax({
                    type: 'get',
                    url: crud_address + '/recorder/delete?recorder-id=' + analysisObject.currentRecorderId,
                    success: function (response) {
                        if (response.code == "0000") {
                            window.location.href = current_address + "?id=" + app['id'] + "&tab=2";
                        } else if (response.code == "1111"){
                            message_info("删除数据片段失败，失败原因为：" + response.data, 'error');
                        }
                    },
                    error: function (response) {
                        message_info("数据请求被拒绝", 'error');
                    }
                });
            }
        }
    });
}

/**
 * 生成用户自定义的数据片段
 */
$('#new-data-recorder-form').formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'new-recorder-name': {validators: {notEmpty: {message: '不能为空'},
        stringLength: {max: 10}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    message_info('内容生成中', 'info');
    $.ajax({
        type: 'get',
        url: data_address + "/user-new-recorder",
        data: {
            "recorder-id": analysisObject.currentRecorderId,
            "start": analysisObject.timeline[analysisObject.timelineStart],
            "end": analysisObject.timeline[analysisObject.timelineEnd],
            "name": $('#new-recorder-name').val(),
            "desc": $('#new-recorder-desc').val()
        },
        success: function (response) {
            if (response.code === "1111"){
                commonObject.printExceptionMsg(response.data);
            } else if (response.code === "0000"){
                window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + response.data;
            }
        },
        error: function (response) {
            commonObject.printRejectMsg();
        }
    });
}).on('err.form.fv', function (evt) {
    commonObject.printRejectMsg();
});

/**
 * 保存更新数据片段描述
 */
function saveDataDesc(){
    var $appAnalysisTitle = $('#app-analysis-title');
    var $appAnalysisDesc = $('#app-analysis-desc');

    $.ajax({
        type: 'get',
        url: crud_address + '/recorder/desc',
        data: {
            "title": $appAnalysisTitle.val(),
            "desc": $appAnalysisDesc.val(),
            "id": analysisObject.currentRecorderId
        },
        success: function (response) {
            if (response.code == "0000"){
                message_info("保存成功", "success");
                Object.keys(recorders).forEach(function (rid) {
                    if (rid == analysisObject.currentRecorderId) {
                        recorders[rid]['name'] = $appAnalysisTitle.val();
                        recorders[rid]['description'] = $appAnalysisDesc.val();
                    }
                });
            } else if (response.code == "1111") {
                message_info('操作无效: ' + response.data, "error");
            }
        },
        error: function (response) {
            message_info("数据请求被拒绝", 'error');
        }
    });
}

/** 分享内容 **/
function shareContent() {
    window.location.href = base_address + "/share?rid=" + analysisObject.currentRecorderId;
}

/** 新增用户自定义图表 **/
$('#new-analysis-chart-modal').on('show.bs.modal', function (m) {
    var usableSensor = {};
    var $select = $('#new-analysis-chart-select');
    var count = 1;
    $select.empty();
    JSON.parse(recorders[analysisObject.currentRecorderId]['devices']).forEach(function (device) {
        if (device['legends'].length > 1) {
            var sensorId = device['sensor'];
            var trackId = device['track'];
            usableSensor[sensorId] = device['legends'];
            $select.append('<option value="' + sensorId + '">' + tracks[trackId]['sensor']['name'] + '</option>');

            if (count === 1) {
                device['legends'].forEach(function (legend) {
                    $('.new-analysis-chart-legend-select').append('<option value="' + legend + '">' + legend + '</option>');
                });
            }
            count ++;
        }
    });
    if (!isEmptyObject(usableSensor)) {
        $select.change(function () {
            $('.new-analysis-chart-legend-select').empty();
            usableSensor[$select.val()].forEach(function (legend) {
                $('.new-analysis-chart-legend-select').append('<option value="' + legend + '">' + legend + '</option>');
            });
        });
    } else {
        $('#new-analysis-chart-modal').modal('hide');
    }
});

$('#new-analysis-chart-form').formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'name-input': {validators: {notEmpty: {message: '名称不能为空'}}},
        'desc-input': {validators: {notEmpty: {message: '描述不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    $.ajax({
        type: 'post',
        url: data_address + "/user-new-chart",
        data: $(this).serialize() + "&recorder-id=" + analysisObject.currentRecorderId,
        success: function (response) {
            if (response.code === "0000"){
                window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + response.data["recorder"] + "&chart=" + response.data["id"];
            } else if (response.code === "1111") {
                commonObject.printExceptionMsg(response.data);
            }
        },
        error: function (response) {
            commonObject.printRejectMsg();
        }
    });
}).on('err.form.fv', function (evt) {
    commonObject.printRejectMsg();
});

