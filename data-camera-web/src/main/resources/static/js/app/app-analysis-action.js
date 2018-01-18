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
    $('#play-btn').attr('disabled', 'disabled');
    $('#pause-btn').removeAttr('disabled');
}

function recorderAction(){
    setTimeLine(++analysisObject.timelineStart, analysisObject.timelineEnd);
    if (analysisObject.timelineStart >= analysisObject.timelineEnd){
        recorderPause();
        recorderInterval = null;
        // 隐藏时间标记
        $("#timeline-slider").find(".ui-slider-tip").css("visibility", "");
    }

    function setTimeLine(start, end){
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
            series[0]['data'] = getNewChartData(chartData);
            series[0]['markArea']['data'] = [];
            analysisObject.chart[i].setOption({
                series: series
            });
        });

        function getNewChartData(d){
            var n = [];
            // find point
            var point = 0;
            for (var j=0; j<d.length; j++){
                if (d[j]['value'][0] > analysisObject.timeline[start] + '.000'){
                    point = j;
                    n = d.slice(0, point);
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
        series[0]['markLine']['data'] = [];
        analysisObject.chart[i].setOption({
            series: series
        });
    }
}

$('#recorder-speed').find('input:radio').change(function(radio){
    var speed = radio.target.getAttribute('value');
    var interval = 1000/parseInt(speed);
    clearInterval(recorderInterval);
    recorderInterval = setInterval(recorderAction, interval);
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
                        url: data_address + "/user-new-recorder",
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

/**
 * 保存更新数据片段描述
 */
function saveDataDesc(){
    var $appAnalysisDesc = $('#app-analysis-desc');

    $.ajax({
        type: 'get',
        url: crud_address + '/recorder/desc',
        data: {
            "desc": $appAnalysisDesc.val(),
            "id": analysisObject.currentRecorderId
        },
        success: function (response) {
            if (response.code == "0000"){
                message_info("保存成功", "success");
                for (var index=0; index<recorders[app['id']].length; index++){
                    if (recorders[app['id']][index]['id'] == analysisObject.currentRecorderId){
                        recorders[app['id']][index]['description'] = $appAnalysisDesc.val();
                    }
                }
            } else if (response.code == "1111") {
                message_info('操作无效: ' + response.data, "error");
            }
        },
        error: function (response) {
            message_info("数据请求被拒绝", 'error');
        }
    });
}

/**
 * 发布内容
 */
$('#content-form').formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'content-name': {validators: {notEmpty: {message: '不能为空'}}},
        'content-desc': {validators: {notEmpty: {message: '不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    $.ajax({
        type: 'post',
        url: crud_address + '/content/new',
        data: $(this).serialize() + "&recorder-id=" + analysisObject.currentRecorderId,
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = base_address + "/content?id=" + response.data['id'];
            } else if (response.code == "1111") {
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

$('#content-modal').on('show.bs.modal', function (evt) {
    $('#content-name').val("");
    $('#content-desc').val("");
    $('#content-tag').val("");
});