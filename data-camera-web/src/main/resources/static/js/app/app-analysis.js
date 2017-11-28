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
    var content_data = {
        4: {
            1: {
                "temp": [{value: ["2017-11-22 20:07:59", 22.05]}, {value: ["2017-11-22 20:08:59", 23.05]}],
                "humi": [{value: ["2017-11-22 20:07:59", 18.05]}, {value: ["2017-11-22 20:08:59", 19.05]}]
            },
            2: {
                "temp-2": [{value: ["2017-11-22 20:07:59", 22.05]}, {value: ["2017-11-22 20:08:59", 23.05]}],
                "humi-2": [{value: ["2017-11-22 20:07:59", 18.05]}, {value: ["2017-11-22 20:08:59", 19.05]}]
            },
            3: {
                "temp-3": [{value: ["2017-11-22 20:07:59", 22.05]}, {value: ["2017-11-22 20:08:59", 23.05]}],
                "humi-3": [{value: ["2017-11-22 20:07:59", 18.05]}, {value: ["2017-11-22 20:08:59", 19.05]}]
            }
        },
        5: {},
        7: {}
    };
    $.ajax({
        type: 'get',
        url: data_addrss + "/content",
        async: false,
        data: {
            "exp-id": exp_id
        },
        success: function (response) {
            if (response.code == "0000") {
                content_data = response.data;
                for (var content_id in content_data){
                    if (!content_data.hasOwnProperty(content_id) || isEmptyObject(content_data[content_id])){
                        continue;
                    }
                    var content_dom = 'analysis-chart-' + exp_id + '-' + content_id;
                    var chart_height = getObjectLength(content_data[content_id]) * (100 + 30) + (25 + 40);
                    var chart = echarts.init(document.getElementById(content_dom), "", opts = {
                        height: chart_height
                    });
                    chart.setOption(analysisChartOption(content_data[content_id]));
                }
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}