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
    var $loader = $("#app-analysis-loading");
    $loader.fakeLoader({
        timeToHide: 10000,
        spinner:"spinner4",
        bgColor:"rgba(154, 154, 154, 0.7)"
    });
    $.ajax({
        type: 'get',
        url: data_addrss + "/content",
        async: false,
        data: {
            "exp-id": exp_id
        },
        success: function (response) {
            if (response.code == "0000") {
                message_info("enter the exp: " + exp_id, "info");
                var content_data = response.data;
                for (var content_id in content_data){
                    var content_dom = 'analysis-chart-' + exp_id + '-' + content_id;
                    if (!content_data.hasOwnProperty(content_id) || isEmptyObject(content_data[content_id])){
                        $('#' + content_dom).html('<strong>该内容段暂未查到相关数据</strong>');
                        continue;
                    }
                    if (echarts.getInstanceByDom(document.getElementById(content_dom)) == null){
                        var chart_height = getObjectLength(content_data[content_id]) * (100 + 30) + (25 + 40);
                        var chart = echarts.init(document.getElementById(content_dom), "", opts = {
                            height: chart_height
                        });
                        chart.setOption(analysisChartOption(content_data[content_id]));
                    } else {
                        echarts.getInstanceByDom(document.getElementById(content_dom)).setOption(analysisChartOption(content_data[content_id]));
                    }
                }

                // complete loading
                $loader.fadeOut();
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}