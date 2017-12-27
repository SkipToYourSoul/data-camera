/**
 * Belongs to
 * Author: liye on 2017/12/27
 * Description:
 */

// -- 当前选中的数据片段id
var currentRecorder = null;

// -- 当前选中的数据片段data
var currentRecorderData = null;

// -- 标记是否正在回放
var recorderInterval = null;

function recorderPlay() {
    recorderInterval = setInterval(function () {
        console.log(timelineStart + "-" + timelineEnd);
        if (timelineStart >= timelineEnd){
            console.log("clear recorder interval");
            recorderPause();
        }
        setTimeLine(++timelineStart, timelineEnd);
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
    }
}

function recorderPause() {
    clearInterval(recorderInterval);
    recorderInterval = null;
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');
}

function recorderReset() {
    clearInterval(recorderInterval);
    recorderInterval = null;
    $('#play-btn').removeAttr('disabled');
    $('#pause-btn').attr('disabled', 'disabled');
    $(".slider")
        .slider({values: [0, timeline.length - 1]})
        .slider("pips", "refresh")
        .slider("float", "refresh");
}

/**
 * 删除实验片段内容
 */
function deleteContent() {
    bootbox.confirm({
        title: "删除数据片段?",
        message: "确认删除数据片段吗? 当前数据片段id为：" + currentRecorder,
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
                    url: crud_address + '/recorder/delete?content-id=' + currentRecorder,
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