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
    recorderInterval = setInterval(function () {
        setTimeLine(++analysisObject.timelineStart, analysisObject.timelineEnd);
        if (analysisObject.timelineStart >= analysisObject.timelineEnd){
            recorderPause();
            recorderInterval = null;
            // 隐藏时间标记
            $("#timeline-slider").find(".ui-slider-tip").css("visibility", "");
        }
    }, 1000);
    $('#play-btn').attr('disabled', 'disabled');
    $('#pause-btn').removeAttr('disabled');

    function setTimeLine(start, end){
        $(".slider")
            .slider({values: [start, end]})
            .slider("pips", "refresh")
            .slider("float", "refresh");

        // 显示时间标记
        $("#timeline-slider").find(".ui-slider-tip").css("visibility", "visible");

        // 重置chart数据
        for (var i in analysisObject.chart){
            var series = analysisObject.chart[i].getOption()['series'];
            var chartData = analysisObject.getChartData()[i];
            series[0]['data'] = getNewChartData(chartData);
            series[0]['markArea']['data'] = [];
            analysisObject.chart[i].setOption({
                series: series
            });
        }

        function getNewChartData(d){
            var n = [];
            for (var j=0; j<d.length; j++){
                n.push(d[j]);
                if (d[j]['value'][0] > analysisObject.timeline[start] && d[j]['value'].length == 2){
                    n[j]['value'].pop();
                }
            }
            return n;
        }
    }
}

function recorderPause() {
    clearInterval(recorderInterval);
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');
}

function recorderReset() {
    clearInterval(recorderInterval);
    recorderInterval = null;
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');
    $(".slider")
        .slider({values: [0, analysisObject.timeline.length - 1]})
        .slider("pips", "refresh")
        .slider("float", "refresh");
    // 重置chart数据
    for (var i in analysisObject.chart){
        var series = analysisObject.chart[i].getOption()['series'];
        series[0]['data'] = analysisObject.getChartData()[i];
        series[0]['markArea']['data'] = [[{
            xAxis: analysisObject.timeline[0]
        }, {
            xAxis: analysisObject.timeline[analysisObject.timeline.length - 1]
        }]];
        analysisObject.chart[i].setOption({
            series: series
        });
    }
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
function generateNewContent() {
    var dialog_message = '<p>数据起始时间：' + analysisObject.timeline[analysisObject.timelineStart] + '</p>';
    dialog_message += '<p>数据结束时间：' + analysisObject.timeline[analysisObject.timelineEnd] + '</p>';

    var dialog = bootbox.dialog({
        title: '即将生成新的数据片段，记录如下',
        message: dialog_message,
        buttons: {
            cancel: {
                label: '<i class="fa fa-times"></i>取消',
                className: 'btn-danger'
            },
            ok: {
                label: '<i class="fa fa-check"></i>确认生成',
                className: 'btn-info',
                callback: function(){
                    message_info('内容生成中', 'info');
                    $.ajax({
                        type: 'get',
                        url: data_addrss + "/user-new-recorder",
                        data: {
                            "recorder-id": analysisObject.currentRecorderId,
                            "start": analysisObject.timeline[analysisObject.timelineStart],
                            "end": analysisObject.timeline[analysisObject.timelineEnd]
                        },
                        success: function (response) {
                            if (response.code == "1111"){
                                message_info('操作无效: ' + response.data, "error");
                            } else if (response.code == "0000"){
                                window.location.href = current_address + "?id=" + app['id'] + "&tab=2";
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