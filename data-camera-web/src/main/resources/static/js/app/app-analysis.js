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
                    // --- chart
                    if (content_data.hasOwnProperty(content_id) && content_data[content_id].hasOwnProperty("CHART")){
                        var content_dom = 'analysis-chart-' + exp_id + '-' + content_id;
                        var chart_data = content_data[content_id]['CHART'];
                        if (isEmptyObject(chart_data)){
                            $('#' + content_dom).html('<strong>该内容段暂未查到相关数据</strong>');
                            continue;
                        }
                        if (echarts.getInstanceByDom(document.getElementById(content_dom)) == null){
                            var chart_height = getObjectLength(chart_data) * (100 + 30) + (25 + 40);
                            var chart = echarts.init(document.getElementById(content_dom), "", opts = {
                                height: chart_height
                            });
                            chart.setOption(analysisChartOption(chart_data));
                        } else {
                            echarts.getInstanceByDom(document.getElementById(content_dom)).setOption(analysisChartOption(chart_data));
                        }
                    }

                    // --- video
                    if (content_data.hasOwnProperty(content_id) && content_data[content_id].hasOwnProperty("VIDEO")){
                        var content_dom = 'analysis-video-' + exp_id + '-' + content_id;
                        var video_data = content_data[content_id]['VIDEO'];
                        var video_html = "";
                        for (var sid in video_data){
                            video_html += '<p>' + sid + '</p>';
                            if (null != video_data[sid]){
                                video_html += '<p>' + video_data[sid] + '</p>';
                            }
                        }
                        $('#' + content_dom).html(video_html);
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