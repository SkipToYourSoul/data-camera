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
function recorderPlay() {
    var interval = 1000/parseInt($('#recorder-speed').find('.active input').val());
    recorderInterval = setInterval(recorderAction, interval);
    analysisObject.playStatus = "play";
    $('#play-btn').attr('disabled', 'disabled');
    $('#pause-btn').removeAttr('disabled');

    // 播放视频
    Object.keys(analysisObject.video).forEach(function (i) {
        var video = analysisObject.video[i];
        video.play();
    });
}

function recorderAction(){
    setTimeLine(++analysisObject.timelineStart, analysisObject.timelineEnd);
    if (analysisObject.timelineStart >= analysisObject.timelineEnd){
        recorderReset();
    }

    // 每次轮询更新时间轴以及图表
    function setTimeLine(start, end){
        // 更新时间轴
        $(".slider")
            .slider({values: [start, end]})
            .slider("pips", "refresh")
            .slider("float", "refresh");

        // 显示时间标记
        $("#timeline-slider").find(".ui-slider-tip").css("visibility", "visible");

        // 重置chart数据
        Object.keys(analysisObject.chart).forEach(function (i) {
            var series = analysisObject.chart[i].getOption()['series'];
            var chartData = analysisObject.getChartData()[i];
            var currentChartData = series[0]['data'];
            series[0]['data'] = getNewChartData(chartData, currentChartData);
            series[0]['markArea']['data'] = [];
            analysisObject.chart[i].setOption({
                series: series
            });
        });

        function getNewChartData(d, cd){
            var n = [];
            var startPoint = (cd.length == d.length)?0:cd.length - 1;
            for (var j=startPoint; j<d.length; j++){
                if (d[j]['value'][0] > analysisObject.timeline[start] + '.000'){
                    n = d.slice(0, j);
                    n.push(d[d.length - 1]);
                    if (n[n.length - 1]['value'].length == 2){
                        n[n.length - 1]['value'].pop();
                    }
                    break;
                }
            }
            return n;
        }
    }
}

function recorderPause() {
    clearInterval(recorderInterval);
    analysisObject.playStatus = "pause";
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');

    // 暂停视频
    Object.keys(analysisObject.video).forEach(function (i) {
        var video = analysisObject.video[i];
        video.pause();
    });
}

function recorderReset() {
    clearInterval(recorderInterval);
    analysisObject.playStatus = "normal";
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');
    $(".slider")
        .slider({values: [0, analysisObject.timeline.length - 1]})
        .slider("pips", "refresh")
        .slider("float", "refresh");
    // 重置chart数据
    Object.keys(analysisObject.chart).forEach(function (i) {
        var series = analysisObject.chart[i].getOption()['series'];
        series[0]['data'] = analysisObject.getChartData()[i];
        series[0]['markArea']['data'] = [[{
            xAxis: analysisObject.timeline[0]
        }, {
            xAxis: analysisObject.timeline[analysisObject.timeline.length - 1]
        }]];
        series[0]['markLine']['data'] = [];
        analysisObject.chart[i].setOption({
            series: series
        });
    });
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
    $('#recorder-current-time').html(analysisObject.secondLine[ui.values[0]]);
    $('#recorder-total-time').html(analysisObject.secondLine[ui.values[1]]);

    // 图表状态
    if (analysisObject.playStatus == "play") {
        updateMarkLine();
    } else if (analysisObject.playStatus == "pause") {
        analysisObject.timelineStart = ui.values[0];
        analysisObject.timelineEnd = ui.values[1];
        updateMarkLine();
        updateVideoTime();
    } else if (analysisObject.playStatus == "normal") {
        analysisObject.timelineStart = ui.values[0];
        analysisObject.timelineEnd = ui.values[1];
        updateMarkArea();
        updateVideoTime();
    }

    // 进行标记线更新
    function updateMarkLine() {
        var line = [{
            xAxis: analysisObject.timeline[ui.values[0]]
        }];
        Object.keys(analysisObject.chart).forEach(function (i) {
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
            video.currentTime(ui.values[0] + analysisObject.videoStartTime);
        });
    }
}

$('#recorder-speed').find('input:radio').change(function(radio){
    var speed = radio.target.getAttribute('value');
    var interval = 1000/parseInt(speed);
    if (recorderInterval != null){
        clearInterval(recorderInterval);
        recorderInterval = setInterval(recorderAction, interval);
    }

    // 调整视频速度
    Object.keys(analysisObject.video).forEach(function (i) {
        var video = analysisObject.video[i];
        video.playbackRate(parseInt(speed));
    });
});


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
function generateNewContent() {
    var dialogMessage = '<div class="row">';
    dialogMessage += '<label class="col-sm-2">片段名</label><div class="col-sm-10" style="margin-bottom: 10px"><input type="text" class="form-control" id="user-new-recorder-name" placeholder="长度不超过10"/></div>';
    dialogMessage += '<label class="col-sm-2">片段描述</label><div class="col-sm-10"><input type="text" class="form-control" id="user-new-recorder-desc"/></div></div>';

    var dialog = bootbox.dialog({
        title: '即将生成新数据片段，时间为<br/><b>' + analysisObject.timeline[analysisObject.timelineStart] + ' - ' + analysisObject.timeline[analysisObject.timelineEnd] + '</b>',
        message: dialogMessage,
        buttons: {
            cancel: {
                label: '<i class="fa fa-times"></i>取消',
                className: 'btn-danger'
            },
            ok: {
                label: '<i class="fa fa-check"></i>确认生成',
                className: 'btn-info',
                callback: function(){
                    var name = $('#user-new-recorder-name').val();
                    var desc = $('#user-new-recorder-desc').val();
                    if (name.length == 0){
                        name = "新片段";
                    }
                    message_info('内容生成中', 'info');
                    $.ajax({
                        type: 'get',
                        url: data_address + "/user-new-recorder",
                        data: {
                            "recorder-id": analysisObject.currentRecorderId,
                            "start": analysisObject.timeline[analysisObject.timelineStart],
                            "end": analysisObject.timeline[analysisObject.timelineEnd],
                            "name": name,
                            "desc": desc
                        },
                        success: function (response) {
                            if (response.code == "1111"){
                                message_info('操作无效: ' + response.data, "error");
                            } else if (response.code == "0000"){
                                window.location.href = current_address + "?id=" + app['id'] + "&tab=2&recorder=" + response.data;
                            }
                        },
                        error: function (response) {
                            message_info('数据请求被拒绝', "error");
                        }
                    });
                }
            }
        }
    });
}

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

function shareContent() {
    window.location.href = base_address + "/share?rid=" + analysisObject.currentRecorderId;
}