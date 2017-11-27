/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/25
 *  Description:
 */

function initResourceOfAnalysisPage(){
    // init the first exp content charts
    for (var exp_id in experiments) {
        initExpContentChart(exp_id);
        break;
    }
}

function initExpContentChart(exp_id){
    $.ajax({
        type: 'get',
        url: data_addrss + "/content",
        data: {
            "exp-id": exp_id
        },
        success: function (response) {
            if (response.code == "0000") {
                message_info("success" + exp_id, "info");
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });

    for (var index in recorders[exp_id]){
        var content = recorders[exp_id][index];
        var content_dom = $('#analysis-chart-' + exp_id + '-' + content['id']);


    }
}