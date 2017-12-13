/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/25
 *  Description:
 */
var analysis_chart_legend_selected = {};
var analysis_chart_data_zoom = {};

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
        url: data_addrss + "/origin-content",
        async: false,
        data: {
            "exp-id": exp_id
        },
        success: function (response) {
            if (response.code == "0000") {
                var content_data = response.data;
                for (var content_id in content_data){
                    if (!content_data.hasOwnProperty(content_id)){
                        continue;
                    }

                    // --- edit able
                    var $content_name_dom = $('#analysis-content-name-' + content_id);
                    var $content_desc_dom = $('#analysis-content-desc-' + content_id);
                    $content_name_dom.editable({
                        type: 'text',
                        pk: content_id,
                        url: crud_address + '/recorder/name',
                        title: '输入标题',
                        success: function(response) {
                            if (response.code == "1111"){
                                message_info('操作无效: ' + response.data, "error");
                            } else if (response.code == "0000"){
                                message_info(response.data, 'success');
                            }
                        },
                        error: function (response) {
                            message_info('修改失败', 'error');
                        }
                    });
                    $content_desc_dom.editable({
                        title: '输入描述',
                        rows: 10,
                        placeholder: '在这里输入描述',
                        pk: content_id,
                        url: crud_address + '/recorder/desc',
                        success: function(response) {
                            if (response.code == "1111"){
                                message_info('操作无效: ' + response.data, "error");
                            } else if (response.code == "0000"){
                                message_info(response.data, 'success');
                            }
                        },
                        error: function (response) {
                            message_info('修改失败', 'error');
                        }
                    });

                    var chart_data = content_data[content_id]['CHART'];
                    var video_data = content_data[content_id]['VIDEO'];
                    if (isEmptyObject(chart_data) && isEmptyObject(video_data)){
                        $('#analysis-info-' + exp_id + '-' + content_id).html('<strong>该内容段暂未查到相关数据</strong>');
                        // -- hide content
                        $('#collapse-' + content_id).removeClass('in');
                        continue;
                    }

                    // --- chart
                    if (!isEmptyObject(chart_data)){
                        var chart_dom = 'analysis-chart-' + exp_id + '-' + content_id;
                        if (echarts.getInstanceByDom(document.getElementById(chart_dom)) == null){
                            var chart_height = getObjectLength(chart_data) * (100 + 30) + (30 + 40);
                            var chart = echarts.init(document.getElementById(chart_dom), "", opts = {
                                height: chart_height
                            });
                            chart.setOption(analysisChartOption(chart_data));
                            // -- bound event
                            chart.on('legendselectchanged', function (params) {
                                console.log(params);
                                var current_legend = "";
                                for (var key in params.selected){
                                    if (params.selected[key] == true){
                                        current_legend += key + ';';
                                    }
                                }
                                if (current_legend.length > 0){
                                    current_legend = current_legend.slice(0, current_legend.length - 1);
                                }
                                analysis_chart_legend_selected[this.dom] = current_legend;
                            }, {'dom': chart_dom});
                            chart.on('datazoom', function (params) {
                                console.log(params);
                                analysis_chart_data_zoom[this.dom] = {
                                    'start': Math.round(params.start),
                                    'end': Math.round(params.end)
                                }
                            }, {'dom': chart_dom});
                            chart.resize({height: chart_height});
                        } else {
                            echarts.getInstanceByDom(document.getElementById(chart_dom)).setOption(analysisChartOption(chart_data));
                        }
                    }

                    // --- video
                    if (!isEmptyObject(video_data)){
                        var $video_dom = $('#analysis-video-' + exp_id + '-' + content_id);
                        $video_dom.addClass('app-analysis-video');
                        for (var sid in video_data){
                            var video_option = video_data[sid]['option'];
                            var video_id = 'video-' + content_id + '-' + sid;
                            var video_div_dom_id = 'video-dom-' + content_id + '-' + sid;
                            // -- current dom not exists
                            if ($('#' + video_div_dom_id).length == 0){
                                if (video_option['sources'] != null){
                                    $video_dom.append('<div id=' + video_div_dom_id + '> <video id="' + video_id + '"' +
                                        'class="video-js vjs-fluid vjs-big-play-centered" data-setup="{}"></video> </div>');
                                    console.log(videojs(video_id, video_option, function () {
                                        videojs.log('The video player ' + video_id + ' is ready');
                                    }));
                                } else {
                                    var progress_bar = '<div class="progress">' +
                                        '<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 45%">' +
                                        '<span class="sr-only">45% Complete</span></div></div>';
                                    $video_dom.append('<div id=' + video_div_dom_id + '> <p class="text-center">视频来自设备(编号：' + sid + ')，上传中</p>' + progress_bar + '</div>');
                                }
                            }
                        }
                    }
                }
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });

    // complete loading
    $loader.fadeOut();
}


/**
 * operation of exp content
 */
function generateNewContent(button) {
    var button_content = button.getAttribute('data');
    var exp_id = button_content.split("-")[0];
    var content_id = button_content.split("-")[1];
    
    var chart_dom = 'analysis-chart-' + exp_id + '-' + content_id;
    var chart = echarts.getInstanceByDom(document.getElementById(chart_dom));
    if (chart != null){
        var option = chart.getOption();
        if (!analysis_chart_data_zoom.hasOwnProperty(chart_dom)){
            analysis_chart_data_zoom[chart_dom] = {
                'start': Math.round(option['dataZoom'][0]['start']),
                'end': Math.round(option['dataZoom'][0]['end'])
            }
        }
        if (!analysis_chart_legend_selected.hasOwnProperty(chart_dom)){
            analysis_chart_legend_selected[chart_dom] = option['legend'][0]['data'].join(';');
        }
    } else {
        message_info("图表中暂无数据，不能生成新的内容", 'error');
        return;
    }

    var dialog_message = '<p>数据起始时间：' + analysis_chart_data_zoom[chart_dom]['start'] + '</p>';
    dialog_message += '<p>数据结束时间：' + analysis_chart_data_zoom[chart_dom]['end'] + '</p>';
    dialog_message += '<p>数据维度：' + analysis_chart_legend_selected[chart_dom] + '</p>';

    var dialog = bootbox.dialog({
        title: '即将生成新的实验数据段，记录如下',
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
                        url: data_addrss + "/new-content",
                        async: false,
                        data: {
                            "exp-id": exp_id,
                            "content-id": content_id,
                            "start": analysis_chart_data_zoom[chart_dom]['start'],
                            "end": analysis_chart_data_zoom[chart_dom]['end'],
                            "legend": analysis_chart_legend_selected[chart_dom]
                        },
                        success: function (response) {
                            if (response.code == "1111"){
                                message_info('操作无效: ' + response.data, "error");
                            } else if (response.code == "0000"){
                                window.location.href = current_address + "?id=" + app['id'];
                            }
                        },
                        error: function (response) {
                            message_info('操作无效', "error");
                        }
                    });
                }
            }
        }
    });
}